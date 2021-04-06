package zubayer.docsites

import androidx.multidex.MultiDexApplication
import zubayer.docsites.util.Helper

class DocSites: MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        Helper.application = this
    }
}