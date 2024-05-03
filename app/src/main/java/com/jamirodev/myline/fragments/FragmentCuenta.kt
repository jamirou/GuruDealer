package com.jamirodev.myline.fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jamirodev.myline.ChangePasswordActivity
import com.jamirodev.myline.Constants
import com.jamirodev.myline.DeleteAccountActivity
import com.jamirodev.myline.EditProfileActivity
import com.jamirodev.myline.LoginOptionsActivity
import com.jamirodev.myline.R
import com.jamirodev.myline.databinding.FragmentCuentaBinding

class FragmentCuenta : Fragment() {

    private lateinit var binding: FragmentCuentaBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var myContext: Context
    private lateinit var progressDialog: ProgressDialog

    override fun onAttach(context: Context) {
        myContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCuentaBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = ProgressDialog(myContext)
        progressDialog.setTitle("Recuerda verificar tu cuenta")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        readInfo()

        binding.BtnEditProfile.setOnClickListener {
            startActivity(Intent(myContext, EditProfileActivity::class.java))
        }

        binding.BtnChangePassword.setOnClickListener {
            startActivity(Intent(myContext, ChangePasswordActivity::class.java))
        }

        binding.BtnVerifyAccount.setOnClickListener {
            verifyAccount()
        }

        binding.BtnDeleteAccount.setOnClickListener {
            startActivity(Intent(myContext, DeleteAccountActivity::class.java))
        }

        binding.BtnSignOut.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(myContext, LoginOptionsActivity::class.java))
            activity?.finishAffinity()
        }
        binding.mailIcon.setOnClickListener {
            val email = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("playconsultsjamiro@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Asunto del correo")
            }

            if (email.resolveActivity(myContext.applicationContext.packageManager) != null) {
                myContext.startActivity(email)
            }
        }
        binding.instagramIcon.setOnClickListener {
            val instagram = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/invites/contact/?i=s7y63x4gg2dm&utm_content=ryeagt6"))
            startActivity(instagram)
        }
        binding.patreonIcon.setOnClickListener {
            val patreon = Intent(Intent.ACTION_VIEW, Uri.parse("https://patreon.com/GuruDealer?utm_medium=clipboard_copy&utm_source=copyLink&utm_campaign=creatorshare_creator&utm_content=join_link"))
            startActivity(patreon)
        }

    }

    private fun readInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val names = "${snapshot.child("names").value}"
                    val email = "${snapshot.child("email").value}"
                    val image = "${snapshot.child("urlProfileImage").value}"
                    val birthday = "${snapshot.child("birthday").value}"
                    var time = "${snapshot.child("time").value}"
                    val phone = "${snapshot.child("phone").value}"
                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val proveedor = "${snapshot.child("proveedor").value}"

                    val code_phone = phoneCode + phone

                    if (time == null) {
                        time = "0"
                    }

                    val for_time = Constants.getDate(time.toLong())

                    //Set data
                    binding.TVEmail.text = email
                    binding.TVNames.text = names
                    binding.TvPhone.text = code_phone
                    binding.TvBirthday.text = birthday
                    binding.TvMember.text = for_time

                    //Set Image
                    try {
                        Glide.with(myContext)
                            .load(image)
                            .placeholder(R.drawable.usuario)
                            .into(binding.IvProfile)
                    } catch (e: Exception) {
                        Toast.makeText(
                            myContext,
                            "${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    if (proveedor == "Email") {
                        val isVerified = firebaseAuth.currentUser!!.isEmailVerified
                        if (isVerified) {
                            binding.BtnVerifyAccount.visibility = View.GONE
                            binding.TvAccountStatus.text = "✔ Verificado"
                        } else {
                            binding.BtnVerifyAccount.visibility = View.VISIBLE
                            binding.TvAccountStatus.text = "No verificado"
                        }
                    } else {
                        binding.BtnVerifyAccount.visibility = View.GONE
                        binding.TvAccountStatus.text = "✔ Verificado"
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun verifyAccount() {
        progressDialog.setMessage("Enviando instrucciones...")
        progressDialog.show()

        firebaseAuth.currentUser!!.sendEmailVerification()
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(myContext, "Instrucciones enviadas a su email", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(myContext, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}









