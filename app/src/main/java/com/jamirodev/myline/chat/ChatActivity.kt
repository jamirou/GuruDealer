package com.jamirodev.myline.chat

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.jamirodev.myline.Constants
import com.jamirodev.myline.Model.ModelChat
import com.jamirodev.myline.R
import com.jamirodev.myline.adapters.AdapterChat
import com.jamirodev.myline.databinding.ActivityChatBinding
import com.jamirodev.myline.detail_seller.DetailSellerActivity
import com.jamirodev.myline.detail_seller.ProfileViewActivity
import org.json.JSONObject

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    private var uidSeller = "" //Receiver Uid
    private var myUid = "" //Sender Uid
    private var miName = ""
    private var receivedToken = ""

    private var chatRoute = ""
    private var imageUri: Uri? = null

    private var my_interstitial: InterstitialAd?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Enviando mensaje")
        progressDialog.setCanceledOnTouchOutside(false)

        uidSeller = intent.getStringExtra("uidSeller")!!
        myUid = firebaseAuth.uid!!
        chatRoute = Constants.routeChat(uidSeller, myUid)

        loadMyInfo()
        loadInfoSeller()
        loadMessages()
        startSdkInterstitial()

        val menuButton: ImageButton = findViewById(R.id.menuButton)
        menuButton.setOnClickListener {
            val popupMenu = PopupMenu(this@ChatActivity, menuButton)
            val inflater: MenuInflater = popupMenu.menuInflater
            inflater.inflate(R.menu.menu_go_to_profile, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_text_profile -> {
                        val intent = Intent(applicationContext, ProfileViewActivity::class.java)
                        startActivity(intent)
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        }

        binding.icBack.setOnClickListener {
            visualizeInterstitial()
            onBackPressedDispatcher.onBackPressed()
        }
        binding.AttachFAB.setOnClickListener {
            selectImgDialog()
        }
        binding.SendFAB.setOnClickListener {
            validateInfo()
        }
        binding.menuButton.setOnClickListener {
            val intent = Intent(this@ChatActivity, DetailSellerActivity::class.java)
            intent.putExtra("uidSeller", uidSeller)
            startActivity(intent)
        }
    }


    private fun loadMyInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    miName = "${snapshot.child("names").value}"
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }


    private fun loadMessages() {
        val messageArrayList = ArrayList<ModelChat>()
        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.child(chatRoute)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageArrayList.clear()
                    for (ds: DataSnapshot in snapshot.children) {
                        try {
                            val modelChat = ds.getValue(ModelChat::class.java)
                            messageArrayList.add(modelChat!!)
                        } catch (e: Exception) {
                        }
                    }
                    val adapterChat = AdapterChat(this@ChatActivity, messageArrayList)
                    binding.ChatsRV.adapter = adapterChat

                    binding.ChatsRV.setHasFixedSize(true)
                    var linearLayoutManager = LinearLayoutManager(this@ChatActivity)
                    linearLayoutManager.stackFromEnd = true
                    binding.ChatsRV.layoutManager = linearLayoutManager
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun validateInfo() {
        val message = binding.EtMessageChat.text.toString().trim()
        val time = Constants.getTimeDis()

        if (message.isEmpty()) {
            Toast.makeText(this, "Adjunte mensaje", Toast.LENGTH_SHORT).show()
        } else {
            sendMessage(Constants.MESSAGE_TYPE_TEXT, message, time)

        }
    }

    private fun loadInfoSeller() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uidSeller)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val names = "${snapshot.child("names").value}"
                        val image = "${snapshot.child("urlProfileImage").value}"
                        val status = "${snapshot.child("estado").value}"
                        receivedToken = "${snapshot.child("fcmToken").value}"


                        binding.TxtSellerNameChat.text = names
                        binding.TxtChatStatus.text = status
                        try {
                            Glide.with(applicationContext)
                                .load(image)
                                .placeholder(R.drawable.usuario)
                                .into(binding.toolbarIv)
                        } catch (e: Exception) {
                        }
                    } catch (e: Exception) {
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun selectImgDialog() {
        val popupMenu = PopupMenu(this, binding.AttachFAB)
        popupMenu.menu.add(Menu.NONE, 1, 1, "Cámara")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Galería")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem ->
            val itemId = menuItem.itemId
            if (itemId == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    allowCameraPermission.launch(arrayOf(android.Manifest.permission.CAMERA))
                } else {
                    allowCameraPermission.launch(
                        arrayOf(
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    )
                }
            } else if (itemId == 2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    imageGalley()
                } else {
                    allowStoragePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
            true
        }
    }

    private fun imageGalley() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultGaley_ARL.launch(intent)
    }

    private val resultGaley_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                imageUri = data!!.data
                uploadImgStorage()
            }
        }

    private val allowStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                imageGalley()
            } else {
                Toast.makeText(this, "Permiso rechazado", Toast.LENGTH_SHORT).show()
            }
        }

    private fun openCamera() {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Title_image")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Description_image")

        imageUri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        resultCamera_ARL.launch(intent)
    }

    private val resultCamera_ARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                uploadImgStorage()
            }
        }

    private val allowCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            var grantedAll = true
            for (isGranted in result.values) {
                grantedAll = grantedAll && isGranted
            }
            if (grantedAll) {
                openCamera()
            } else {
                Toast.makeText(this, "Permiso rechazado", Toast.LENGTH_SHORT).show()
            }
        }

    private fun uploadImgStorage() {
        progressDialog.setMessage("Subiendo imagen...")
        progressDialog.show()

        val time = Constants.getTimeDis()
        val nameRouteImg = "ImagesChat/$time"

        val storageRef = FirebaseStorage.getInstance().getReference(nameRouteImg)
        storageRef.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);

                val urlImage = uriTask.result.toString()
                if (uriTask.isSuccessful) {
                    sendMessage(Constants.MESSAGE_TYPE_IMAGE, urlImage, time)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendMessage(typeMessage: String, message: String, time: Long) {
        progressDialog.setMessage("Enviando...")
        progressDialog.show()

        val refChat = FirebaseDatabase.getInstance().getReference("Chats")
        val keyId = "${refChat.push().key}"
        val hashMap = HashMap<String, Any>()

        hashMap["id_message"] = "$keyId"  //idMessage
        hashMap["typeMessage"] = "$typeMessage"
        hashMap["message"] = "$message"
        hashMap["senderUid"] = "$myUid"
        hashMap["receiverUid"] = "$uidSeller"
        hashMap["time"] = time

        refChat.child(chatRoute)
            .child(keyId)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                binding.EtMessageChat.setText("")

                if (typeMessage == Constants.MESSAGE_TYPE_TEXT) {
                    prepareNotification(message)
                } else {
                    prepareNotification("Imagen")
                }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "ERROR. No se ha logrado enviar mensaje", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun prepareNotification(message: String) {
        val notificationJo = JSONObject()
        val notificationDataJo = JSONObject()
        val notificationNotificationJo = JSONObject()
        try {
            notificationDataJo.put("notificationType", "${Constants.NOTIFICATION_NEW_MESSAGE}")
            notificationDataJo.put("senderUid", "${firebaseAuth.uid}")
            notificationNotificationJo.put("title", "$miName")
            notificationNotificationJo.put("body", "$message")
            notificationNotificationJo.put("sound", "default")
            notificationJo.put("to", "$receivedToken")
            notificationJo.put("notification", notificationNotificationJo)
            notificationJo.put("data", notificationDataJo)
        } catch (e: Exception) {
        }
        sendNotification(notificationJo)

    }

    private fun sendNotification(notificationJo: JSONObject) {
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            "https://fcm.googleapis.com/fcm/send",
            notificationJo,
            Response.Listener {

            },
            Response.ErrorListener { e ->

            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["Authorization"] = "key=${Constants.FCM_SERVER_KEY}"
                return headers
            }
        }
        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }


    private fun startSdkInterstitial() {
        MobileAds.initialize(this) {

        }
        loadInterstitial()
    }
    private fun loadInterstitial() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, getString(R.string.ID_Test_InterstitialChats), adRequest,
            object : InterstitialAdLoadCallback(){
                override fun onAdLoaded(interstitial: InterstitialAd) {
                    super.onAdLoaded(interstitial)
                    my_interstitial = interstitial
                }
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    my_interstitial = null
                }

            })
    }

    private fun visualizeInterstitial() {
        if(my_interstitial != null) {
            my_interstitial!!.setFullScreenContentCallback(object : FullScreenContentCallback(){
                override fun onAdClicked() {
                    super.onAdClicked()
                }

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    my_interstitial = null
                    loadInterstitial()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    super.onAdFailedToShowFullScreenContent(adError)
                    my_interstitial = null
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                }
            })
            my_interstitial!!.show(this)
        }
    }

    private fun updateStatus(status: String) {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            val ref =
                FirebaseDatabase.getInstance().reference.child("Users").child(firebaseAuth.uid!!)
            val hashMap = HashMap<String, Any>()
            hashMap["estado"] = status
            ref!!.updateChildren(hashMap)
        }
    }
        override fun onResume() {
        super.onResume()
        updateStatus("conectado")
    }
    override fun onPause() {
        super.onPause()
        updateStatus("desconectado")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        onBackPressedDispatcher.onBackPressed()
        visualizeInterstitial()
    }

}

