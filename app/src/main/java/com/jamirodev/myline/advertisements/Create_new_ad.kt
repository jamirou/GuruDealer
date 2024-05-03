package com.jamirodev.myline.advertisements

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.jamirodev.myline.Constants
import com.jamirodev.myline.MainActivity
import com.jamirodev.myline.Model.ModelImageSelected
import com.jamirodev.myline.R
import com.jamirodev.myline.SelectLocationActivity
import com.jamirodev.myline.adapters.AdapterSelectedImage
import com.jamirodev.myline.databinding.ActivityCreateNewAdBinding

class Create_new_ad : AppCompatActivity() {

    private lateinit var binding: ActivityCreateNewAdBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var imageUri: Uri? = null

    private lateinit var selectedImageArrayList: ArrayList<ModelImageSelected>
    private lateinit var adapterImageSelected: AdapterSelectedImage

    private var Edition = false
    private var idAnuncioEdit = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNewAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere")
        progressDialog.setCanceledOnTouchOutside(false)

        val adapterCat = ArrayAdapter(this, R.layout.item_category, Constants.category)
        binding.Category.setAdapter(adapterCat)

        val adapterCondition = ArrayAdapter(this, R.layout.item_condition, Constants.conditions)
        binding.Condition.setAdapter(adapterCondition)

        Edition = intent.getBooleanExtra("Edition", false)

        if (Edition) {
            idAnuncioEdit = intent.getStringExtra("idAnnounce") ?: ""
            loadDetails()
            binding.BtnCreateAnnouncement.text = "Actualizar"
        } else {
            binding.BtnCreateAnnouncement.text = "Publicar"
        }

        binding.IbBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        selectedImageArrayList = ArrayList()
        loadImages()

        binding.AddImg.setOnClickListener {
            showOptions()
        }

        binding.Location.setOnClickListener {
            val intent = Intent(this, SelectLocationActivity::class.java)
            selectLocation_ARL.launch(intent)
        }

