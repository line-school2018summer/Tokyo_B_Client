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

import java.util.ArrayList
import kotlinx.android.synthetic.main.fragment_register.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [RegisterFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
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
        password.error = null
        password_confirm.error = null

        // Store values at the time of the login attempt.
        val userNameStr = user_name.text.toString()
        val userIDStr = user_id.text.toString()
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
            password_confirm.error = getString(R.string.error_invalid_password)
            focusView = password_confirm
            cancel = true
        }

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

        // Check for a valid user name.
        if (TextUtils.isEmpty(userNameStr)) {
            user_name.error = getString(R.string.error_field_required)
            focusView = user_name
            cancel = true
        } else if (!isUserNameValid(userIDStr)) {
            user_name.error = getString(R.string.error_invalid_email)
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
            mAuthTaskRegister = UserRegisterTask(userNameStr, userIDStr, passwordStr, passwordConfirmStr)
            mAuthTaskRegister!!.execute(null as Void?)
        }
    }

    private fun isUserNameValid(user_name: String): Boolean {
        //TODO: Replace this with your own logic
        return true
    }

    private fun isUserIDValid(user_id: String): Boolean {
        //TODO: Replace this with your own logic
        return true
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length > 4
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
                        register_scroll.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

        register_progress.visibility = if (show) View.VISIBLE else View.GONE
        register_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        register_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }

    inner class UserRegisterTask internal constructor(private val mUserName: String, private val mUserID: String, private val mPassword: String, private val mPasswordConfirm: String) : AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void): Boolean? {
            // TODO: attempt authentication against a network service.

            return true
        }

        override fun onPostExecute(success: Boolean?) {
            mAuthTaskRegister = null
            showProgress(false)

            if (success!!) {
                activity?.finish()
            } else {
                password.error = getString(R.string.error_incorrect_password)
                password.requestFocus()
            }
        }

        override fun onCancelled() {
            mAuthTaskRegister = null
            showProgress(false)
        }
    }
}
