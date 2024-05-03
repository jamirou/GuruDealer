package com.jamirodev.myline

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jamirodev.myline.databinding.ActivityDeleteAccountBinding

class DeleteAccountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeleteAccountBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Eliminar tu cuenta")
        progressDialog.setCanceledOnTouchOutside(false)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser

        binding.IbBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.BtnDeleteAccount.setOnClickListener {
            deleteAccount()
        }
    }

    private fun deleteAccount() {
        progressDialog.setMessage("Eliminando cuenta")
        progressDialog.show()

        val myUid = firebaseAuth.uid
        firebaseUser!!.delete()
            .addOnSuccessListener {
                val announces = FirebaseDatabase.getInstance().getReference("Announcements")
                announces.orderByChild("uid").equalTo(myUid)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (ds in snapshot.children) {
                                ds.ref.removeValue()
                            }
                            val userDB = FirebaseDatabase.getInstance().getReference("Users")
                            userDB.child(myUid!!).removeValue()
                                .addOnSuccessListener {
                                    progressDialog.dismiss()
                                    goToMainActivity()
                                }
                                .addOnFailureListener { e ->
                                    progressDialog.dismiss()
                                    Toast.makeText(
                                        this@DeleteAccountActivity,
                                        "${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    goToMainActivity()
                                }

                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        goToMainActivity()
    }

}