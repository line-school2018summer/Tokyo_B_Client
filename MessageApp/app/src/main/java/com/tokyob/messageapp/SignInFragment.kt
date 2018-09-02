package com.tokyob.messageapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.android.synthetic.main.fragment_sign_in.*
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SignInFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SignInFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class SignInFragment : Fragment() {
    private var mAuthTaskSignIn: UserSignInTask? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptSignIn()
                return@OnEditorActionListener true
            }
            false
        })

        sign_in_button.setOnClickListener { attemptSignIn() }
    }

    private fun attemptSignIn() {
        if (mAuthTaskSignIn != null) {
            return
        }

        // Reset errors.
        user_id.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val userIDStr = user_id.text.toString()
        val passwordStr = password.text.toString()

        var cancel = false
        var focusView: View? = null

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
        if (TextUtils.isEmpty(userIDStr)) {
            user_id.error = getString(R.string.error_field_required)
            focusView = user_id
            cancel = true
        } else if (!isUserIDValid(userIDStr)) {
            user_id.error = "ID should have length between 3 and 13 and only have alphabet or number."
            focusView = user_id
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
            mAuthTaskSignIn = UserSignInTask(userIDStr, passwordStr)
            mAuthTaskSignIn!!.execute(null as Void?)
        }
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

        sign_in_scroll.visibility = if (show) View.GONE else View.VISIBLE
        sign_in_scroll.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        sign_in_scroll.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

        sign_in_progress.visibility = if (show) View.VISIBLE else View.GONE
        sign_in_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        sign_in_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }

    inner class UserSignInTask internal constructor(private val mUserID: String, private val mPassword: String) : AsyncTask<Void, Void, Boolean>() {
        private var receivedJson: String? = null

        override fun doInBackground(vararg params: Void): Boolean? {
            val sendJson = JSONObject()
            sendJson.put("target", "/account/login")
            sendJson.put("authenticated", 0)
            sendJson.put("user_id", mUserID)
            sendJson.put("password", mPassword)

            try {
                receivedJson = postToServer("/account/login", sendJson)
            } catch (e: InterruptedException) {
                return false
            }

            return true
        }

        override fun onPostExecute(success: Boolean?) {
            mAuthTaskSignIn = null
            showProgress(false)
            if (success!!){
                val mapper = ObjectMapper().registerKotlinModule()
                val obj:Response = mapper.readValue(receivedJson!!)

                if (obj.error == 0) {
                    val parentActivity = activity as? LoginActivity
                    parentActivity?.userID = obj.content["logged_user_id"].toString()
                    parentActivity?.userName = obj.content["logged_name"].toString()
                    parentActivity?.userNumber = obj.content["logged_id"].toString().toInt()
                    parentActivity?.userPassword = mPassword
                    parentActivity?.userToken = obj.content["token"].toString()
                    parentActivity?.sendUserInfo()
                    parentActivity?.finish()
                } else {
                    if (obj.content["authenticated"].toString().toInt() == 1) {
                        user_id.error = "This account has already logged in."
                        user_id.requestFocus()
                    } else if (obj.content["missing_id"].toString().toInt() == 1) {
                        user_id.error = "This ID is not used."
                        user_id.requestFocus()
                    } else if (obj.content["invalid_password"].toString().toInt() == 1) {
                        user_id.error = "Invalid Password"
                        user_id.requestFocus()
                    }
                }
            }
        }

        override fun onCancelled() {
            mAuthTaskSignIn = null
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