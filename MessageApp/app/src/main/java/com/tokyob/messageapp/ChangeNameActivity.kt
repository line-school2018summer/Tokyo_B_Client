package com.tokyob.messageapp

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Debug
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

import kotlinx.android.synthetic.main.activity_change_name.*
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_talk.*
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject


class ChangeNameActivity : Activity() {

    lateinit var user_id: String
    lateinit var password: String

    fun postHtml(): String {

        val url = getString(R.string.server_url) + "/account/modify"
        val client: OkHttpClient = OkHttpClient.Builder().build()

        // create json

        val json = JSONObject()
        val m = JSONObject()

        m.put("user_id", "")
        m.put("name", "suzuki00")
        m.put("password", "")
        m.put("password_confirm", "")

        json.put("authenticated", 1)
        json.put("user_id", user_id)
        json.put("password", password)
        json.put("modify", m)

        // post
        val postBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        val request: Request = Request.Builder().url(url).post(postBody).build()
        val response = client.newCall(request).execute()

        // getResult
        val result: String = response.body()!!.string()
        response.close()

        Log.d("__________", "test 05")
        Log.d("__________", result)

        return result
    }

    inner class MyAsyncTask: AsyncTask<Void, Void, String>() {

        private var receivedJson: String = ""

        override fun doInBackground(vararg p0: Void?): String {

            Log.d("__________", "test 04")


            try {
                receivedJson = postHtml()
            } catch (e: Exception) {
                println(e.message)
                return ""
            }
            return receivedJson
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            Log.d("__________", "test 02")


            if (receivedJson != ""){
                val mapper = ObjectMapper().registerKotlinModule()
                val obj:Response = mapper.readValue(receivedJson!!)

                if (obj.error == 0) {
                    val json: S_JSON = mapper.readValue(receivedJson!!)
                    val friendsList = json.content.user_id
                }
            }

            Log.d("__________", "test 03")

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_change_name)

        button.setOnClickListener {
            Log.d("__________", "test 01")
            MyAsyncTask().execute()
            Log.d("__________", "test 00")
        }

        user_id = intent.getStringExtra("s_user_id")
        password = intent.getStringExtra("s_password")

    }

}