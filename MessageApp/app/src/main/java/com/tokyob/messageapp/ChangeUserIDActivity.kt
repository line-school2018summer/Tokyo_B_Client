package com.tokyob.messageapp

import android.os.Bundle
import android.app.Activity

import kotlinx.android.synthetic.main.activity_change_user_id.*

class ChangeUserIDActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_user_id)

        button.setOnClickListener{
            finish()
        }
    }

}