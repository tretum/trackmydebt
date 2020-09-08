package com.mmutert.trackmydebt.util

import android.view.View
import android.view.ViewGroup

object ViewHelper {

    fun <T : View> findViewsWithType(root: View, type: Class<T>): List<T> {
        val views = ArrayList<T>()
        findViewsWithType(root, type, views)
        return views
    }

    private fun <T : View> findViewsWithType(view: View, type: Class<T>, views: MutableList<T>) {
        if (type.isInstance(view)) {
            type.cast(view)?.let { views.add(it) }
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                findViewsWithType(view.getChildAt(i), type, views)
            }
        }
    }
}