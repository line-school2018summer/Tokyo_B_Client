package com.tokyob.messageapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.support.transition.TransitionManager
import android.support.v7.app.AppCompatActivity
import android.transition.Slide
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

import kotlinx.android.synthetic.main.activity_talk.*
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

data class sendTalkNG(val error: Int, val content: sendTalkNGContent)
data class sendTalkNGContent(val not_authenticated: Int, val invalid_verify: Int, val not_join: Int, val invalid_talk_id: Int, val too_long_text: Int, val  meaningless_text: Int)

data class getTalkNG(val error: Int, val content: getTalkNGContent)
data class getTalkNGContent(val not_authenticated: Int, val invalid_verify: Int)

data class getTalkOK(val error: Int, val content: getTalkOKContent)
data class getTalkOKContent(val talk: List<talkContent>)
data class talkContent(val talk_id: Int, val name: String, val content: talkContent2)
data class talkContent2(val new: List<oneMessage>)
data class oneMessage(val sent_user_id: String, val sent_user_name: String, val content_type: Int, val content_content: String, val content_id: Int)

data class inviteMemberNG(val error: Int, val content: inviteMemberNGContent)
data class inviteMemberNGContent(val not_authenticated: Int, val invalid_verify: Int, val invalid_user_id: Int, val invalid_talk_id: Int, val user_not_joined: Int, val  already_joined: Int)

class TalkActivity : AppCompatActivity() {

    var id: Int = 0
    var userNumber: Int = 0
    var userName: String = ""
    var groupName: String = ""
    var talkID: Int = 0
    var userToken: String = ""
    var latest_content_id = 0

    val getTalkHandler = Handler()
    lateinit var getTalkRunnable: Runnable

    fun postHtml(relativeUrl: String, json: JSONObject): String {

        val url = getString(R.string.server_url) + relativeUrl
        val client: OkHttpClient = OkHttpClient.Builder().build()

        // post
        val postBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        val request: Request = Request.Builder().url(url).post(postBody).build()
        val response = client.newCall(request).execute()

        // getResult
        val result: String = response.body()!!.string()
        response.close()
        return result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_talk)

        val homeactivity = intent

        id = homeactivity.getIntExtra("id", -1)
        userNumber = homeactivity.getIntExtra("id", -1)
        userName = homeactivity.getStringExtra("user_name")
        groupName = homeactivity.getStringExtra("group_name")
        talkID = homeactivity.getIntExtra("group_id", -1)
        println(talkID)
        userToken = homeactivity.getStringExtra("token")

        supportActionBar?.title = groupName

        getTalkRunnable = object : Runnable {
            override fun run() {
                getTalkTask(talkID).execute()
                getTalkHandler.postDelayed(this, 1000)
            }
        }

