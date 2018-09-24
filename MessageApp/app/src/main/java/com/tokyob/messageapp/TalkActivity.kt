package com.tokyob.messageapp

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView

import kotlinx.android.synthetic.main.activity_talk.*

class TalkActivity : AppCompatActivity() {

    var id: Int = 0
    var userID: String = ""
    var groupName: String = ""
    var userToken: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_talk)

        val homeactivity = intent

        id = homeactivity.getIntExtra("id", -1)
        userID = homeactivity.getStringExtra("user_id")
        groupName = homeactivity.getStringExtra("group_name")
        userToken = homeactivity.getStringExtra("token")

        supportActionBar?.title = groupName

        val textView1 = TextView(this)
        textView1.text = this.userID
        talk_layout.addView(textView1)
        val textView2 = TextView(this)
        textView2.text = this.groupName
        talk_layout.addView(textView2)
        val textView3 = TextView(this)
        textView3.text = this.userToken
        talk_layout.addView(textView3)


    }

}