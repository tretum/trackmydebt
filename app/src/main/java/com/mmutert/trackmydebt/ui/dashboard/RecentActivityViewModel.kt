package com.mmutert.trackmydebt.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.mmutert.trackmydebt.data.AppDatabase
import com.mmutert.trackmydebt.data.AppRepository
import com.mmutert.trackmydebt.data.Transaction

class RecentActivityViewModel(application: Application) : AndroidViewModel(application) {

    val repository = AppRepository(AppDatabase.getDatabase(application).dao())

    val transactions : LiveData<List<Transaction>> = repository.transactions
}