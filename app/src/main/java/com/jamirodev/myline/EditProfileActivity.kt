package com.jamirodev.myline

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.jamirodev.myline.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Verifica tu cuenta y valida tus datos")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.IbBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        loadInfo()

        binding.BtnUpdate.setOnClickListener {
            validateInfo()
        }

        binding.FABChangeImg.setOnClickListener {
            select_image_from()
        }
    }

    private var names = ""
    private var birthday = ""
    private var code = ""
    private var phone = ""
    private fun validateInfo() {
        names = binding.EtNames.text.toString().trim()
        birthday = binding.EtBirthday.text.toString().trim()
        code = binding.codeSelector.selectedCountryCodeWithPlus
        phone = binding.EtPhone.text.toString().trim()

        //update info validation
        if (names.isEmpty()) {
            binding.EtNames.error = "Ingrese nombre"
            binding.EtNames.requestFocus()
        } else if (birthday.isEmpty()) {
            binding.EtBirthday.error = "Ingrese una edad"
            binding.EtBirthday.requestFocus()
        } else if (!isValidAge(birthday.toInt())) {
            binding.EtBirthday.error = "Edad no válida"
            binding.EtBirthday.requestFocus()
        } else if (code.isEmpty()) {
            Toast.makeText(this, "Seleccione codigo", Toast.LENGTH_SHORT).show()
        } else if (phone.isEmpty()) {
            binding.EtPhone.error = "Ingrese un teléfono"
            binding.EtPhone.requestFocus()
        } else {
            updateInfo()
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun isValidAge(age: Int): Boolean {
        return age in 18..99
    }

    private fun updateInfo() {
        progressDialog.setMessage("Actualizando")

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["names"] = "${names}"
        hashMap["birthday"] = "${birthday}"
        hashMap["phoneCode"] = "${code}"
        hashMap["phone"] = "${phone}"

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val names = "${snapshot.child("names").value}"
                    val image = "${snapshot.child("urlProfileImage").value}"
                    val birthday = "${snapshot.child("birthday").value}"
                    val phone = "${snapshot.child("phone").value}"
                    val phoneCode = "${snapshot.child("phoneCode").value}"

                    binding.EtNames.setText(names)
                    binding.EtBirthday.setText(birthday)
                    binding.EtPhone.setText(phone)

                    try {
                        Glide.with(applicationContext)
                            .load(image)
                            .placeholder(R.drawable.usuario)
                            .into(binding.imgProfile)
                    } catch (e: Exception) {
                    }
                    try {
                        val code = phoneCode.replace("+", "").toInt()
                        binding.codeSelector.setCountryForPhoneCode(code)
                    } catch (e: Exception) {
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun uploadImageStorage() {
        progressDialog.setMessage("Almacenando")
        progressDialog.show()

        val routeImage = "ProfileImage/" + firebaseAuth.uid
        val ref = FirebaseStorage.getInstance().getReference(routeImage)
        ref.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val urlLoadedImage = uriTask.result.toString()
                if (uriTask.isSuccessful) {
                    updateImageInDB(urlLoadedImage)
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateImageInDB(urlLoadedImage: String) {
        progressDialog.setMessage("Actualizando")
        progressDialog.show()

        val hashMap: HashMap<String, Any> = HashMap()
        if (imageUri != null) {
            hashMap["urlProfileImage"] = urlLoadedImage
        }

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun select_image_from() {
        val popupMenu = PopupMenu(this, binding.FABChangeImg)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Cámara")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Galería")
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { item ->
            val itemId = item.itemId
            if (itemId == 1) {
                //Open Camera
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    cameramanPermission.launch(arrayOf(android.Manifest.permission.CAMERA))
                } else {
                    cameramanPermission.launch(
                        arrayOf(
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    )
                }
            } else if (itemId == 2) {
                //Open Gallery
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    galleryImage()
                } else {
                    storagePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            return@setOnMenuItemClickListener true
        }
    }

    //Request camara permissions
    private val cameramanPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            var grantedAll = true
            for (granted in result.values) {
                grantedAll = grantedAll && granted
            }
            if (grantedAll) {
                cameraImage()
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }

    private fun cameraImage() {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Titulo_imagen")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Descripcion_imagen")
        imageUri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraResult_ARL.launch(intent)
    }


    private val cameraResult_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                uploadImageStorage()
            } else {
                Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
            }
        }

    private val storagePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                galleryImage()
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show()
            }
        }

    private fun galleryImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultGallery_ARL.launch(intent)
    }

    private val resultGallery_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                imageUri = data!!.data
                uploadImageStorage()
            } else {
                Toast.makeText(this, "", Toast.LENGTH_SHORT).show()
            }
        }

}



