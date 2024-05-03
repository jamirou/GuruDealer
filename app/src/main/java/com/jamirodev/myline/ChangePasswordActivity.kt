package com.jamirodev.myline

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.jamirodev.myline.databinding.ActivityChangePasswordBinding
import com.jamirodev.myline.options_login.Login_email_Activity

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Cargando...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        binding.IbBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.BtnUpdatePassword.setOnClickListener {
            validateInfo()
        }
    }
    var actual_password = ""
    var new_password = ""
    var r_new_password = ""
    private fun validateInfo() {
        actual_password = binding.EtPasswordActual.text.toString().trim()
        new_password = binding.EtPasswordNew.text.toString().trim()
        r_new_password = binding.EtPasswordNewR.text.toString().trim()

        if (actual_password.isEmpty()) {
            binding.EtPasswordActual.error = "Ingrese su contraseña actual"
            binding.EtPasswordActual.requestFocus()
        } else if (new_password.isEmpty()) {
            binding.EtPasswordNew.error = "Ingrese nueva contraseña"
            binding.EtPasswordNew.requestFocus()
        } else if (r_new_password.isEmpty()) {
            binding.EtPasswordNewR.error = "Repita su nueva contraseña"
            binding.EtPasswordNewR.requestFocus()
        } else if (new_password != r_new_password) {
            binding.EtPasswordNewR.error = "No coinciden las contraseñas"
            binding.EtPasswordNewR.requestFocus()
        } else {
            authUserChangePass()
        }//The password must be changed
    }
    
    private fun authUserChangePass() {
        progressDialog.setMessage("Validando información")
        progressDialog.show()

        val autoCredential =
            EmailAuthProvider.getCredential(firebaseUser.email.toString(), actual_password)
        firebaseUser.reauthenticate(autoCredential)
            .addOnSuccessListener {
                progressDialog.dismiss()
                updatePassword()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
                

            }
    }

    private fun updatePassword() {
        progressDialog.setMessage("Actualizando")
        progressDialog.show()
        firebaseUser.updatePassword(new_password)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                firebaseAuth.signOut()
                startActivity(Intent(this, Login_email_Activity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}