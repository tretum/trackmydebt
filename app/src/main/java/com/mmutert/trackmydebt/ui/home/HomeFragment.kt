package com.mmutert.trackmydebt.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mmutert.trackmydebt.EventObserver
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.databinding.FragmentHomeBinding
import com.mmutert.trackmydebt.ui.dialogs.AddPersonDialogFragment
import com.mmutert.trackmydebt.util.getViewModelFactory
import com.mmutert.trackmydebt.util.setupSnackbar

const val PERSON_DELETED_OK = 1

class HomeFragment : Fragment(), AddPersonDialogFragment.PersonAddedListener {

    private val viewModel: HomeViewModel by viewModels { getViewModelFactory() }

    private lateinit var mBinding: FragmentHomeBinding

    private val args: HomeFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentHomeBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
            lifecycleOwner = viewLifecycleOwner
        }

        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setupFAB()
        setupRecyclerView()
        setupNavigation()
        setupSnackbar()
    }

    private fun setupRecyclerView() {
        val adapter = HomeListAdapter(requireContext(), viewModel)
        viewModel.persons.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        mBinding.rvDebtList.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            this.adapter = adapter
        }

        // TODO Reactivate when functionality is implemented
        // val createSwipeHelper = ItemTouchHelper(HomeListSwipeHelperCallBack(mAdapter))
        // createSwipeHelper.attachToRecyclerView(mBinding.rvDebtList)
    }

    private fun setupSnackbar() {
        view?.setupSnackbar(this, viewModel.snackbarTextId, Snackbar.LENGTH_SHORT)
        arguments?.let {
            viewModel.showEditResultMessage(args.userMessage)
        }
    }

    private fun setupFAB() {
        mBinding.fabAddItem.setOnClickListener {
            AddPersonDialogFragment(this@HomeFragment).show(parentFragmentManager, "AddPerson")
        }
    }

    private fun setupNavigation() {
        viewModel.personClicked.observe(viewLifecycleOwner, EventObserver {
            val action =
                HomeFragmentDirections.fragmentPersonDetail(title = it.name, personId = it.id)
            findNavController().navigate(action)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_filter_persons){
            showFilteringPopUpMenu()
            return true
        }

        return item.onNavDestinationSelected(findNavController()) || super.onOptionsItemSelected(
            item
        )
    }

    private fun showFilteringPopUpMenu() {
        val view = activity?.findViewById<View>(R.id.action_filter_persons) ?: return
        PopupMenu(requireContext(), view).run {
            menuInflater.inflate(R.menu.filter_persons, menu)

            setOnMenuItemClickListener {
                viewModel.setFilterMode(
                    when (it.itemId) {
                        R.id.action_filter_credit -> FilterType.FILTER_CREDIT_ONLY
                        R.id.action_filter_debt -> FilterType.FILTER_DEBT_ONLY
                        else -> FilterType.FILTER_ALL
                    }
                )
                true
            }
            show()
        }
    }

    override fun personAdded(person: Person) {
        viewModel.addPerson(person)
    }
}