package com.mmutert.trackmydebt.util

import android.content.Intent
import android.net.Uri
import com.mmutert.trackmydebt.R

object IntentHelper {


    fun createBrowserIntent(url: String) : Intent {
        val uri = Uri.parse(url)
        return Intent(Intent.ACTION_VIEW, uri)
    }
}