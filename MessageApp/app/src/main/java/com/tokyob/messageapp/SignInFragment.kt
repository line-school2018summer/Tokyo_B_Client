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
import kotlinx.android.synthetic.main.fragment_sign_in.*


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
            password.error = getString(R.string.error_invalid_password)
            focusView = password
            cancel = true
        }

        // Check for a valid user id.
        if (TextUtils.isEmpty(userIDStr)) {
            user_id.error = getString(R.string.error_field_required)
            focusView = user_id
            cancel = true
        } else if (!isUserIDValid(userIDStr)) {
            user_id.error = getString(R.string.error_invalid_email)
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

        override fun doInBackground(vararg params: Void): Boolean? {
            // TODO: attempt authentication against a network service.

            return true
        }

        override fun onPostExecute(success: Boolean?) {
            mAuthTaskSignIn = null
            showProgress(false)

            if (success!!) {
                activity?.finish()
            } else {
                password.error = getString(R.string.error_incorrect_password)
                password.requestFocus()
            }
        }

        override fun onCancelled() {
            mAuthTaskSignIn = null
            showProgress(false)
        }
    }
}