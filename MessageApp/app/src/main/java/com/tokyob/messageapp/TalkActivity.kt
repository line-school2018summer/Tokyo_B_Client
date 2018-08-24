package com.tokyob.messageapp

import android.os.Bundle
import android.app.Activity

import kotlinx.android.synthetic.main.activity_talk.*

class TalkActivity : Activity() {
    private lateinit var id: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        id = intent.getStringExtra("user_id")

    }

}
