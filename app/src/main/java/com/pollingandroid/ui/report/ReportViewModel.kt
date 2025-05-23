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
    val note: String?,
    val private: Boolean,
    val memberName: String,
    val vote: String = ""
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

    // Track which report is currently being shown
    private val _showingInProcessReport = MutableStateFlow(false)
    val showingInProcessReport: StateFlow<Boolean> = _showingInProcessReport

    init {
        // Initialize the polling order name from storage
        viewModelScope.launch {
            _pollingOrderName.value = UserUtils.getStoredPollingOrderName() ?: "Order"
        }
    }

    fun toggleReport() {
        // Toggle between closed and in-process report views
        val wasInProcess = _showingInProcessReport.value

        viewModelScope.launch {
            try {
                // Get the auth token and order ID
                val authToken =
                    SecureStorage.retrieve("accessToken")?.let { UserUtils.decryptData(it) }
                val orderIdString = SecureStorage.retrieve("pollingOrder")
                val orderId = orderIdString?.toIntOrNull() ?: 0

                if (authToken != null && orderId > 0) {
                    if (wasInProcess) {
                        // Switch to closed polling
                        val reportData = repository.getPollingReport(orderId, authToken)
                        if (reportData.candidates.isNotEmpty()) {
                            _showingInProcessReport.value = false
                            handleReportData(reportData)
                        }
                    } else {
                        // Switch to in-process polling
                        val reportData = repository.getInProcessPollingReport(orderId, authToken)
                        if (reportData.candidates.isNotEmpty()) {
                            _showingInProcessReport.value = true
                            handleReportData(reportData)
                        }
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to toggle report: ${e.message}"
            }
        }
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
                    // First check if an in-process polling is available
                    val inProcessData = repository.getInProcessPollingReport(orderId, authToken)
                    val hasInProcessData = inProcessData.candidates.isNotEmpty()
                    _inProcessPollingAvailable.value = hasInProcessData

                    // Then check if a closed polling is available
                    val closedReportData = repository.getPollingReport(orderId, authToken)
                    val hasClosedData = closedReportData.candidates.isNotEmpty()
                    _closedPollingAvailable.value = hasClosedData

                    // Determine which report to show initially
                    if (_closedPollingAvailable.value) {
                        // Default to showing closed polling first if available
                        handleReportData(closedReportData)
                        _showingInProcessReport.value = false
                    } else if (_inProcessPollingAvailable.value) {
                        // Otherwise show in-process polling if available
                        handleReportData(inProcessData)
                        _showingInProcessReport.value = true
                    } else {
                        _candidateList.value = emptyList()
                        _errorMessage.value = "No polling data available"
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
