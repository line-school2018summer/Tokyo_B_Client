package com.tokyob.messageapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.app.LoaderManager.LoaderCallbacks
import android.content.CursorLoader
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView

import java.util.ArrayList
import android.Manifest.permission.READ_CONTACTS
import android.app.Activity
import android.content.Intent
import android.os.PersistableBundle

import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), SelectMenuFragment.OnSelectMenuFragmentListener {
    public var verifyID: Int = -1
    public var userID: String? = null
    public var userName: String? = null
    public var userNumber: Int? = null
    public var userPassword: String? = null
    public var userToken: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.title = "LIME"
        supportFragmentManager.beginTransaction().replace(R.id.loginFrame, SelectMenuFragment()).commit()
    }

    override fun onLoginSelected() {
        supportFragmentManager.beginTransaction().replace(R.id.loginFrame, SignInFragment()).commit()
    }

    override fun onRegisterSelected() {
        supportFragmentManager.beginTransaction().replace(R.id.loginFrame, RegisterFragment()).commit()
    }

    public fun sendUserInfo(){
        val intent = Intent()
        intent.putExtra("user_number", userNumber)
        intent.putExtra("user_name", userName)
        intent.putExtra("user_id", userID)
        intent.putExtra("user_password", userPassword)
        intent.putExtra("user_token", userToken)

        setResult(Activity.RESULT_OK, intent)
    }
}
