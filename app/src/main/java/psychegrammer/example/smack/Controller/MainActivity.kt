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
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import psychegrammer.example.smack.Model.Channel
import psychegrammer.example.smack.R
import psychegrammer.example.smack.Services.AuthService
import psychegrammer.example.smack.Services.MessageService
import psychegrammer.example.smack.Services.UserDataService
import psychegrammer.example.smack.Utilities.BROADCAST_USER_DATA_CHANGE
import psychegrammer.example.smack.Utilities.SOCKET_URL

class MainActivity : AppCompatActivity() {

    val socket = IO.socket(SOCKET_URL)
    lateinit var channelAdapter: ArrayAdapter<Channel>

    private fun setupAdapters() {
        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
        channel_list.adapter = channelAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        socket.connect()
        socket.on("channelCreated", onNewChannel)
        setupAdapters()

        if (App.prefs.isLoggedIn) {
            AuthService.findUserByEmail(this) {}
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver, IntentFilter(BROADCAST_USER_DATA_CHANGE))

    }

    override fun onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver, IntentFilter(BROADCAST_USER_DATA_CHANGE))
        super.onResume()

    }


    override fun onDestroy() {
        socket.disconnect()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)
        super.onDestroy()
    }

    private val userDataChangeReceiver =  object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            // what we want to happen when that broadcast has been sent out
            if (App.prefs.isLoggedIn) {
                 nav_drawer_header_include.userNameNavHeader.text = UserDataService.name
                 nav_drawer_header_include.userEmailNavHeader.text = UserDataService.email
                 val resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable", packageName)
                 nav_drawer_header_include.userImageNavHeader.setImageResource(resourceId)
                 nav_drawer_header_include.userImageNavHeader.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
                 nav_drawer_header_include.loginBtnNavHeader.text = "Logout"

                MessageService.getChannels(context) {complete ->
                    if (complete) {
                        channelAdapter.notifyDataSetChanged()
                    }
                }
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

        if (App.prefs.isLoggedIn) {
            // log out
            UserDataService.logout()
            nav_drawer_header_include.userNameNavHeader.text = ""
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
        if (App.prefs.isLoggedIn) {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)
            
            builder.setView(dialogView).setPositiveButton("Add") { dialogInterface, i ->
                // perform some logic when clicked
                val nameTextField = dialogView.findViewById<EditText>(R.id.addChannelNameTxt)
                val descTextField = dialogView.findViewById<EditText>(R.id.addChannelDescTxt)
                val channelName = nameTextField.text.toString()
                val channelDesc = descTextField.text.toString()

                // Create channel with the channel name and description
                socket.emit("newChannel", channelName, channelDesc)


            }.setNegativeButton("Cancel") { dialogInterface, i ->
                // cancel and close the dialog

            }.show()
        }
    }

    private val onNewChannel = Emitter.Listener {args ->
        runOnUiThread {
            val channelName = args[0] as String
            val channelDesc = args[1] as String
            val channelId = args[2] as String

            val newChannel = Channel(channelName, channelDesc, channelId)
            MessageService.channels.add(newChannel)
            println(newChannel.name)
            println(newChannel.description)
            println(newChannel.id)

            channelAdapter.notifyDataSetChanged()
        }
    }

    fun sendMsgBtnClicked(view: View) {

    }


    fun hideKeyboard() {
        // getting the object and casting it as InputMethodManager
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

}
