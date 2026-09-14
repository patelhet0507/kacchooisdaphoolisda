package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.KaachuPhoolDatabase
import com.example.data.MatchHistoryEntity
import com.example.data.MatchHistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MatchHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MatchHistoryRepository

    init {
        val db = KaachuPhoolDatabase.getDatabase(application)
        repository = MatchHistoryRepository(db.matchHistoryDao())
    }

    val lastFiveMatches: StateFlow<List<MatchHistoryEntity>> = repository.lastFiveMatches
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
