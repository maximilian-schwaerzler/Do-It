package at.co.schwaerzler.maximilian.doit

import android.app.Application
import at.co.schwaerzler.maximilian.doit.data.db.TodoDatabase

class DoItApplication : Application() {
    val database: TodoDatabase by lazy {
        TodoDatabase.getDatabase(this)
    }
}