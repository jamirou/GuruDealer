package com.jamirodev.myline.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jamirodev.myline.Model.ModelAnnounce
import com.jamirodev.myline.R
import com.jamirodev.myline.adapters.AdapterAnnounce
import com.jamirodev.myline.databinding.FragmentMyAdsPublishedBinding

class My_Ads_Published_Fragment : Fragment() {

    private lateinit var binding: FragmentMyAdsPublishedBinding
    private lateinit var myContext: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var announceArrayList: ArrayList<ModelAnnounce>
    private lateinit var announceAdapter: AdapterAnnounce

    private var adView_my_adds_published: AdView?=null

    override fun onAttach(context: Context) {
        this.myContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMyAdsPublishedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        loadMyAnnounces()

        binding.EtSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(filter: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    val consult = filter.toString()
                    announceAdapter.filter.filter(consult)
                }catch (e:Exception) {

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.IbClear.setOnClickListener {
            val consult = binding.EtSearch.text.toString().trim()
            if (consult.isNotEmpty()) {
                binding.EtSearch.setText("")
            } else {

            }
        }
    }


    private fun loadMyAnnounces() {
        announceArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Announcements")
        ref.orderByChild("uid").equalTo(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    announceArrayList.clear()

                    for (ds in snapshot.children) {
                        try {
                            val modelAnnounce = ds.getValue(ModelAnnounce::class.java)
                            announceArrayList.add(modelAnnounce!!)
                        }catch (e:Exception) {

                        }
                    }
                    announceAdapter = AdapterAnnounce(myContext, announceArrayList)
                    binding.myAnnouncesRV.adapter = announceAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }


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
        adView_my_adds_published?.resume()
        updateStatus("conectado")
    }

    override fun onPause() {
        super.onPause()
        adView_my_adds_published?.pause()
        updateStatus("desconectado")
    }

    override fun onDestroy() {
        super.onDestroy()
        adView_my_adds_published?.destroy()
    }

}