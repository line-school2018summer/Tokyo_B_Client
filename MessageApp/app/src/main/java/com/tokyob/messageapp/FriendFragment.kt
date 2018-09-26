package com.tokyob.messageapp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.getSystemService
import android.transition.Slide
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

import kotlinx.android.synthetic.main.fragment_friend.*
import kotlinx.android.synthetic.main.friend_popup.*
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
@JsonIgnoreProperties(ignoreUnknown = true)
data class ResponseFriendList(val error: Int, val content: FriendListOK)
@JsonIgnoreProperties(ignoreUnknown = true)
data class FriendListOK(val message: String, val friends: List<Friend>)
data class Friend(val user_id: String, val name: String, val id: Int)

data class ResponseFriendListError(val error: Int, val content: FriendListError)
data class FriendListError(val not_authenticated: Int, val invalid_verify: Int)

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
                    val objOK: ResponseFriendList = mapper.readValue(receivedJson!!)
                    val friendsList = objOK.content.friends
                    friend_layout.removeAllViews()
                    friendsList.forEach{ makeFriendButton(it) }
                } else {
                    val objNG: ResponseFriendListError = mapper.readValue(receivedJson!!)
                    if (objNG.content.not_authenticated == 1) {
                        Toast.makeText(activity, "Without Login", Toast.LENGTH_LONG).show()
                    } else if (objNG.content.invalid_verify == 1) {
                        Toast.makeText(activity, "Invalid Verification", Toast.LENGTH_LONG).show()
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

        fun makeFriendButton(friend: Friend) {
            val parentActivity = activity as? HomeActivity
            val button = Button(parentActivity)
            val text = friend.name + "\n  @" + friend.user_id
            button.text = text
            button.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            button.isAllCaps = false
            friend_layout.addView(button)
            button.setOnClickListener { makeFriendPopup(friend.name, friend.user_id) }
        }

        fun makeFriendPopup(friend_userName: String, friend_userID: String){
            //val inflater:LayoutInflater = getSystemService() as LayoutInflater
            val inflater = LayoutInflater.from(activity)
            val view = inflater.inflate(R.layout.friend_popup, null)
            val popupWindow = PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.elevation = 10.0F
            }
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                // Create a new slide animation for popup window enter transition
                val slideIn = Slide()
                slideIn.slideEdge = Gravity.TOP
                popupWindow.enterTransition = slideIn

                // Slide animation for popup window exit transition
                val slideOut = Slide()
                slideOut.slideEdge = Gravity.RIGHT
                popupWindow.exitTransition = slideOut
            }
            val text = friend_userName + "\n  @" + friend_userID
            val fn = view.findViewById<TextView>(R.id.friend_name)
            fn.text = text

            val tb = view.findViewById<Button>(R.id.talk_button)
            tb.setOnClickListener{
                //TODO move to TalkActivity
            }

            val arb = view.findViewById<Button>(R.id.add_or_remove_button)
            arb.text = "Remove from friend list"
            arb.setOnClickListener {
                try {
                    removeFriendTask(userID!!, userNumber!!, userToken!!, friend_userID).execute()
                    popupWindow.dismiss()
                } catch (e: Exception) {
                    println(e.message)
                }
            }

            TransitionManager.beginDelayedTransition(friend_parent)
            popupWindow.showAtLocation(
                    friend_parent, // Location to display popup window
                    Gravity.CENTER, // Exact position of layout to display popup
                    0, // X offset
                    0 // Y offset
            )
        }
    }

    inner class removeFriendTask(private val mUserID: String, private val mUserNumber: Int, private val mToken: String, private val mFriendID: String): AsyncTask<Void, Void, Boolean>() {
        private var receivedJson: String? = null

        override fun doInBackground(vararg params: Void): Boolean? {
            val sendJson = JSONObject()
            sendJson.put("use_id", 0)
            sendJson.put("target_id", mFriendID)
            sendJson.put("target", "/friend/remove")
            sendJson.put("authenticated", 1)
            sendJson.put("id", mUserNumber)
            sendJson.put("token", mToken)


            try {
                receivedJson = postToServer("/friend/remove", sendJson)
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
                    Toast.makeText(activity, obj.content["message"].toString(), Toast.LENGTH_LONG).show()
                    getFriendListTask(mUserNumber, mToken).execute()
                } else {
                    if (obj.content["not_authenticated"].toString().toInt() == 1) {
                        Toast.makeText(activity, "Invalid Verification", Toast.LENGTH_LONG).show()
                    } else if (obj.content["invalid_verify"].toString().toInt() == 1) {
                        Toast.makeText(activity, "Invalid Verification", Toast.LENGTH_LONG).show()
                    } else if (obj.content["unexist_id"].toString().toInt() == 1) {
                        Toast.makeText(activity, "This user does not exist", Toast.LENGTH_LONG).show()
                    } else if (obj.content["self_removing"].toString().toInt() == 1) {
                        Toast.makeText(activity, "This ID is you", Toast.LENGTH_LONG).show()
                    } else if (obj.content["already_stranger"].toString().toInt() == 1) {
                        Toast.makeText(activity, "This user has already been a stranger", Toast.LENGTH_LONG).show()
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
    }

    inner class searchFriendTask internal constructor(private val mUserNumber: Int, private val mToken: String, private val mText: String): AsyncTask<Void, Void, Boolean>() {
        private var receivedJson: String? = null

        override fun doInBackground(vararg params: Void): Boolean? {
            val sendJson = JSONObject()
            val contentJson = JSONObject()
            contentJson.put("use_id", 0)
            contentJson.put("target_user_id", mText)
            sendJson.put("content", contentJson)
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
                    makeSearchResultPopUp(obj.content["name"].toString(), obj.content["user_id"].toString())
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

        fun makeSearchResultPopUp(friend_userName: String, friend_userID: String){
            //val inflater:LayoutInflater = getSystemService() as LayoutInflater
            val inflater = LayoutInflater.from(activity)
            val view = inflater.inflate(R.layout.friend_popup, null)
            val popupWindow = PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.elevation = 10.0F
            }
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                // Create a new slide animation for popup window enter transition
                val slideIn = Slide()
                slideIn.slideEdge = Gravity.TOP
                popupWindow.enterTransition = slideIn

                // Slide animation for popup window exit transition
                val slideOut = Slide()
                slideOut.slideEdge = Gravity.RIGHT
                popupWindow.exitTransition = slideOut
            }
            val text = friend_userName + "\n  @" + friend_userID
            val fn = view.findViewById<TextView>(R.id.friend_name)
            fn.text = text

            val tb = view.findViewById<Button>(R.id.talk_button)
            tb.setOnClickListener{
                //TODO move to TalkActivity
            }

            val arb = view.findViewById<Button>(R.id.add_or_remove_button)
            arb.text = "Add to friend list"
            arb.setOnClickListener {
                try {
                    addFriendTask(userID!!, userNumber!!, userToken!!, friend_userID).execute()
                    popupWindow.dismiss()
                } catch (e: Exception) {
                    println(e.message)
                }
            }

            TransitionManager.beginDelayedTransition(friend_parent)
            popupWindow.showAtLocation(
                    friend_parent, // Location to display popup window
                    Gravity.CENTER, // Exact position of layout to display popup
                    0, // X offset
                    0 // Y offset
            )
        }
    }

    inner class addFriendTask(private val mUserID: String, private val mUserNumber: Int, private val mToken: String, private val mFriendID: String): AsyncTask<Void, Void, Boolean>() {
        private var receivedJson: String? = null

        override fun doInBackground(vararg params: Void): Boolean? {
            val sendJson = JSONObject()
            sendJson.put("use_id", 0)
            sendJson.put("target_id", mFriendID)
            sendJson.put("target", "/friend/add")
            sendJson.put("authenticated", 1)
            sendJson.put("id", mUserNumber)
            sendJson.put("token", mToken)


            try {
                receivedJson = postToServer("/friend/add", sendJson)
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
                    Toast.makeText(activity, obj.content["message"].toString(), Toast.LENGTH_LONG).show()
                    getFriendListTask(mUserNumber, mToken).execute()
                } else {
                    if (obj.content["not_authenticated"].toString().toInt() == 1) {
                        Toast.makeText(activity, "Invalid Verification", Toast.LENGTH_LONG).show()
                    } else if (obj.content["invalid_verify"].toString().toInt() == 1) {
                        Toast.makeText(activity, "Invalid Verification", Toast.LENGTH_LONG).show()
                    } else if (obj.content["unexist_id"].toString().toInt() == 1) {
                        Toast.makeText(activity, "This user does not exist", Toast.LENGTH_LONG).show()
                    } else if (obj.content["self_adding"].toString().toInt() == 1) {
                        Toast.makeText(activity, "This ID is you", Toast.LENGTH_LONG).show()
                    } else if (obj.content["already_friend"].toString().toInt() == 1) {
                        Toast.makeText(activity, "This user has already been a friend of yours", Toast.LENGTH_LONG).show()
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
    }
}