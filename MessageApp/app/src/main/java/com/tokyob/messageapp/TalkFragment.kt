package com.tokyob.messageapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.os.AsyncTask
import android.widget.Button
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.lookup
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import android.content.res.Resources
import android.os.Handler
import android.widget.Toast
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

import kotlinx.android.synthetic.main.fragment_talk.*

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject

data class TalkListOK(val error: Int, val content: TalkListContent)
data class TalkListContent(val message: String, val groups: Map<String, String>)

data class TalkListNG(val error: Int, val content: TalkListError)
data class TalkListError(val not_authenticated: Int, val invalid_verify: Int)

data class makeGroupNG(val error: Int, val content: makeGroupError)
data class makeGroupError(val not_authenticated: Int, val invalid_verify: Int)

data class makeGroupOK(val error: Int, val content: makeGroupOKContent)
data class makeGroupOKContent(val talk_id: Int, val message: String)

class TalkFragment : Fragment() {
    val talkListHandler = Handler()
    val talkListRunnable = object : Runnable {
        override fun run() {
            getTalkListTask().execute()
            talkListHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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

    inner class getTalkListTask: AsyncTask<Void, Void, Boolean>() {
        private var receivedJson: String = ""
        private val homeActivity = activity as HomeActivity

        override fun doInBackground(vararg p0: Void?): Boolean {
            val sendJson = JSONObject()
            sendJson.put("target", "/chat/list")
            sendJson.put("authenticated", 1)
            sendJson.put("id", homeActivity.userNumber)
            sendJson.put("token", homeActivity.userToken)

            try {
                receivedJson = postHtml("/chat/list", sendJson)
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

                if (obj.error == 0) {
                    val objOK: TalkListOK = mapper.readValue(receivedJson)
                    val talkList = objOK.content.groups

                    liner_layout.removeAllViews()
                    for (key in talkList.keys){
                        val button = Button(getActivity())
                        button.text = talkList[key]

                        button.setOnClickListener {
                            val intent = Intent(getActivity(), TalkActivity::class.java)
                            intent.putExtra("id", homeActivity.userNumber)
                            intent.putExtra("user_id", homeActivity.userID)
                            intent.putExtra("user_name", homeActivity.userName)
                            intent.putExtra("group_name", talkList[key])
                            intent.putExtra("group_id", key.toInt())
                            intent.putExtra("token", homeActivity.userToken)

                            startActivity(intent)
                        }
                        liner_layout.addView(button)
                    }
                } else {
                    val objNG: TalkListNG = mapper.readValue(receivedJson)
                    if (objNG.content.not_authenticated == 1) {
                        Toast.makeText(activity, "Without Login", Toast.LENGTH_LONG).show()
                    }
                    else if (objNG.content.invalid_verify == 1) {
                        Toast.makeText(activity, "Invalid Verification", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    inner class createGroupTask internal constructor(private val mGroupName: String): AsyncTask<Void, Void, Boolean>() {
        private var receivedJson: String = ""
        private val homeActivity = activity as HomeActivity

        override fun doInBackground(vararg p0: Void?): Boolean {
            val sendJson = JSONObject()
            val contentJson = JSONObject()
            contentJson.put("group_name", mGroupName)
            sendJson.put("target", "/chat/make")
            sendJson.put("authenticated", 1)
            sendJson.put("id", homeActivity.userNumber)
            sendJson.put("token", homeActivity.userToken)
            sendJson.put("content", contentJson)

            try {
                receivedJson = postHtml("/chat/make", sendJson)
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
                    val objNG: makeGroupNG = mapper.readValue(receivedJson)
                    if (objNG.content.not_authenticated == 1) {
                        Toast.makeText(activity, "Without Login", Toast.LENGTH_LONG).show()
                    }
                    else if (objNG.content.invalid_verify == 1) {
                        Toast.makeText(activity, "Invalid Verification", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val objOK: makeGroupOK = mapper.readValue(receivedJson)
                    val talkID = objOK.content.talk_id
                    group_name.text.clear()
                    val intent = Intent(getActivity(), TalkActivity::class.java)
                    val homeActivity = activity as HomeActivity
                    intent.putExtra("id", homeActivity.userNumber)
                    intent.putExtra("user_id", homeActivity.userID)
                    intent.putExtra("user_name", homeActivity.userName)
                    intent.putExtra("group_name", mGroupName)
                    intent.putExtra("group_id", talkID)
                    intent.putExtra("token", homeActivity.userToken)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        talk_start_button.setOnClickListener {
            val groupName = group_name.text.toString()
            createGroupTask(groupName).execute()
        }

        talkListHandler.post(talkListRunnable)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_talk, container, false)

    }

    override fun onStop() {
        super.onStop()
        talkListHandler.removeCallbacks(talkListRunnable)
    }

    override fun onResume() {
        super.onResume()
        talkListHandler.post(talkListRunnable)
    }
}