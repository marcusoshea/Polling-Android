package com.pollingandroid.ui.candidates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollingandroid.repository.CandidateRepository
import com.pollingandroid.repository.PollingOrderRepository
import com.pollingandroid.ui.candidates.models.Candidate
import com.pollingandroid.ui.candidates.models.CandidateImage
import com.pollingandroid.ui.candidates.models.ExternalNote
import com.pollingandroid.ui.candidates.models.PollingGroup
import com.pollingandroid.ui.candidates.models.PollingNote
import com.pollingandroid.ui.login.SecureStorage
import com.pollingandroid.util.UserUtils
import kotlinx.coroutines.launch

class CandidatesViewModel : ViewModel() {

    private val repository = CandidateRepository()
    val pollingOrderName: LiveData<String> = PollingOrderRepository.pollingOrderName

    private val _candidates = MutableLiveData<List<Candidate>>(emptyList())
    val candidates: LiveData<List<Candidate>> = _candidates

    private val _selectedCandidate = MutableLiveData<Candidate?>()
    val selectedCandidate: LiveData<Candidate?> = _selectedCandidate

    private val _pollingNotes = MutableLiveData<List<PollingNote>>(emptyList())
    val pollingNotes: LiveData<List<PollingNote>> = _pollingNotes

    private val _externalNotes = MutableLiveData<List<ExternalNote>>(emptyList())
    val externalNotes: LiveData<List<ExternalNote>> = _externalNotes

    private val _pollingGroups = MutableLiveData<List<PollingGroup>>(emptyList())
    val pollingGroups: LiveData<List<PollingGroup>> = _pollingGroups

    private val _candidateImages = MutableLiveData<List<CandidateImage>>(emptyList())
    val candidateImages: LiveData<List<CandidateImage>> = _candidateImages

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _newNoteText = MutableLiveData<String>("")
    val newNoteText: LiveData<String> = _newNoteText

    private val _isPrivateNote = MutableLiveData<Boolean>(false)
    val isPrivateNote: LiveData<Boolean> = _isPrivateNote

    private val _showPollingNotes = MutableLiveData<Boolean>(true)
    val showPollingNotes: LiveData<Boolean> = _showPollingNotes

    private val _showExternalNotes = MutableLiveData<Boolean>(true)
    val showExternalNotes: LiveData<Boolean> = _showExternalNotes

    private val _noteAddedSuccess = MutableLiveData<Boolean?>(null)
    val noteAddedSuccess: LiveData<Boolean?> = _noteAddedSuccess

    init {
        loadCandidates()
    }

    fun loadCandidates() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Get auth token and order ID from storage
                val authToken =
                    SecureStorage.retrieve("accessToken")?.let { UserUtils.decryptData(it) }
                val orderIdString = SecureStorage.retrieve("pollingOrder")
                val orderId = orderIdString?.toIntOrNull() ?: 0

