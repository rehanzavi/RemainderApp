package com.rpzsoftware.remindly.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rpzsoftware.remindly.data.AppDatabase
import com.rpzsoftware.remindly.data.NoteRepository
import com.rpzsoftware.remindly.data.UserPreferences
import com.rpzsoftware.remindly.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository
    private val userPreferences = UserPreferences(application)
    
    val allNotes: Flow<List<Note>>
    val userName: StateFlow<String?> = userPreferences.userName.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    init {
        val noteDao = AppDatabase.getDatabase(application).noteDao()
        repository = NoteRepository(noteDao)
        allNotes = repository.allNotes
    }

    fun saveUserName(name: String) = viewModelScope.launch {
        userPreferences.saveUserName(name)
    }

    fun insert(note: Note) = viewModelScope.launch {
        repository.insert(note)
    }

    fun update(note: Note) = viewModelScope.launch {
        repository.update(note)
    }

    fun delete(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }
}
