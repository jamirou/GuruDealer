package com.jamirodev.myline

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.jamirodev.myline.databinding.ActivityRecoverPasswordBinding

class RecoverPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecoverPasswordBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecoverPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Cargando...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.IbBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.BtnSendInstructions.setOnClickListener {
            validateEmail()
        }
    }

    private var email = ""
    private fun validateEmail() {
        email = binding.EtEmail.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(this, "Ingrese email", Toast.LENGTH_SHORT).show()
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.EtEmail.error = "Email no válido"
            binding.EtEmail.requestFocus()
        } else {
            sendInstructions()
        }
    }

    private fun sendInstructions() {
        progressDialog.setMessage("cargando Instrucciones")
        progressDialog.dismiss()

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Instrucciones enviadas al email: ${email}",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}







