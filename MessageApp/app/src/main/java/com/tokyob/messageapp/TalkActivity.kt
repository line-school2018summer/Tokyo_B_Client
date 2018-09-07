package com.tokyob.messageapp

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.widget.Button
import android.widget.TextView

import kotlinx.android.synthetic.main.activity_talk.*
import kotlinx.android.synthetic.main.fragment_friend.*

class TalkActivity : Activity() {

    var id: Int = 0
    var userID: String = ""
    var friendID: String = ""
    var userToken: String = ""

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val received = data!!
            id = received.getIntExtra("id", -1)
            userID = received.getStringExtra("user_id")
            friendID = received.getStringExtra("friend_id")
            userToken = received.getStringExtra("token")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_talk)

        val textView1 = TextView(this)
        textView1.text = this.userID
        liner_layout.addView(textView1)
        val textView2 = TextView(this)
        textView2.text = this.friendID
        liner_layout.addView(textView2)
        val textView3 = TextView(this)
        textView3.text = this.userToken
        liner_layout.addView(textView3)


    }

}
