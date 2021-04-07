package zubayer.docsites.util

import android.content.Context


interface HomeAdapterClickListener {
    fun onClick(context: Context, title: String)
}