package com.jamirodev.myline

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.jamirodev.myline.advertisements.Create_new_ad
import com.jamirodev.myline.databinding.ActivityMainBinding
import com.jamirodev.myline.fragments.FragmentAllChats
import com.jamirodev.myline.fragments.FragmentCuenta
import com.jamirodev.myline.fragments.FragmentMisAnuncios
import com.jamirodev.myline.fragments.FragmentStart

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkSession()

        verFragmentStart()

        binding.BtnNV.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_start -> {
                    verFragmentStart()
                    true
                }

                R.id.item_chats -> {
                    verFragmentChats()
                    true
                }

                R.id.item_mis_anuncios -> {
                    verFragmentMisanuncios()
                    true
                }

                R.id.item_cuenta -> {
                    verFragmentCuenta()
                    true
                }

                else -> {
                    false
                }
            }
        }

        binding.FAB.setOnClickListener {
            val intent = Intent(this, Create_new_ad::class.java)
            intent.putExtra("Edition", false)
            startActivity(intent)
        }

    }




    private fun checkSession() {
        if (firebaseAuth.currentUser == null) {
            startActivity(Intent(this, LoginOptionsActivity::class.java))
            finishAffinity()
        } else {
            addFcmToken()
            requestNotificationPermission()
        }
    }

    private fun verFragmentStart() {
        binding.titleRL.text = "Tienda"
        val fragment = FragmentStart()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "FragmentInicio")
        fragmentTransition.commit()
    }

    private fun verFragmentChats() {
        binding.titleRL.text = "Chats"
        val fragment = FragmentAllChats()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "FragmentAllChats")
        fragmentTransition.commit()
    }

    private fun verFragmentMisanuncios() {
        binding.titleRL.text = "Mío"
        val fragment = FragmentMisAnuncios()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "FragmentMisAnuncios")
        fragmentTransition.commit()
    }

    private fun verFragmentCuenta() {
        binding.titleRL.text = "Perfil"
        val fragment = FragmentCuenta()
        val fragmentTransition = supportFragmentManager.beginTransaction()
        fragmentTransition.replace(binding.FragmentL1.id, fragment, "FragmentCuenta")
        fragmentTransition.commit()
    }

    private fun addFcmToken() {
        val myUid = "${firebaseAuth.uid}"
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { fcmToken ->
                val hashMap = HashMap<String, Any>()
                hashMap["fcmToken"] = "$fcmToken"
                val ref = FirebaseDatabase.getInstance().getReference("Users")
                ref.child(myUid)
                    .updateChildren(hashMap)
                    .addOnSuccessListener {

                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_DENIED
            ) {
                permissionNotification.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val permissionNotification =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->

        }

}