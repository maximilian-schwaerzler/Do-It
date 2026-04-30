package at.co.schwaerzler.maximilian.doit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import at.co.schwaerzler.maximilian.doit.data.db.TodoDatabase

class HomeViewModel(
    private val db: TodoDatabase
) : ViewModel() {
    val todos = db.todoDao().getAll()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val db = (this[APPLICATION_KEY] as DoItApplication).database
                HomeViewModel(
                    db = db
                )
            }
        }
    }
}