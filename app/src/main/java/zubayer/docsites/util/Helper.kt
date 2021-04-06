package zubayer.docsites.util

import android.app.Application
import android.graphics.Typeface
import android.widget.TextView
import zubayer.docsites.Constant

object Helper {

    lateinit var application: Application

    fun setTypeFace(textViews: List<TextView>) {
        Typeface.createFromAsset(application.assets, Constant.FONT).apply {
            textViews.forEach { it.setTypeface(this) }
        }
    }





}