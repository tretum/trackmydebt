package com.mmutert.trackmydebt.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mmutert.trackmydebt.R

class RecentActivityFragment : Fragment() {

    private lateinit var recentActivityViewModel: RecentActivityViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        recentActivityViewModel =
                ViewModelProvider(this).get(RecentActivityViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_recent_activity, container, false)
        val textView: TextView = root.findViewById(R.id.text_dashboard)
        recentActivityViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}