                if (authToken != null && orderId > 0) {
                    val candidatesList = repository.getAllCandidates(orderId, authToken)
                    _candidates.value = candidatesList

                    if (candidatesList.isEmpty()) {
                        _errorMessage.value = "No candidates found for this order"
                    }
                } else {
                    _errorMessage.value = "Authentication error or invalid order ID"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading candidates: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectCandidate(candidate: Candidate) {
        _selectedCandidate.value = candidate
        loadCandidateNotes(candidate.candidate_id)
        loadCandidateImages(candidate.candidate_id)
    }

    fun clearSelectedCandidate() {
        _selectedCandidate.value = null
        _pollingNotes.value = emptyList()
        _externalNotes.value = emptyList()
        _pollingGroups.value = emptyList()
        _candidateImages.value = emptyList()
    }

    fun loadCandidateNotes(candidateId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val authToken =
                    SecureStorage.retrieve("accessToken")?.let { UserUtils.decryptData(it) }

                if (authToken != null) {
                    // Load both polling notes and external notes concurrently
                    val pollingNotesList = repository.getPollingNotes(candidateId, authToken)
                    val externalNotesList = repository.getExternalNotes(candidateId, authToken)

                    _pollingNotes.value = pollingNotesList
                    _externalNotes.value = externalNotesList

                    // Log external notes data for debugging


                    // Group polling notes by polling
                    _pollingGroups.value = repository.groupPollingNotesByPolling(pollingNotesList)
                } else {
                    _errorMessage.value = "Authentication error"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading candidate notes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCandidateImages(candidateId: Int) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val authToken =
                    SecureStorage.retrieve("accessToken")?.let { UserUtils.decryptData(it) }

                if (authToken != null) {
                    val images = repository.getCandidateImages(candidateId, authToken)
                    _candidateImages.value = images
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading candidate images: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateNewNoteText(text: String) {
        _newNoteText.value = text
    }

    fun toggleIsPrivateNote() {
        _isPrivateNote.value = !(_isPrivateNote.value ?: false)
    }

    fun toggleShowPollingNotes() {
        _showPollingNotes.value = !(_showPollingNotes.value ?: true)
    }

    fun toggleShowExternalNotes() {
        _showExternalNotes.value = !(_showExternalNotes.value ?: true)
    }

    fun addExternalNote() {
        val candidateId = _selectedCandidate.value?.candidate_id ?: return
        val noteText = _newNoteText.value?.trim() ?: ""
        val isPrivate = _isPrivateNote.value ?: false

        if (noteText.isEmpty()) {
            _errorMessage.value = "Note text cannot be empty"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val authToken =
                    SecureStorage.retrieve("accessToken")?.let { UserUtils.decryptData(it) }

                if (authToken != null) {
                    val success =
                        repository.createExternalNote(candidateId, noteText, isPrivate, authToken)

                    if (success) {
                        // Clear the note input
                        _newNoteText.value = ""
                        _noteAddedSuccess.value = true

                        // Reload notes to show the newly added note
                        loadCandidateNotes(candidateId)
                    } else {
                        _errorMessage.value = "Failed to add note"
                        _noteAddedSuccess.value = false
                    }
                } else {
                    _errorMessage.value = "Authentication error"
                    _noteAddedSuccess.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error adding note: ${e.message}"
                _noteAddedSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteExternalNote(noteId: Int) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val authToken =
                    SecureStorage.retrieve("accessToken")?.let { UserUtils.decryptData(it) }
                val candidateId = _selectedCandidate.value?.candidate_id

                if (authToken != null && candidateId != null) {
                    val success = repository.deleteExternalNote(noteId, authToken)

                    if (success) {
                        // Reload notes after deletion
                        loadCandidateNotes(candidateId)
                    } else {
                        _errorMessage.value = "Failed to delete note"
                    }
                } else {
                    _errorMessage.value = "Authentication error"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting note: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleCandidateWatchlist(candidate: Candidate) {
        val updatedCandidate = candidate.copy(watch_list = !candidate.watch_list)

        // Update the candidate in the list
        _candidates.value = _candidates.value?.map {
            if (it.candidate_id == candidate.candidate_id) updatedCandidate else it
        }

        // If this is the currently selected candidate, update that too
        if (_selectedCandidate.value?.candidate_id == candidate.candidate_id) {
            _selectedCandidate.value = updatedCandidate
        }

        // Send the update to the server
        viewModelScope.launch {
            try {
                val authToken =
                    SecureStorage.retrieve("accessToken")?.let { UserUtils.decryptData(it) }

                if (authToken != null) {
                    val success = repository.toggleWatchlist(
                        candidate.candidate_id,
                        updatedCandidate.watch_list,
                        authToken
                    )

                    if (!success) {
                        // Revert the change if the API call fails
                        _candidates.value = _candidates.value?.map {
                            if (it.candidate_id == candidate.candidate_id) candidate else it
                        }

                        if (_selectedCandidate.value?.candidate_id == candidate.candidate_id) {
                            _selectedCandidate.value = candidate
                        }

                        _errorMessage.value = "Failed to update watchlist status"
                    }
                }
            } catch (e: Exception) {
                // Revert the change if there's an exception
                _candidates.value = _candidates.value?.map {
                    if (it.candidate_id == candidate.candidate_id) candidate else it
                }

                if (_selectedCandidate.value?.candidate_id == candidate.candidate_id) {
                    _selectedCandidate.value = candidate
                }

                _errorMessage.value = "Error updating watchlist status: ${e.message}"
            }
        }
    }

    fun resetNoteAddedStatus() {
        _noteAddedSuccess.value = null
    }
}