package com.tokyob.messageapp

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import android.os.AsyncTask
import android.os.Build
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView
import com.fasterxml.jackson.annotation.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

import java.util.ArrayList
import kotlinx.android.synthetic.main.fragment_register.*
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import com.fasterxml.jackson.module.kotlin.readValue
import org.json.JSONObject

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [RegisterFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */

public data class Response(val error: Int, val content: MutableMap<String, Any>) {
    @JsonAnySetter
    fun setCntents(key: String, value: Any) {
        this.content[key] = value
    }
}

class RegisterFragment : Fragment() {
    private var mAuthTaskRegister: UserRegisterTask? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        password_confirm.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptRegister()
                return@OnEditorActionListener true
            }
            false
        })

        register_button.setOnClickListener { attemptRegister() }
    }

    private fun attemptRegister() {
        if (mAuthTaskRegister != null) {
            return
        }

        // Reset errors.
        user_name.error = null
        user_id.error = null
        email.error = null
        password.error = null
        password_confirm.error = null

        // Store values at the time of the login attempt.
        val userNameStr = user_name.text.toString()
        val userIDStr = user_id.text.toString()
        val emailStr = email.text.toString()
        val passwordStr = password.text.toString()
        val passwordConfirmStr = password_confirm.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password confirmation, if the user entered one.
        if (TextUtils.isEmpty(passwordConfirmStr)) {
            password_confirm.error = getString(R.string.error_field_required)
            focusView = password_confirm
            cancel = true
        } else if (!TextUtils.isEmpty(passwordStr) && passwordStr!=passwordConfirmStr) {
            password_confirm.error = "This field should be equal to above."
            focusView = password_confirm
            cancel = true
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(passwordStr)) {
            password.error = getString(R.string.error_field_required)
            focusView = password
            cancel = true
        } else if (!isPasswordValid(passwordStr)) {
            password.error = "Password should have length between 5 and 13 and only have alphabet or number."
            focusView = password
            cancel = true
        }

        // Check for a valid user id.
        if (TextUtils.isEmpty(emailStr)) {
            email.error = getString(R.string.error_field_required)
            focusView = email
            cancel = true
        } else if (!isEmailValid(emailStr)) {
            email.error = getString(R.string.error_invalid_email)
            focusView = email
            cancel = true
        }

        // Check for a valid user id.
        if (TextUtils.isEmpty(userIDStr)) {
            user_id.error = getString(R.string.error_field_required)
            focusView = user_id
            cancel = true
        } else if (!isUserIDValid(userIDStr)) {
            user_id.error = "ID should have length between 3 and 13 and only have alphabet or number."
            focusView = user_id
            cancel = true
        }

        // Check for a valid user name.
        if (TextUtils.isEmpty(userNameStr)) {
            user_name.error = getString(R.string.error_field_required)
            focusView = user_name
            cancel = true
        } else if (!isUserNameValid(userIDStr)) {
            user_name.error = "User Name should have length between 0 and 32."
            focusView = user_name
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            mAuthTaskRegister = UserRegisterTask(userNameStr, userIDStr, emailStr, passwordStr, passwordConfirmStr)
            mAuthTaskRegister!!.execute(null as Void?)
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return email.contains("@")
    }

    private fun isUserNameValid(user_name: String): Boolean {
        return user_name.length in 1..31
    }

    private fun isUserIDValid(user_id: String): Boolean {
        return user_id.length in 4..12 && user_id.all { it -> it.isLetterOrDigit() }
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length in 6..12 && password.all { it -> it.isLetterOrDigit() }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        register_scroll.visibility = if (show) View.GONE else View.VISIBLE
        register_scroll.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        register_scroll?.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

        register_progress.visibility = if (show) View.VISIBLE else View.GONE
        register_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        register_progress?.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }

    inner class UserRegisterTask internal constructor(private val mUserName: String, private val mUserID: String, private val mEmail: String, private val mPassword: String, private val mPasswordConfirm: String) : AsyncTask<Void, Void, Boolean>() {
        private var receivedJson: String? = null

        override fun doInBackground(vararg params: Void): Boolean? {
            val sendJson = JSONObject()
            sendJson.put("target", "/account/register/register")
            sendJson.put("authenticated", 0)
            sendJson.put("user_id", mUserID)
            sendJson.put("name", mUserName)
            sendJson.put("email", mEmail)
            sendJson.put("password", mPassword)
            sendJson.put("password_confirm", mPasswordConfirm)

            try {
                receivedJson = postToServer("/account/register/register", sendJson)
            } catch (e: InterruptedException) {
                return false
            }

            return true
        }

        override fun onPostExecute(success: Boolean?) {
            mAuthTaskRegister = null
            showProgress(false)
            if (success!!){
                val mapper = ObjectMapper().registerKotlinModule()
                //var obj:Any? = null

                val obj:Response = mapper.readValue(receivedJson!!)

                if (obj.error == 0) {
                    val parentActivity = activity as? LoginActivity
                    parentActivity?.verifyID = obj.content["verify_id"].toString().toInt()
                    fragmentManager?.beginTransaction()?.replace(R.id.loginFrame, VerifyFragment())?.commit()
                } else {
                    if (obj.content["authenticated"].toString().toInt() == 1) {
                        user_id.error = "This account has already logged in."
                        user_id.requestFocus()
                    } else if (obj.content["exist_id"].toString().toInt() == 1) {
                        user_id.error = "This ID has been used."
                        user_id.requestFocus()
                    } else if (obj.content["bad_id"].toString().toInt() == 1) {
                        user_id.error = "ID should have length between 3 and 13 and only have alphabet or number."
                        user_id.requestFocus()
                    } else if (obj.content["bad_name"].toString().toInt() == 1) {
                        user_name.error = "User Name should have length between 0 and 32."
                        user_name.requestFocus()
                    } else if (obj.content["bad_password"].toString().toInt() == 1) {
                        password.error = "Password should have length between 5 and 13 and only have alphabet or number."
                        password.requestFocus()
                    } else if (obj.content["password_confirm_does_not_match"].toString().toInt() == 1) {
                        password_confirm.error = "This field should be equal to above."
                        password_confirm.requestFocus()
                    }
                }
            }
        }

        override fun onCancelled() {
            mAuthTaskRegister = null
            showProgress(false)
        }

        fun postToServer(urlRelative: String, json: JSONObject): String {
            val urlAbsolute = getString(R.string.server_url) + urlRelative
            val client: OkHttpClient = OkHttpClient.Builder().build()

            // post
            val postBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
            val request: Request = Request.Builder().url(urlAbsolute).post(postBody).build()
            val response = client.newCall(request).execute()

            // getResult
            val result = response.body()!!.string()
            response.close()
            return result
        }
    }
}
