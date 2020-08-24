package com.mmutert.trackmydebt.ui.persondetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mmutert.trackmydebt.R
import com.mmutert.trackmydebt.databinding.FragmentPersonDetailBinding
import com.mmutert.trackmydebt.ui.home.SharedViewModel

class PersonDetailFragment : Fragment() {

    private var columnCount = 1
    private lateinit var mBinding: FragmentPersonDetailBinding
    private lateinit var mViewModel: PersonDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_person_detail, container, false)

        mViewModel = ViewModelProvider(this).get(PersonDetailViewModel::class.java)
        val sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        sharedViewModel.selectedPerson.observe(viewLifecycleOwner) {
            person -> mViewModel.selectPerson(person)
        }


        // Set the adapter
        with(mBinding.list) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = PersonDetailAdapter()

            mViewModel.transactions.observe(
                viewLifecycleOwner,
                { (this.adapter as PersonDetailAdapter).transactions = it })
        }
        return mBinding.root
    }

    companion object {

        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            PersonDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}