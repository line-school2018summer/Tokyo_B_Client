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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.SearchView
import android.widget.TextView
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

import kotlinx.android.synthetic.main.fragment_friend.*
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [FriendFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [FriendFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class FriendFragment : Fragment() {
    var userID: String? = null
    var userName: String? = null
    var userNumber: Int? = null
    var userPassword: String? = null
    var userToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_friend, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val parentActivity = activity as? HomeActivity
        userID = parentActivity?.userID
        userName = parentActivity?.userName
        userNumber = parentActivity?.userNumber
        userPassword = parentActivity?.userPassword
        userToken = parentActivity?.userToken
        try {
            showProgress(true)
            getFriendListTask(userNumber!!, userToken!!).execute()
        } catch (e: Exception) {
            println(e.message)
        }

        friend_search.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String?): Boolean {
                try {
                    showProgress(true)
                    searchFriendTask(userNumber!!, userToken!!, text!!).execute()
                } catch (e: Exception) {
                    println(e.message)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        friend_scroll.visibility = if (show) View.GONE else View.VISIBLE
        friend_scroll.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        friend_scroll?.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })

        friend_progress.visibility = if (show) View.VISIBLE else View.GONE
        friend_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        friend_progress?.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }

    inner class getFriendListTask internal constructor(private val mUserNumber: Int, private val mToken: String): AsyncTask<Void, Void, Boolean>() {
        private var receivedJson: String? = null

        override fun doInBackground(vararg params: Void): Boolean? {
            val sendJson = JSONObject()
            sendJson.put("target", "/friend/list")
            sendJson.put("authenticated", 1)
            sendJson.put("id", mUserNumber)
            sendJson.put("token", mToken)

            try {
                receivedJson = postToServer("/friend/list", sendJson)
            } catch (e: Exception) {
                println(e.message)
                return false
            }

            return true
        }

        override fun onPostExecute(success: Boolean?) {
            showProgress(false)
            if (success!!){
                val mapper = ObjectMapper().registerKotlinModule()
                val obj:Response = mapper.readValue(receivedJson!!)

                if (obj.error == 0) {
                    println(obj.content["friends"])
                    //TODO for makeFriendList(obj.content["friends"]), obj.content["friends"] should be transformed to Map.
                } else {
                    if (obj.content["not_authenticated"].toString().toInt() == 1) {

                    } else if (obj.content["invalid_verify"].toString().toInt() == 1) {

                    }
                }
            }
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

        fun makeFriendList(friends: Map<Int, Any>) {
            //TODO
        }
    }

    inner class searchFriendTask internal constructor(private val mUserNumber: Int, private val mToken: String, private val mText: String): AsyncTask<Void, Void, Boolean>() {
        private var receivedJson: String? = null

        override fun doInBackground(vararg params: Void): Boolean? {
            val sendJson = JSONObject()
            val contentJson = JSONObject()
            contentJson.put("use_id", 0)
            contentJson.put("target_user_id", mText)
            sendJson.put("target", "/friend/search")
            sendJson.put("authenticated", 1)
            sendJson.put("id", mUserNumber)
            sendJson.put("token", mToken)


            try {
                receivedJson = postToServer("/friend/search", sendJson)
            } catch (e: Exception) {
                println(e.message)
                return false
            }

            return true
        }

        override fun onPostExecute(success: Boolean?) {
            showProgress(false)
            if (success!!){
                val mapper = ObjectMapper().registerKotlinModule()
                val obj:Response = mapper.readValue(receivedJson!!)

                if (obj.error == 0) {
                    makeSearchResultPopUp(obj.content["user_id"].toString(), obj.content["name"].toString())
                } else {
                    if (obj.content["not_authenticated"].toString().toInt() == 1) {

                    } else if (obj.content["invalid_verify"].toString().toInt() == 1) {

                    } else if (obj.content["invalid_user_id"].toString().toInt() == 1) {

                    }
                }
            }
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

        fun makeSearchResultPopUp(userID: String, userName: String) {
            //TODO
        }
    }
}