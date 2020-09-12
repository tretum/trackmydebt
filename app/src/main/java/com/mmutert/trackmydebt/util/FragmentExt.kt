package com.mmutert.trackmydebt.util

import androidx.fragment.app.Fragment
import com.mmutert.trackmydebt.ViewModelFactory
import com.mmutert.trackmydebt.data.AppDatabase
import com.mmutert.trackmydebt.data.AppRepository

fun Fragment.getViewModelFactory(): ViewModelFactory {
    val repository = AppRepository(AppDatabase.getDatabase(requireContext().applicationContext).dao())
    return ViewModelFactory(repository, this)
}
