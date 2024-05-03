package com.jamirodev.myline.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
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
import com.jamirodev.myline.databinding.FragmentFavAdBinding


class Fav_Ad_Fragment : Fragment() {

    private lateinit var binding: FragmentFavAdBinding
    private lateinit var myContext: Context
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var announceArrayList: ArrayList<ModelAnnounce>
    private lateinit var announceAdapter: AdapterAnnounce

    private var adViewFabAdd: AdView? = null

    override fun onAttach(context: Context) {
        this.myContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFavAdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        loadAnnounceFav()
        startSdkBanner()

        binding.EtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(filter: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    val consult = filter.toString()
                    announceAdapter.filter.filter(consult)
                } catch (e: Exception) {

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.IBClear.setOnClickListener {
            val consult = binding.EtSearch.text.toString().trim()
            if (consult.isNotEmpty()) {
                binding.EtSearch.setText("")
            } else {

            }
        }
    }

    private fun startSdkBanner() {
        adViewFabAdd = binding.adViewFavAd
        MobileAds.initialize(myContext) {

        }
        val adRequest = AdRequest.Builder().build()
        adViewFabAdd?.loadAd(adRequest)
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
        adViewFabAdd?.resume()
        updateStatus("conectado")
    }

    override fun onPause() {
        super.onPause()
        adViewFabAdd?.pause()
        updateStatus("desconectado")
    }

    override fun onDestroy() {
        super.onDestroy()
        adViewFabAdd?.destroy()
    }

    private fun loadAnnounceFav() {
        announceArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    announceArrayList.clear()
                    for (ds in snapshot.children) {
                        val idAnnounce = "${ds.child("idAnnounce").value}"

                        val refFavorite =
                            FirebaseDatabase.getInstance().getReference("Announcements")
                        refFavorite.child(idAnnounce)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    try {
                                        val modelAnnounce =
                                            snapshot.getValue(ModelAnnounce::class.java)
                                        announceArrayList.add(modelAnnounce!!)
                                    } catch (e: Exception) {
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }
                            })
                    }
                    Handler().postDelayed({
                        announceAdapter = AdapterAnnounce(myContext, announceArrayList)
                        binding.announcesRV.adapter = announceAdapter
                    }, 500)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

}