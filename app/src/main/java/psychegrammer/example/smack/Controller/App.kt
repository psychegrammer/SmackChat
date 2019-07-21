package psychegrammer.example.smack.Controller

import android.app.Application
import psychegrammer.example.smack.Utilities.SharedPrefs

// App that is created before anything else
class App : Application() {
// we create App class so that we can access the data globally (everywhere)
    companion object {
        lateinit var prefs: SharedPrefs // only one instance of this class
    }

    override fun onCreate() {
        prefs = SharedPrefs(applicationContext)
        super.onCreate()
    }

}