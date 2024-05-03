package com.jamirodev.myline

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.jamirodev.myline.databinding.ActivityRegisterEmailBinding

class Register_email_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterEmailBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("No olvides verificar tu cuenta:)")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.BtnRegister.setOnClickListener {
            validateInfo()
        }
        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private var email = ""
    private var password = ""
    private var r_password = ""
    private fun validateInfo() {
        email = binding.EtEmail.text.toString().trim()
        password = binding.EtPassword.text.toString().trim()
        r_password = binding.EdRPassword.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.EtEmail.error = "Email Inválido"
            binding.EtEmail.requestFocus()
        } else if (email.isEmpty()) {
            binding.EtEmail.error = "Ingrese un email"
            binding.EtEmail.requestFocus()
        } else if (password.isEmpty()) {
            binding.EtPassword.error = "Ingrese una contraseña de mínimo 6 dígitos"
            binding.EtPassword.requestFocus()
        } else if (r_password.isEmpty()) {
            binding.EdRPassword.error = "Repita la misma contraseña"
            binding.EdRPassword.requestFocus()
        } else if (password != r_password) {
            binding.EdRPassword.error = "Las contraseñas no coinciden"
            binding.EdRPassword.requestFocus()
        } else {
            registerUser()
        }
    }

    private fun registerUser() {
        progressDialog.setMessage("Registrando usuario")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                fillInfoInDB()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "No se ha podido registrar el usuario. Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

            }
    }

    private fun fillInfoInDB() {
        progressDialog.setMessage("Obteniendo la información")

        val time = Constants.getTimeDis()
        val emailUser = firebaseAuth.currentUser!!.email
        val uidUser = firebaseAuth.uid

        var username = firebaseAuth.currentUser!!.displayName
        if (username.isNullOrEmpty()) {
            username = "Usuario no identificado"
        }


        val hashMap = HashMap<String, Any>()
        hashMap["names"] = "$username"
        hashMap["phoneCode"] = ""
        hashMap["phone"] = ""
        hashMap["urlProfileImage"] = ""
        hashMap["proveedor"] = "Email"
        hashMap["escribiendo"] = ""
        hashMap["time"] = time
        hashMap["estado"] = "desconectado"
        hashMap["email"] = "${emailUser}"
        hashMap["uid"] = "${uidUser}"
        hashMap["birthday"] = ""

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uidUser!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                startActivity(Intent(this, SplashScreenActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "ha ocurrido un error: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}