        binding.BtnCreateAnnouncement.setOnClickListener {
            validateDatta()
        }
    }

    private fun loadDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Announcements")
        ref.child(idAnuncioEdit)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val marca = "${snapshot.child("marca").value}"
                    val category = "${snapshot.child("category").value}"
                    val condition = "${snapshot.child("condition").value}"
                    val direction = "${snapshot.child("direction").value}"
                    val price = "${snapshot.child("price").value}"
                    val title = "${snapshot.child("title").value}"
                    val description = "${snapshot.child("description").value}"
                    latitud = (snapshot.child("latitud").value) as Double
                    longitud = (snapshot.child("longitud").value) as Double

                    binding.EtMarca.setText(marca)
                    binding.Category.setText(category)
                    binding.Condition.setText(condition)
                    binding.Location.setText(direction)
                    binding.EtPrice.setText(price)
                    binding.EtTitle.setText(title)
                    binding.EtDescription.setText(description)

                    val refImages = snapshot.child("Images").ref
                    refImages.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (ds in snapshot.children) {
                                val id = "${ds.child("id").value}"
                                val imageUrl = "${ds.child("imageUrl").value}"

                                val modelImageSelected =
                                    ModelImageSelected(id, null, imageUrl, true)
                                selectedImageArrayList.add(modelImageSelected)
                            }
                            loadImages()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private var marca = ""
    private var category = ""
    private var condition = ""
    private var direction = ""
    private var price = ""
    private var title = ""
    private var description = ""
    private var latitud = 0.0
    private var longitud = 0.0
    private fun validateDatta() {
        marca = binding.EtMarca.text.toString().trim()
        category = binding.Category.text.toString().trim()
        condition = binding.Condition.text.toString().trim()
        direction = binding.Location.text.toString().trim()
        price = binding.EtPrice.text.toString().trim()
        title = binding.EtTitle.text.toString().trim()
        description = binding.EtDescription.text.toString().trim()

        if (marca.isEmpty()) {
            binding.EtMarca.error = "Ingrese marca"
            binding.EtMarca.requestFocus()
        } else if (category.isEmpty()) {
            binding.Category.error = "Ingrese categoría"
            binding.Category.requestFocus()
        } else if (condition.isEmpty()) {
            binding.Condition.error = "Ingrese una Condición"
            binding.Condition.requestFocus()
        } else if (direction.isEmpty()) {
            binding.Location.error = "Ingrese una ubicación"
            binding.Location.requestFocus()
        } else if (price.isEmpty()) {
            binding.EtPrice.error = "Ingrese precio"
            binding.EtPrice.requestFocus()
        } else if (title.isEmpty()) {
            binding.EtTitle.error = "Ingrese un título"
            binding.EtTitle.requestFocus()
        } else if (description.isEmpty()) {
            binding.EtDescription.error = "Ingrese una descripción"
            binding.EtDescription.requestFocus()
        } else {
            if (Edition) {
                updateAnnounce()
                onBackPressedDispatcher.onBackPressed()
            } else {
                if (imageUri == null) {
                    Toast.makeText(this, "Debes al menos agregar una imagen", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    addAnnounce()
                }
            }
        }
    }

    private fun updateAnnounce() {
        progressDialog.setMessage("Actualizando")
        progressDialog.show()
        val hashMap = HashMap<String, Any>()
        hashMap["marca"] = "${marca}"
        hashMap["category"] = "${category}"
        hashMap["condition"] = "${condition}"
        hashMap["direction"] = "${direction}"
        hashMap["price"] = "${price}"
        hashMap["title"] = "${title}"
        hashMap["description"] = "${description}"
        hashMap["latitud"] = latitud
        hashMap["longitud"] = longitud

        val ref = FirebaseDatabase.getInstance().getReference("Announcements")
        ref.child(idAnuncioEdit)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                loadImagesStorage(idAnuncioEdit)
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private val selectLocation_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                if (data != null) {
                    latitud = data.getDoubleExtra("latitud", 0.0)
                    longitud = data.getDoubleExtra("longitud", 0.0)
                    direction = data.getStringExtra("direction") ?: ""

                    binding.Location.setText(direction)
                }
            } else {
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }
        }

    private fun addAnnounce() {
        progressDialog.setMessage("Publicando")
        progressDialog.show()

        val time = Constants.getTimeDis()

        val ref = FirebaseDatabase.getInstance().getReference("Announcements")
        val keyId = ref.push().key

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "${keyId}"
        hashMap["uid"] = "${firebaseAuth.uid}"
        hashMap["marca"] = "${marca}"
        hashMap["category"] = "${category}"
        hashMap["condition"] = "${condition}"
        hashMap["direction"] = "${direction}"
        hashMap["price"] = "${price}"
        hashMap["title"] = "${title}"
        hashMap["description"] = "${description}"
        hashMap["status"] = "${Constants.announcement_available}"
        hashMap["time"] = time
        hashMap["latitud"] = latitud
        hashMap["longitud"] = longitud
        hashMap["counterViews"] = 0

        ref.child(keyId!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                loadImagesStorage(keyId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadImagesStorage(keyId: String) {
        for (i in selectedImageArrayList.indices) {
            val modelImageSelect = selectedImageArrayList[i]

            if (!modelImageSelect.fromInternet) {
                val nameImage = modelImageSelect.id
                val routeNameImage = "Announcements/$nameImage"

                val storageReference = FirebaseStorage.getInstance().getReference(routeNameImage)
                storageReference.putFile(modelImageSelect.imageUri!!)
                    .addOnSuccessListener { taskSnapshot ->
                        val uriTask = taskSnapshot.storage.downloadUrl
                        while (!uriTask.isSuccessful);
                        val urlImgLoaded = uriTask.result

                        if (uriTask.isSuccessful) {
                            val hashMap = HashMap<String, Any>()
                            hashMap["id"] = "${modelImageSelect.id}"
                            hashMap["imageUrl"] = "$urlImgLoaded"

                            val ref = FirebaseDatabase.getInstance().getReference("Announcements")
                            ref.child(keyId).child("Images")
                                .child(nameImage)
                                .updateChildren(hashMap)
                        }

                        if (Edition) {
                            progressDialog.dismiss()
                            val intent = Intent(this@Create_new_ad, MainActivity::class.java)
                            startActivity(intent)
                            Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show()
                            finishAffinity()
                        } else {
                            progressDialog.dismiss()
                            Toast.makeText(this, "Publicado", Toast.LENGTH_SHORT).show()
                            clearFields()
                            onBackPressedDispatcher.onBackPressed()

                        }

                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun clearFields() {
        selectedImageArrayList.clear()
        adapterImageSelected.notifyDataSetChanged()
        binding.EtMarca.setText("")
        binding.Category.setText("")
        binding.Condition.setText("")
        binding.Location.setText("")
        binding.EtPrice.setText("")
        binding.EtTitle.setText("")
        binding.EtDescription.setText("")

    }

    private fun showOptions() {
        val popupMenu = PopupMenu(this, binding.AddImg)

        popupMenu.menu.add(Menu.NONE, 1, 1, "Cámara")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Galería")

        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { item ->
            val itemId = item.itemId
            if (itemId == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestCameraPermission.launch(arrayOf(android.Manifest.permission.CAMERA))
                } else {

                    requestCameraPermission.launch(
                        arrayOf(
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    )
                }
            } else if (itemId == 2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    galleryImage()
                } else {
                    requestStoragePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            true
        }
    }

    private val requestStoragePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            galleryImage()
        } else {
            Toast.makeText(this, "Permiso rechazado", Toast.LENGTH_SHORT).show()
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

                val time = "${Constants.getTimeDis()}"
                val modelImgSel = ModelImageSelected(
                    time, imageUri, null, false
                )
                selectedImageArrayList.add(modelImgSel)
                loadImages()

            }
        }


    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        var allAgree = true
        for (isAgree in result.values) {
            allAgree = allAgree && isAgree
        }
        if (allAgree) {
            cameraImage()
        } else {
            Toast.makeText(this, "Permiso rechazado", Toast.LENGTH_SHORT).show()
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
                val time = "${Constants.getTimeDis()}"
                val modelImgSel = ModelImageSelected(
                    time, imageUri, null, false
                )
                selectedImageArrayList.add(modelImgSel)
                loadImages()
            }
        }

    private fun loadImages() {
        adapterImageSelected = AdapterSelectedImage(this, selectedImageArrayList, idAnuncioEdit)
        binding.RVImages.adapter = adapterImageSelected
    }
}