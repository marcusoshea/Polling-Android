package com.pollingandroid.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pollingandroid.repository.PollingReportRepository
import com.pollingandroid.repository.PollingReportRepository.ReportData
import com.pollingandroid.ui.login.SecureStorage
import com.pollingandroid.util.UserUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class PollingSummary(
    val pollingName: String,
    val startDate: String,
    val endDate: String,
    val pollingOrderPollingType: Int = 0,
    val pollingOrderParticipation: Int = 0,
    val pollingOrderScore: Int = 0,
    val activeMembers: Int = 0,
    val participatingMembers: Int = 0,
    val participationRate: String = "0",
    val certified: String = "not certified."
)

data class VoteTotal(
    val candidateId: Int,
    val pollingOrderId: Int,
    val name: String,
    val vote: String,
    val total: Int
)

data class Candidate(
    val name: String,
    val recommended: String,
    val inProcessRating: String,
    val voteCounts: Map<String, Int> = mapOf(), // Yes, No, Wait, Abstain vote counts
    val notes: List<Note> = emptyList()
)

data class Note(
    val note: String,
    val private: Boolean,
    val memberName: String
)

class ReportViewModel : ViewModel() {

    private val repository = PollingReportRepository()

    private val _pollingTitle = MutableStateFlow("Polling Report")
    val pollingTitle: StateFlow<String> = _pollingTitle

    private val _closedPollingAvailable = MutableStateFlow(true)
    val closedPollingAvailable: StateFlow<Boolean> = _closedPollingAvailable

    private val _inProcessPollingAvailable = MutableStateFlow(false)
    val inProcessPollingAvailable: StateFlow<Boolean> = _inProcessPollingAvailable

    private val _candidateList = MutableStateFlow<List<Candidate>>(emptyList())
    val candidateList: StateFlow<List<Candidate>> = _candidateList

    private val _pollingSummary = MutableStateFlow<PollingSummary?>(null)
    val pollingSummary: StateFlow<PollingSummary?> = _pollingSummary

    private val _pollingTotals = MutableStateFlow<List<VoteTotal>>(emptyList())
    val pollingTotals: StateFlow<List<VoteTotal>> = _pollingTotals

    private val _pollingOrderName = MutableStateFlow("")
    val pollingOrderName: StateFlow<String> = _pollingOrderName

    private val _showNotes = MutableStateFlow(true)
    val showNotes: StateFlow<Boolean> = _showNotes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var reportShown = false

    init {
        // Initialize the polling order name from storage
        viewModelScope.launch {
            _pollingOrderName.value = UserUtils.getStoredPollingOrderName() ?: "Order"
        }
    }

    fun toggleReport() {
        reportShown = !reportShown
        _closedPollingAvailable.value = !_closedPollingAvailable.value
        _inProcessPollingAvailable.value = !_inProcessPollingAvailable.value
        loadCandidates()
    }

    fun toggleNotesVisibility() {
        _showNotes.value = !_showNotes.value
    }

    fun loadCandidates() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // Get the auth token and order ID
                val authToken =
                    SecureStorage.retrieve("accessToken")?.let { UserUtils.decryptData(it) }
                val orderIdString =
                    SecureStorage.retrieve("pollingOrder")
                val orderId = orderIdString?.toIntOrNull() ?: 0

                if (authToken != null && orderId > 0) {
                    if (_closedPollingAvailable.value) {
                        // Get closed polling report
                        val reportData = repository.getPollingReport(orderId, authToken)
                        if (reportData.candidates.isNotEmpty()) {
                            _closedPollingAvailable.value = true
                            _inProcessPollingAvailable.value = false
                            handleReportData(reportData)
                        } else {
                            // If no closed polling data, try in-process
                            _closedPollingAvailable.value = false
                            val inProcessData =
                                repository.getInProcessPollingReport(orderId, authToken)
                            if (inProcessData.candidates.isNotEmpty()) {
                                _inProcessPollingAvailable.value = true
                                handleReportData(inProcessData)
                            } else {
                                _inProcessPollingAvailable.value = false
                                _candidateList.value = emptyList()
                                _errorMessage.value = "No polling data available"
                            }
                        }
                    } else {
                        // Get in-process polling report
                        val reportData = repository.getInProcessPollingReport(orderId, authToken)
                        if (reportData.candidates.isNotEmpty()) {
                            _inProcessPollingAvailable.value = true
                            _closedPollingAvailable.value = false
                            handleReportData(reportData)
                        } else {
                            _inProcessPollingAvailable.value = false
                            _candidateList.value = emptyList()
                            _errorMessage.value = "No in-process polling data available"
                        }
                    }
                } else {
                    _errorMessage.value = "Authentication error. Please login again."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun handleReportData(reportData: PollingReportRepository.ReportData) {
        _candidateList.value = reportData.candidates
        _pollingTotals.value = reportData.voteTotals

        reportData.summary?.let { summary ->
            _pollingSummary.value = summary
            _pollingTitle.value = summary.pollingName
        }
    }
}