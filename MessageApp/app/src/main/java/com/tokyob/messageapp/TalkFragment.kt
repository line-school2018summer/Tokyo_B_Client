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
import android.widget.Toast
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.android.synthetic.main.fragment_register.*

import kotlinx.android.synthetic.main.fragment_talk.*

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject

import kotlinx.android.synthetic.main.fragment_talk.view.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class ResponseFriendList(val error: Int, val content: FriendListOK)
@JsonIgnoreProperties(ignoreUnknown = true)
data class FriendListOK(val message: String, val friends: List<Friend>)
data class Friend(val user_id: String, val name: String, val id: Int)

data class ResponseFriendListError(val error: Int, val content: FriendListError)
data class FriendListError(val not_authenticated: Int, val invalid_verify: Int)

class TalkFragment : Fragment() {
    var friend_name :String = "test"
    lateinit var friend_list: JsonObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MyAsyncTask().execute()

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


    inner class MyAsyncTask: AsyncTask<Void, Void, String>() {

        private var receivedJson: String = ""

        override fun doInBackground(vararg p0: Void?): String {
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

            if (receivedJson != ""){
                val mapper = ObjectMapper().registerKotlinModule()
                val obj:Response = mapper.readValue(receivedJson!!)

                if (obj.error == 0) {
                    val objOK: ResponseFriendList = mapper.readValue(receivedJson!!)
                    val friendsList = objOK.content.friends

                    for (i in friendsList){

                        val button = Button(getActivity())
                        button.setText(i.name)

                        button.setOnClickListener {
                            val intent = Intent(getActivity(), TalkActivity::class.java)
                            var homeActivity = activity as HomeActivity
                            intent.putExtra("id", homeActivity.userNumber)
                            intent.putExtra("user_id", homeActivity.userID)
                            intent.putExtra("friend_id", i.id)
                            intent.putExtra("token", homeActivity.userToken)

                            startActivity(intent)
                        }
                        liner_layout.addView(button)
                    }
                } else {
                    val objNG: ResponseFriendListError = mapper.readValue(receivedJson!!)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_talk, container, false)

    }


    /*
    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TalkFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                TalkFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
    */
}
