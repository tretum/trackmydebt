package com.mmutert.trackmydebt.ui.home

import android.graphics.Canvas
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.data.Person
import com.mmutert.trackmydebt.databinding.FragmentHomeBinding
import com.mmutert.trackmydebt.ui.BottomSpaceDecoration
import com.mmutert.trackmydebt.ui.dialogs.AddPersonDialogFragment
import com.mmutert.trackmydebt.util.FormatHelper

class HomeFragment : Fragment(), AddPersonDialogFragment.PersonAddedListener {

    private lateinit var mViewModel: HomeViewModel
    lateinit var mSharedViewModel: SharedViewModel
        private set
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

        mViewModel.balance.observe(viewLifecycleOwner) {
            if (it !== null) {
                mBinding.tvOverallBalanceValue.text = FormatHelper.printAsCurrency(it)
            }
        }

        mAdapter =
            HomeListAdapter(
                requireContext(),
                mViewModel,
                object : HomeListAdapter.ListItemClickListener {
                    override fun listItemClicked(p: Person) {
                        mSharedViewModel.selectPerson(p)
                        val fragmentPersonDetail =
                            HomeFragmentDirections.fragmentPersonDetail(p.name)
                        Navigation.findNavController(mBinding.root).navigate(fragmentPersonDetail)
                    }
                })

        mViewModel.persons.observe(viewLifecycleOwner, {
            mAdapter.submitList(it)
        })

        mBinding.rvDebtList.apply {
            addItemDecoration(BottomSpaceDecoration(200))
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = mAdapter
        }

        val createSwipeHelper = createSwipeHelper()
        createSwipeHelper.attachToRecyclerView(mBinding.rvDebtList)

        mBinding.fabAddItem.setOnClickListener {
            AddPersonDialogFragment(this).show(parentFragmentManager, "AddPerson")
        }

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

    /**
     * Creates the ItemTouchHelper that archives items in the item list on swipe to the right.
     *
     * @return The item touch helper
     */
    private fun createSwipeHelper(): ItemTouchHelper {
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                val partner = mAdapter.currentList[pos].person

                if (direction == ItemTouchHelper.RIGHT) {
                    // TODO Delete transaction on swipe
                } else if (direction == ItemTouchHelper.LEFT) {
                    // TODO Show transaction editor on swipe
                }
                mAdapter.notifyItemChanged(pos)
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                if (viewHolder != null) {
                    val binding = (viewHolder as HomeListAdapter.HomeListViewHolder).binding
                    val foregroundView: View = binding.listItemForeground
                    getDefaultUIUtil().onSelected(foregroundView)
                }
            }

            override fun onChildDrawOver(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {

                val binding = (viewHolder as HomeListAdapter.HomeListViewHolder).binding
                val giveMoneyBackground: View = binding.listItemGiveMoneyBackground
                val receiveMoneyBackground: View = binding.listItemReceiveMoneyBackground
                when {
                    dX < 0 -> {
                        giveMoneyBackground.visibility = View.INVISIBLE
                        receiveMoneyBackground.visibility = View.VISIBLE
                    }
                    dX > 0 -> {
                        giveMoneyBackground.visibility = View.VISIBLE
                        receiveMoneyBackground.visibility = View.INVISIBLE
                    }
                    else -> {
                        giveMoneyBackground.visibility = View.INVISIBLE
                        receiveMoneyBackground.visibility = View.INVISIBLE
                    }
                }
                val foregroundView: View = binding.listItemForeground
                getDefaultUIUtil().onDrawOver(
                    c,
                    recyclerView,
                    foregroundView,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                val binding = (viewHolder as HomeListAdapter.HomeListViewHolder).binding
                val foregroundView: View = binding.listItemForeground
                getDefaultUIUtil().clearView(foregroundView)
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
                val binding = (viewHolder as HomeListAdapter.HomeListViewHolder).binding
                val foregroundView: View = binding.listItemForeground
                getDefaultUIUtil().onDraw(
                    c,
                    recyclerView,
                    foregroundView,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        })
    }

    override fun personAdded(name: String, paypalUsername: String?) {
        mViewModel.addPerson(name, paypalUsername)
    }
}