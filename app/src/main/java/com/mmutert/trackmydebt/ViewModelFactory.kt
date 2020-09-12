package com.mmutert.trackmydebt

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.mmutert.trackmydebt.data.AppRepository
import com.mmutert.trackmydebt.ui.home.HomeViewModel
import com.mmutert.trackmydebt.ui.persondetail.PersonDetailViewModel
import com.mmutert.trackmydebt.ui.transaction.AddTransactionViewModel

/**
 * Factory for all ViewModels.
 */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(
        private val tasksRepository: AppRepository,
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
    ) = with(modelClass) {
        when {
            isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(handle, tasksRepository)
            isAssignableFrom(PersonDetailViewModel::class.java) ->
                PersonDetailViewModel(tasksRepository)
            isAssignableFrom(AddTransactionViewModel::class.java) ->
                AddTransactionViewModel(tasksRepository)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}
