package com.jamirodev.myline

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.jamirodev.myline.databinding.ActivityLoginOptionsBinding
import com.jamirodev.myline.options_login.Login_email_Activity

class LoginOptionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginOptionsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var myGoogleSignInClient: GoogleSignInClient
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Quita los anuncios con un pago único")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        checkSession()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        myGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.logInEmail.setOnClickListener {
            startActivity(Intent(this@LoginOptionsActivity, Login_email_Activity::class.java))
        }

        binding.logInGoogle.setOnClickListener {
            googleLogin()
        }

        binding.toggleButton.setOnClickListener {
            toggleTermsVisibility()
        }

    }

    private fun toggleTermsVisibility() {
        val isTermsVisible = binding.scrollView.visibility == View.VISIBLE
        if (isTermsVisible) {
            binding.scrollView.visibility = View.GONE
            binding.termsTextView.visibility = View.VISIBLE
        } else {
            binding.scrollView.visibility = View.VISIBLE
            binding.termsTextView.visibility = View.VISIBLE
        }
    }

    private fun googleLogin() {
        val googleSignInIntent = myGoogleSignInClient.signInIntent
        googleSignInARL.launch(googleSignInIntent)
    }

    private val googleSignInARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                authenticationGoogle(account.idToken)
            } catch (e: Exception) {
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun authenticationGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { resultAuth ->
                if (resultAuth.additionalUserInfo!!.isNewUser) {
                    fillInfoInDB()
                } else {
                    startActivity(Intent(this, SplashScreenActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
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
        hashMap["proveedor"] = "Google"
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
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT)
                    .show()

            }
    }

    private fun checkSession() {
        if (firebaseAuth.currentUser != null) {
            startActivity(Intent(this, SplashScreenActivity::class.java))
            finishAffinity()
        }
    }
}
