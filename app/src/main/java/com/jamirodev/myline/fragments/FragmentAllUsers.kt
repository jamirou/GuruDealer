package com.jamirodev.myline.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jamirodev.myline.Model.ModelUser
import com.jamirodev.myline.R
import com.jamirodev.myline.adapters.AdapterUser


class FragmentAllUsers : Fragment() {
    private var userAdapter: AdapterUser? = null
    private var userList: List<ModelUser>? = null
    private var rvUsers: RecyclerView? = null
    private lateinit var Et_search_user: EditText


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_all_users, container, false)

        rvUsers = view.findViewById(R.id.RV_users)
        rvUsers!!.setHasFixedSize(true)
        rvUsers!!.layoutManager = LinearLayoutManager(context)
        Et_search_user = view.findViewById(R.id.Et_search_user)

        userList = ArrayList()
        userAdapter = AdapterUser(requireContext(), userList!!) // this could be fail
        rvUsers!!.adapter = userAdapter // this could be fail

        getUsersDB()

        Et_search_user.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                TODO("Not yet implemented")
            }

            override fun onTextChanged(s_user: CharSequence?, start: Int, before: Int, count: Int) {
                searchUser(s_user.toString().lowercase())
            }

            override fun afterTextChanged(s: Editable?) {
//                TODO("Not yet implemented")
            }
        })

        return view
    }


    private fun getUsersDB() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("uid")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Verifica que Et_search_user no sea nulo y que su texto esté vacío
                if (Et_search_user != null && Et_search_user.text.toString().isEmpty()) {
                    (userList as ArrayList<ModelUser>).clear()
                    for (sh in snapshot.children) {
                        val user: ModelUser? = sh.getValue(ModelUser::class.java)
                        if (!(user!!.getUid()).equals(firebaseUser)) {
                            (userList as ArrayList<ModelUser>).add(user)
                        }
                    }
                    userAdapter?.notifyDataSetChanged() // Notifica al adaptador después de actualizar la lista
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun searchUser(searchUser: String) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
        val consult = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("names")

        val searchUserLowerCase = searchUser.lowercase()

        consult.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                (userList as ArrayList<ModelUser>).clear()

                for (sh in snapshot.children) {
                    val user: ModelUser? = sh.getValue(ModelUser::class.java)
                    if (!(user!!.getUid()).equals(firebaseUser)) {
                        val userNameLowerCase = user.getNames()!!.lowercase()

                        if (userNameLowerCase.contains(searchUserLowerCase)) {
                            (userList as ArrayList<ModelUser>).add(user)
                        }
                    }
                }
                userAdapter = AdapterUser(context!!, userList!!)
                rvUsers!!.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


}