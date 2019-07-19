package psychegrammer.example.smack.Controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import psychegrammer.example.smack.R
import psychegrammer.example.smack.Services.AuthService
import psychegrammer.example.smack.Services.UserDataService
import psychegrammer.example.smack.Utilities.BROADCAST_USER_DATA_CHANGE

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver, IntentFilter(  BROADCAST_USER_DATA_CHANGE))

    }

    private val userDataChangeReceiver =  object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // what we want to happen when that broadcast has been sent out
            if (AuthService.isLoggedIn) {
                Log.d("qwe", UserDataService.name)
                Log.d("qwe", UserDataService.email)
                Log.d("qwe", UserDataService.avatarName)
                Log.d("qwe", "Made it inside the broadcast bro")

                Log.d("qwe", "Wrap")

                 nav_drawer_header_include.userNameNavHeader.text = UserDataService.name
                 nav_drawer_header_include.userEmailNavHeader.text = UserDataService.email
                val resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable", packageName)
                 nav_drawer_header_include.userImageNavHeader.setImageResource(resourceId)
                 nav_drawer_header_include.userImageNavHeader.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
                 nav_drawer_header_include.loginBtnNavHeader.text = "Logout"
            }
        }
    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun loginBtnNavClicked(view: View) {

        if (AuthService.isLoggedIn) {
            // log out
            UserDataService.logout()
            nav_drawer_header_include.userNameNavHeader.text = "Login"
            nav_drawer_header_include.userEmailNavHeader.text = ""
            nav_drawer_header_include.userImageNavHeader.setImageResource(R.drawable.profiledefault)
            nav_drawer_header_include.userImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
            nav_drawer_header_include.loginBtnNavHeader.text = "Login"

        } else {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }

    }

    fun addChannelClicked(view: View) {

    }

    fun sendMsgBtnClicked(view: View) {

    }

}
