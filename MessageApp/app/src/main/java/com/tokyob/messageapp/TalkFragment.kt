package com.tokyob.messageapp

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

import kotlinx.android.synthetic.main.fragment_talk.*

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject

import kotlinx.android.synthetic.main.fragment_talk.view.*

fun getHtml(): String {
    val client = OkHttpClient()
    val req = Request.Builder().url("---").get().build()
    val resp = client.newCall(req).execute()
    return resp.body()!!.string()
}

fun postHtml(): String {

    val url = "---" + "/friend/list"
    val client: OkHttpClient = OkHttpClient.Builder().build()

    // create json
    val json = JSONObject()
    json.put("authenticated", 1)
    json.put("id", 2)
    json.put("token", "014283b9607c51dd3d3df66243cbc5ebfeeec1e632c8f7f90a1c155dffab0f0c")

    // post
    val postBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
    val request: Request = Request.Builder().url(url).post(postBody).build()
    val response = client.newCall(request).execute()

    // getResult
    val result: String = response.body()!!.string()
    response.close()
    return result
}

class TalkFragment : Fragment() {
    var friend_name :String = "test"
    lateinit var friend_list: JsonObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MyAsyncTask().execute()

    }

    inner class MyAsyncTask: AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg p0: Void?): String {
            return postHtml()
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            val parser: Parser = Parser()
            val stringBuilder: StringBuilder = StringBuilder(result)
            val json: JsonObject = parser.parse(stringBuilder) as JsonObject

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        /*
        a.forEach { (k, v) ->
            s = v
        }
        */

        add_button.setOnClickListener{
            val button = Button(getActivity())
            button.setText("test")

            button.setOnClickListener {
                val intent = Intent(getActivity(), TalkActivity::class.java)
                val id = arrayOf(1, 2)
                intent.putExtra("user_id", id)
                startActivity(intent)
            }

            liner_layout.addView(button)

            //val textView = TextView(getActivity())
            //textView.text = friend_name
            //liner_layout.addView(textView)
        }
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
