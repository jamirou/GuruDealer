package com.jamirodev.myline.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jamirodev.myline.Model.ModelChats
import com.jamirodev.myline.adapters.AdapterChats
import com.jamirodev.myline.databinding.FragmentChatsBinding

class FragmentChats : Fragment() {

    private lateinit var binding: FragmentChatsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var myUid = ""
    private lateinit var chatsArrayList: ArrayList<ModelChats>
    private lateinit var adapterChats: AdapterChats
    private lateinit var myContext: Context

    private var uidSeller = ""

//    private var adViewChats: AdView?=null


    override fun onAttach(context: Context) {
        myContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        myUid = "${firebaseAuth.uid}"

        loadChats()
//        startSdkBanner()

        binding.EtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(filter: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    val consult = filter.toString()
                    adapterChats.filter.filter(consult)
                } catch (e: Exception) {
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

    }

    private fun loadChats() {
        chatsArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatsArrayList.clear()
                for (ds in snapshot.children) {
                    val chatKey = "${ds.key}"
                    if (chatKey.contains(myUid)) {
                        val modelChats = ModelChats()
                        modelChats.keyChat = chatKey
                        chatsArrayList.add(modelChats)
                    }
                }
                adapterChats = AdapterChats(myContext, chatsArrayList)
                binding.ChatsRV.adapter = adapterChats
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

//    private fun startSdkBanner() {
//        adViewChats = binding.adViewChats
//        MobileAds.initialize(myContext) {
//
//        }
//        val adRequest = AdRequest.Builder().build()
//        adViewChats?.loadAd(adRequest)
//    }

    private fun updateStatus(status: String) {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            val ref =
                FirebaseDatabase.getInstance().reference.child("Users").child(firebaseAuth.uid!!)
            val hashMap = HashMap<String, Any>()
            hashMap["estado"] = status
            ref!!.updateChildren(hashMap)
        }
    }

    override fun onResume() {
        super.onResume()
//        adViewChats?.resume()
        updateStatus("conectado")
    }

    override fun onPause() {
        super.onPause()
//        adViewChats?.pause()
        updateStatus("desconectado")
    }

    override fun onDestroy() {
        super.onDestroy()
//        adViewChats?.destroy()
    }

}