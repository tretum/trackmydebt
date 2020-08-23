package com.mmutert.trackmydebt.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mmutert.trackmydebt.data.AppDatabase
import com.mmutert.trackmydebt.data.AppRepository
import com.mmutert.trackmydebt.data.Transaction
import kotlinx.coroutines.launch
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import kotlin.random.Random

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository =
        AppRepository(AppDatabase.getDatabase(application).dao())

    fun addDemoTransaction() {
        val amount = Random.nextLong()
        viewModelScope.launch {
            repository.addTransaction(
                Transaction(
                    0,
                    1,
                    true,
                    amount,
                    LocalDateTime.now(DateTimeZone.getDefault()),
                    "Some Reason $amount"
                )
            )
        }
    }
}