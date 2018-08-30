package com.tokyob.messageapp


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_home.*

const val MY_REQUEST_CODE = 0

class HomeActivity : AppCompatActivity() {

    var userID: String? = null
    var userName: String? = null
    var userNumber: Int? = null
    var userPassword: String? = null
    var userToken: String? = null

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                supportFragmentManager.beginTransaction().replace(R.id.frameLayout, FriendFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                supportFragmentManager.beginTransaction().replace(R.id.frameLayout, TalkFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                supportFragmentManager.beginTransaction().replace(R.id.frameLayout, SettingFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val intent = Intent(applicationContext, LoginActivity::class.java)
        startActivityForResult(intent, MY_REQUEST_CODE)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, FriendFragment()).commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val received = data!!
            userID = received.getStringExtra("user_id")
            userName = received.getStringExtra("user_name")
            userNumber = received.getIntExtra("user_number", -1)
            userPassword = received.getStringExtra("user_password")
            userToken = received.getStringExtra("user_token")
        }
    }
}
