package com.tokyob.messageapp

import android.os.Bundle
import android.app.Activity

import kotlinx.android.synthetic.main.activity_change_password.*


class ChangePassWordActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        button.setOnClickListener{
            finish()
        }

    }

}
