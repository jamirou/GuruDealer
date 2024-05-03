package com.jamirodev.myline.options_login

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.jamirodev.myline.RecoverPasswordActivity
import com.jamirodev.myline.Register_email_Activity
import com.jamirodev.myline.SplashScreenActivity
import com.jamirodev.myline.databinding.ActivityLoginEmailBinding

class Login_email_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginEmailBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("GuruDealer es un espacio único")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.BtnGetIn.setOnClickListener {
            validateInfo()
        }

        binding.Register.setOnClickListener {
            startActivity(Intent(this@Login_email_Activity, Register_email_Activity::class.java))
        }

        binding.TvRecoverPassword.setOnClickListener {
            startActivity(Intent(this@Login_email_Activity, RecoverPasswordActivity::class.java))
        }
        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private var email = ""
    private var password = ""
    private fun validateInfo() {
        email = binding.EtEmail.text.toString().trim()
        password = binding.EtPassword.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.EtEmail.error = "Email inválido"
            binding.EtEmail.requestFocus()
        } else if (email.isEmpty()) {
            binding.EtEmail.error = "Ingrese email"
            binding.EtEmail.requestFocus()
        } else if (password.isEmpty()) {
            binding.EtPassword.error = "Ingresar contraseña"
            binding.EtPassword.requestFocus()
        } else {
            loginUser()
        }
    }

    private fun loginUser() {
        progressDialog.setMessage("Ingresando datos")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(this, SplashScreenActivity::class.java))
                finishAffinity()
                Toast.makeText(
                    this,
                    "Bienvenid@ a GuruDealer!",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Contraseña incorrecta",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
