package com.mmutert.trackmydebt.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import androidx.recyclerview.widget.LinearLayoutManager
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.databinding.FragmentHomeBinding
import com.mmutert.trackmydebt.ui.BottomSpaceDecoration
import com.mmutert.trackmydebt.ui.dialogs.AddPersonDialogFragment

class HomeFragment : Fragment(), AddPersonDialogFragment.PersonAddedListener, HomeListAdapter.ListItemClickListener {

    private lateinit var mViewModel: HomeViewModel
    private lateinit var mSharedViewModel: SharedViewModel

    private lateinit var mBinding: FragmentHomeBinding

    private lateinit var mAdapter: HomeListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        mSharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home,
            container,
            false
        )
        mBinding.apply {
            viewmodel = mViewModel
            lifecycleOwner = viewLifecycleOwner

            fabAddItem.setOnClickListener {
                AddPersonDialogFragment(this@HomeFragment).show(parentFragmentManager, "AddPerson")
            }
        }

        mAdapter = HomeListAdapter(requireContext(), mViewModel, this)

        mViewModel.persons.observe(viewLifecycleOwner, {
            mAdapter.submitList(it)
        })

        mBinding.rvDebtList.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = mAdapter
        }

        // TODO Reactivate when functionality is implemented
        // val createSwipeHelper = ItemTouchHelper(HomeListSwipeHelperCallBack(mAdapter))
        // createSwipeHelper.attachToRecyclerView(mBinding.rvDebtList)

        return mBinding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController()
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    override fun personAdded(name: String, paypalUsername: String?) {
        mViewModel.addPerson(name, paypalUsername)
    }

    override fun listItemClicked(p: Person) {
        mSharedViewModel.selectPerson(p)
        val fragmentPersonDetail =
            HomeFragmentDirections.fragmentPersonDetail(p.name)
        Navigation.findNavController(mBinding.root).navigate(fragmentPersonDetail)
    }
}