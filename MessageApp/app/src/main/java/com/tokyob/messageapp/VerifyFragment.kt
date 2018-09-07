package com.tokyob.messageapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.AsyncTask
import android.os.Build
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.android.synthetic.main.fragment_verify.*
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class VerifyFragment : android.support.v4.app.Fragment() {
    private var mAuthTaskVerify: UserVerifyTask? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_verify, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        code.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptVerify()
                return@OnEditorActionListener true
            }
            false
        })

        verify_button.setOnClickListener { attemptVerify() }
    }

    private fun attemptVerify() {
        if (mAuthTaskVerify != null) {
            return
        }

        // Reset errors.
        code.error = null

        // Store values at the time of the login attempt.
        val codeStr = code.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid user name.
        if (TextUtils.isEmpty(codeStr)) {
            code.error = getString(R.string.error_field_required)
            focusView = code
            cancel = true
        } else if (!isCodeValid(codeStr)) {
            code.error = "This Code is invalid"
            focusView = code
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
            mAuthTaskVerify = UserVerifyTask(codeStr)
            mAuthTaskVerify!!.execute(null as Void?)
        }
    }

    private fun isCodeValid(code: String): Boolean {
        return code.length == 4
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

        verify_scroll.visibility = if (show) View.GONE else View.VISIBLE
        verify_scroll.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        verify_scroll.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

        verify_progress.visibility = if (show) View.VISIBLE else View.GONE
        verify_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        verify_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }

    inner class UserVerifyTask internal constructor(private val mCode: String) : AsyncTask<Void, Void, Boolean>() {
        private val parentActivity = activity as? LoginActivity
        private val mVerifyID = parentActivity?.verifyID
        private var receivedJson: String? = null

        override fun doInBackground(vararg params: Void): Boolean? {
            val sendJson = JSONObject()
            sendJson.put("target", "/account/register/verify")
            sendJson.put("authenticated", 0)
            sendJson.put("verify_id", mVerifyID)
            sendJson.put("code", mCode)

            try {
                receivedJson = postToServer("/account/register/verify", sendJson)
            } catch (e: Exception) {
                println(e.message)
                return false
            }

            return true
        }

        override fun onPostExecute(success: Boolean?) {
            mAuthTaskVerify = null
            showProgress(false)
            if (success!!) {
                val mapper = ObjectMapper().registerKotlinModule()
                val obj: Response = mapper.readValue(receivedJson!!)

                if (obj.error == 0) {
                    parentActivity?.userID = obj.content["logged_user_id"].toString()
                    parentActivity?.userName = obj.content["logged_name"].toString()
                    parentActivity?.userNumber = obj.content["logged_id"].toString().toInt()
                    parentActivity?.userToken = obj.content["token"].toString()
                    parentActivity?.sendUserInfo()
                    parentActivity?.finish()
                } else {
                    if (obj.content["authenticated"].toString().toInt() == 1) {
                        code.error = "This account has already logged in."
                        code.requestFocus()
                    } else if (obj.content["invalid_verify_id"].toString().toInt() == 1) {
                        code.error = "Invalid Verify ID"
                        code.requestFocus()
                    } else if (obj.content["invalid_code"].toString().toInt() == 1) {
                        code.error = "Invalid Code"
                        code.requestFocus()
                    }
                }
            }
        }

        override fun onCancelled() {
            mAuthTaskVerify = null
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