        talk_send_button.setOnClickListener {
            val message = message_text.text.toString()
            sendTalkTask(message).execute()
        }
        invite_button.setOnClickListener {
            getTalkHandler.removeCallbacks(getTalkRunnable)
            val memberID = invite_member_search.text.toString()
            memberAddTask(memberID).execute()
            getTalkHandler.post(getTalkRunnable)
        }
        getTalkHandler.post(getTalkRunnable)
    }

    override fun onStop() {
        super.onStop()
        getTalkHandler.removeCallbacks(getTalkRunnable)
    }

    inner class memberAddTask internal constructor(private val mMemberID: String): AsyncTask<Void, Void, Boolean>() {
        private var receivedJson: String = ""

        override fun doInBackground(vararg p0: Void?): Boolean {
            val sendJson = JSONObject()
            val contentJson = JSONObject()
            contentJson.put("target_group", talkID)
            contentJson.put("use_id", 0)
            contentJson.put("target_user_id", mMemberID)
            sendJson.put("target", "/chat/join/other")
            sendJson.put("authenticated", 1)
            sendJson.put("id", userNumber)
            sendJson.put("token", userToken)
            sendJson.put("content", contentJson)

            try {
                receivedJson = postHtml("/chat/join/other", sendJson)
            } catch (e: Exception) {
                println(e.message)
                return false
            }
            return true
        }

        override fun onPostExecute(success: Boolean) {
            super.onPostExecute(success)

            if (success){
                val mapper = ObjectMapper().registerKotlinModule()
                val obj:Response = mapper.readValue(receivedJson)
                if (obj.error == 1) {
                    val objNG: inviteMemberNG = mapper.readValue(receivedJson)
                    if (objNG.content.not_authenticated == 1) {
                        Toast.makeText(applicationContext, "Without Login", Toast.LENGTH_LONG).show()
                    }
                    else if (objNG.content.invalid_verify == 1) {
                        Toast.makeText(applicationContext, "Invalid Verification", Toast.LENGTH_LONG).show()
                    }
                    else if (objNG.content.invalid_user_id == 1) {
                        Toast.makeText(applicationContext, "There is not such user ID.", Toast.LENGTH_LONG).show()
                    }
                    else if (objNG.content.invalid_talk_id == 1) {
                        Toast.makeText(applicationContext, "This ID is meaning less", Toast.LENGTH_LONG).show()
                    }
                    else if (objNG.content.user_not_joined == 1) {
                        Toast.makeText(applicationContext, "You don't join this talk", Toast.LENGTH_LONG).show()
                    }
                    else if (objNG.content.already_joined == 1) {
                        Toast.makeText(applicationContext, "This user has already join this talk.", Toast.LENGTH_LONG).show()
                    }
                }
                else{
                    invite_member_search.text.clear()
                    Toast.makeText(applicationContext, "Invitation Success", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    inner class sendTalkTask internal constructor(private val mMessage: String): AsyncTask<Void, Void, Boolean>() {
        private var receivedJson: String = ""

        override fun doInBackground(vararg p0: Void?): Boolean {
            val sendJson = JSONObject()
            val contentJson = JSONObject()
            contentJson.put("talk_id", talkID)
            contentJson.put("type", 1)
            contentJson.put("content", mMessage)
            sendJson.put("target", "/chat/send")
            sendJson.put("authenticated", 1)
            sendJson.put("id", userNumber)
            sendJson.put("token", userToken)
            sendJson.put("content", contentJson)

            try {
                receivedJson = postHtml("/chat/send", sendJson)
            } catch (e: Exception) {
                println(e.message)
                return false
            }
            return true
        }

        override fun onPostExecute(success: Boolean) {
            super.onPostExecute(success)

            if (success){
                val mapper = ObjectMapper().registerKotlinModule()
                val obj:Response = mapper.readValue(receivedJson)
                if (obj.error == 1) {
                    val objNG: sendTalkNG = mapper.readValue(receivedJson)
                    if (objNG.content.not_authenticated == 1) {
                        Toast.makeText(applicationContext, "Without Login", Toast.LENGTH_LONG).show()
                    }
                    else if (objNG.content.invalid_verify == 1) {
                        Toast.makeText(applicationContext, "Invalid Verification", Toast.LENGTH_LONG).show()
                    }
                    else if (objNG.content.not_join == 1) {
                        Toast.makeText(applicationContext, "You do not join this group now.", Toast.LENGTH_LONG).show()
                    }
                    else if (objNG.content.too_long_text == 1) {
                        Toast.makeText(applicationContext, "This Message is Too Long", Toast.LENGTH_LONG).show()
                    }
                    else if (objNG.content.meaningless_text == 1) {
                        Toast.makeText(applicationContext, "This Message has no mean", Toast.LENGTH_LONG).show()
                    }
                }
                else {
                    message_text.text.clear()
                }
            }
        }
    }

    inner class getTalkTask internal constructor(private val mTalkID: Int): AsyncTask<Void, Void, Boolean>() {
        private var receivedJson: String = ""

        override fun doInBackground(vararg p0: Void?): Boolean {
            val sendJson = JSONObject()
            val contentJson = JSONObject()
            val talkhisJson = JSONObject()
            talkhisJson.put(mTalkID.toString(), latest_content_id)
            contentJson.put("talk_all_need", 0)
            contentJson.put("talk_his", talkhisJson)
            sendJson.put("target", "/chat/get")
            sendJson.put("authenticated", 1)
            sendJson.put("id", userNumber)
            sendJson.put("token", userToken)
            sendJson.put("content", contentJson)

            try {
                receivedJson = postHtml("/chat/get", sendJson)
            } catch (e: Exception) {
                println(e.message)
                return false
            }
            return true
        }

        override fun onPostExecute(success: Boolean) {
            super.onPostExecute(success)

            if (success){
                val mapper = ObjectMapper().registerKotlinModule()
                val obj:Response = mapper.readValue(receivedJson)
                if (obj.error == 1) {
                    val objNG: getTalkNG = mapper.readValue(receivedJson)
                    if (objNG.content.not_authenticated == 1) {
                        Toast.makeText(applicationContext, "Without Login", Toast.LENGTH_LONG).show()
                    }
                    else if (objNG.content.invalid_verify == 1) {
                        Toast.makeText(applicationContext, "Invalid Verification", Toast.LENGTH_LONG).show()
                    }
                }
                else {
                    val objOK: getTalkOK = mapper.readValue(receivedJson)
                    val talks = objOK.content.talk[0].content.new
                    for (one_message in talks) {
                        val sent_user_name = one_message.sent_user_name
                        val content_content = one_message.content_content
                        val sender = "From " + sent_user_name
                        val content_id = one_message.content_id
                        if(latest_content_id < content_id) {
                            if (sent_user_name == userName) {
                                val wc = LinearLayout.LayoutParams.WRAP_CONTENT
                                val layoutParams = LinearLayout.LayoutParams(wc, wc)
                                layoutParams.setMargins(50, 0, 0, 0)
                                layoutParams.gravity = Gravity.RIGHT

                                val messageText = TextView(this@TalkActivity)
                                messageText.text = sender
                                messageText.layoutParams = layoutParams
                                talk_layout.addView(messageText)

                                val messageContetnt = TextView(this@TalkActivity)
                                messageContetnt.text = " " + content_content + " "
                                messageContetnt.setBackgroundColor(Color.GREEN)
                                messageContetnt.layoutParams = layoutParams
                                messageContetnt.textSize = 20.toFloat()
                                talk_layout.addView(messageContetnt)
                            } else {
                                val wc = LinearLayout.LayoutParams.WRAP_CONTENT
                                val layoutParams = LinearLayout.LayoutParams(wc, wc)
                                layoutParams.setMargins(0, 0, 50, 0)
                                layoutParams.gravity = Gravity.LEFT

                                val messageText = TextView(this@TalkActivity)
                                messageText.text = sender
                                messageText.layoutParams = layoutParams
                                talk_layout.addView(messageText)

                                val messageContetnt = TextView(this@TalkActivity)
                                messageContetnt.text = " " + content_content + " "
                                messageContetnt.setBackgroundColor(Color.LTGRAY)
                                messageContetnt.layoutParams = layoutParams
                                messageContetnt.textSize = 20.toFloat()
                                talk_layout.addView(messageContetnt)
                            }
                            //scroll_talk.fullScroll(ScrollView.FOCUS_DOWN)
                            scroll_talk.post( object: Runnable {
                                override fun run() {
                                    scroll_talk.scrollTo(0, scroll_talk.bottom)
                                }
                            })
                        }
                        latest_content_id = if(latest_content_id < content_id) content_id else latest_content_id
                    }
                }
            }
        }
    }
}