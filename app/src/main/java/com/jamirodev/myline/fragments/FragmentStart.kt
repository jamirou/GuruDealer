package com.jamirodev.myline.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jamirodev.myline.Constants
import com.jamirodev.myline.Model.ModelAnnounce
import com.jamirodev.myline.Model.ModelCategory
import com.jamirodev.myline.R
import com.jamirodev.myline.RvListenerCategory
import com.jamirodev.myline.SelectLocationActivity
import com.jamirodev.myline.adapters.AdapterAnnounce
import com.jamirodev.myline.adapters.AdapterCategory
import com.jamirodev.myline.databinding.FragmentStartBinding


class FragmentStart : Fragment() {

    private lateinit var binding: FragmentStartBinding

    private companion object {
        private const val MAX_DISTANCE_SHOW_AD = 10
    }

    private lateinit var myContext: Context
    private lateinit var announceArrayList: ArrayList<ModelAnnounce>
    private lateinit var adapterAnnounce: AdapterAnnounce
    private lateinit var locationSP: SharedPreferences

    private var actualLatitude = 0.0
    private var actualLongitude = 0.0
    private var actualDirection = ""

    override fun onAttach(context: Context) {
        myContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStartBinding.inflate(LayoutInflater.from(myContext), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationSP = myContext.getSharedPreferences("LOCATION_SP", Context.MODE_PRIVATE)

        actualLatitude = locationSP.getFloat("ACTUAL_LATITUDE", 0.0f).toDouble()
        actualLongitude = locationSP.getFloat("ACTUAL_LONGITUDE", 0.0f).toDouble()
        actualDirection = locationSP.getString("ACTUAL_DIRECTION", "")!!

        if (actualLatitude != 0.0 && actualLongitude != 0.0) {
            binding.TvLocation.text = actualDirection
        }
        loadCategory()
        loadAnnounces("Todo")

        binding.TvLocation.setOnClickListener {
            val intent = Intent(myContext, SelectLocationActivity::class.java)
            selectLocationARL.launch(intent)
        }

        binding.EtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(filter: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    val consult = filter.toString()
                    adapterAnnounce.filter.filter(consult)
                } catch (e: Exception) {

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

    }

    private val selectLocationARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                actualLatitude = data.getDoubleExtra("latitud", 0.0)
                actualLongitude = data.getDoubleExtra("longitud", 0.0)
                actualDirection = data.getStringExtra("direction").toString()

                locationSP.edit()
                    .putFloat("ACTUAL_LATITUDE", actualLatitude.toFloat())
                    .putFloat("ACTUAL_LONGITUDE", actualLongitude.toFloat())
                    .putString("ACTUAL_DIRECTION", actualDirection)
                    .apply()

                binding.TvLocation.text = actualDirection

                loadAnnounces("Todo")
            } else {
                Toast.makeText(
                    context, "Cancelado", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadCategory() {
        val categoryArrayList = ArrayList<ModelCategory>()
        for (i in 0 until Constants.category.size) {
            val modelCategory = ModelCategory(Constants.category[i], Constants.categoryIcon[i])
            categoryArrayList.add(modelCategory)
        }

        val adapterCategory = AdapterCategory(
            myContext,
            categoryArrayList,
            object : RvListenerCategory {
                override fun onCategoryClick(modelCategory: ModelCategory) {
                    val categorySelected = modelCategory.category
                    loadAnnounces(categorySelected)
                }
            }
        )

        binding.categoryRV.adapter = adapterCategory
    }

    private fun loadAnnounces(category: String) {
        announceArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Announcements")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allAnnounces = snapshot.children.mapNotNull { ds ->
                    try {
                        ds.getValue(ModelAnnounce::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }

                val filteredAnnounces = allAnnounces.filter { modelAnnounce ->
                    val distance = calculateDistanceKM(
                        modelAnnounce.latitud ?: 0.0,
                        modelAnnounce.longitud ?: 0.0
                    )
                    distance <= MAX_DISTANCE_SHOW_AD
                }

                val filteredByCategory = if (category == "Todo") {
                    filteredAnnounces
                } else {
                    filteredAnnounces.filter { modelAnnounce ->
                        modelAnnounce.category == category
                    }
                }

                val shuffledAnnounces = filteredByCategory.shuffled()

                announceArrayList.addAll(shuffledAnnounces)
                adapterAnnounce = AdapterAnnounce(myContext, announceArrayList)
                binding.announcesRV.adapter = adapterAnnounce
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun calculateDistanceKM(latitud: Double, longitud: Double): Double {
        val puntoStart = Location(LocationManager.NETWORK_PROVIDER)
        puntoStart.latitude = actualLatitude
        puntoStart.longitude = actualLongitude

        val puntoEnd = Location(LocationManager.NETWORK_PROVIDER)
        puntoEnd.latitude = latitud
        puntoEnd.longitude = longitud

        val distanceMeters = puntoStart.distanceTo(puntoEnd).toDouble()
        return distanceMeters / 2400
    }

}