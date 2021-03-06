package com.tokyob.messageapp

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_select_menu.*


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SelectMenuFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [SelectMenuFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class SelectMenuFragment : Fragment() {
    private var listener: OnSelectMenuFragmentListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sign_in_button.setOnClickListener { listener?.onLoginSelected()}
        register_button.setOnClickListener { listener?.onRegisterSelected()}
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSelectMenuFragmentListener) {
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
    interface OnSelectMenuFragmentListener {
        fun onLoginSelected()
        fun onRegisterSelected()
    }
}
