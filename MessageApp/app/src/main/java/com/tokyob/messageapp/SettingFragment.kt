package com.tokyob.messageapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.beust.klaxon.JsonObject
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.android.synthetic.main.fragment_talk.*
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

import kotlinx.android.synthetic.main.fragment_setting.*

data class S_JSON(val error: Int, val content: S_Content)
data class S_Content(val user_id: String, val name: String, val message: String)


class SettingFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    fun postHtml(): String {

        val url = getString(R.string.server_url) + "/friend/list"
        val client: OkHttpClient = OkHttpClient.Builder().build()

        // create json
        val json = JSONObject()
        var homeActivity = activity as HomeActivity
        json.put("authenticated", 1)
        json.put("id", homeActivity.userNumber)
        json.put("token", homeActivity.userToken)

        // post
        val postBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        val request: Request = Request.Builder().url(url).post(postBody).build()
        val response = client.newCall(request).execute()

        // getResult
        val result: String = response.body()!!.string()
        response.close()
        return result
    }

    fun getHtml(): String {
        val client = OkHttpClient()
        val req = Request.Builder().url(getString(R.string.server_url)).get().build()
        val resp = client.newCall(req).execute()
        return resp.body()!!.string()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var homeActivity = activity as HomeActivity
        textView01.setText(homeActivity.userName)
        textView02.setText(homeActivity.userID)

        button_name.setOnClickListener{
            val intent = Intent(getActivity(), ChangeNameActivity::class.java)
            intent.putExtra("s_user_id", homeActivity.userID)
            intent.putExtra("s_password", homeActivity.userPassword)
            startActivity(intent)

            textView01.setText(homeActivity.userName)
        }

        button_userid.setOnClickListener{
            val intent = Intent(getActivity(), ChangeUserIDActivity::class.java)
            intent.putExtra("s_user_id", homeActivity.userID)
            intent.putExtra("s_password", homeActivity.userPassword)
            startActivity(intent)

            textView02.setText(homeActivity.userID)
        }

        button_password.setOnClickListener{
            val intent = Intent(getActivity(), ChangePassWordActivity::class.java)
            intent.putExtra("s_user_id", homeActivity.userID)
            intent.putExtra("s_password", homeActivity.userPassword)

            startActivity(intent)
        }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false)

    }
}