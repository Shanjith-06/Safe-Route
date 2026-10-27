package com.example.saferoute.ui.hazards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saferoute.domain.model.HazardReport
import com.example.saferoute.domain.repository.HazardsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HazardsViewModel(
    private val repository: HazardsRepository
) : ViewModel() {

    var searchQuery = MutableStateFlow("")
    var selectedCategory = MutableStateFlow("ALL")

    val allHazards: StateFlow<List<HazardReport>> = repository.getAllHazards()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredHazards: StateFlow<List<HazardReport>> = combine(
        allHazards,
        searchQuery,
        selectedCategory
    ) { hazards, query, category ->
        hazards.filter { h ->
            val matchesQuery = query.isEmpty() ||
                    h.title.contains(query, ignoreCase = true) ||
                    h.description.contains(query, ignoreCase = true)

            val matchesCategory = category == "ALL" || h.category.equals(category, ignoreCase = true)

            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun reportHazard(
        title: String,
        category: String,
        description: String,
        severity: Int,
        lat: Double = 37.7749,
        lng: Double = -122.4194
    ) {
        viewModelScope.launch {
            val report = HazardReport(
                title = title,
                category = category,
                description = description,
                severity = severity,
                latitude = lat,
                longitude = lng,
                timestamp = System.currentTimeMillis(),
                isSynced = false,
                status = "ACTIVE"
            )
            repository.reportHazard(report)
        }
    }

    fun deleteHazard(id: Long) {
        viewModelScope.launch {
            repository.deleteHazard(id)
        }
    }
}
