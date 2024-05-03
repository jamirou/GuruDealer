package com.jamirodev.myline.DetailAnnounce

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jamirodev.myline.Constants
import com.jamirodev.myline.MainActivity
import com.jamirodev.myline.Model.ModelAnnounce
import com.jamirodev.myline.Model.ModelImgSlider
import com.jamirodev.myline.R
import com.jamirodev.myline.adapters.AdapterImgSlider
import com.jamirodev.myline.advertisements.Create_new_ad
import com.jamirodev.myline.chat.ChatActivity
import com.jamirodev.myline.databinding.ActivityDetailAnnonceBinding
import com.jamirodev.myline.detail_seller.DetailSellerActivity

class DetailAnnonce : AppCompatActivity() {

    private lateinit var binding: ActivityDetailAnnonceBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var idAnnounce = ""

    private var announceLatitude = 0.0
    private var announceLongitude = 0.0

    private var uidSeller = ""
    private var phoneSeller = ""

    private var favorite = false

    private lateinit var imageSliderArrayList: ArrayList<ModelImgSlider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailAnnonceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.IbEdit.visibility = View.GONE
        binding.IbDelete.visibility = View.GONE
        binding.BtnMap.visibility = View.GONE
        binding.BtnCall.visibility = View.GONE
        binding.BtnChat.visibility = View.GONE

        firebaseAuth = FirebaseAuth.getInstance()
        idAnnounce = intent.getStringExtra("idAnnounce").toString()

        Constants.increaseViews(idAnnounce)

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        checkFavAnnounce()
        loadInfoAnnounce()
        loadImgAnnounce()
        binding.IbEdit.setOnClickListener {
            optionsDialog()
        }
        binding.IbFav.setOnClickListener {
            if (favorite) {
                Constants.deleteFavorite(this, idAnnounce)
            } else {
                Constants.addAdFavorite(this, idAnnounce)
            }
        }
        binding.IbDelete.setOnClickListener {
            val myAlertDialog = MaterialAlertDialogBuilder(this)
            myAlertDialog.setTitle("Eliminar anuncio")
                .setMessage("¿Quieres eliminar el anuncio?")
                .setPositiveButton("Eliminar") { _, _ ->
                    deleteAnnounce()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }.show()
        }
        binding.BtnMap.setOnClickListener {
            Log.d(
                "DetailAnnonce",
                "announceLatitude: $announceLatitude, announceLongitude: $announceLongitude"
            )
            Constants.mapIntent(this, announceLatitude, announceLongitude)
        }
        binding.BtnCall.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val numTel = phoneSeller
                if (numTel.isEmpty()) {
                    Toast.makeText(
                        this@DetailAnnonce,
                        "Necesitas un número de teléfono",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Constants.callIntent(this, numTel)
                }
            } else {
                callPermission.launch(Manifest.permission.CALL_PHONE)
            }
        }

        binding.BtnChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("uidSeller", uidSeller)
            startActivity(intent)
        }

        binding.RLUserInfo.setOnClickListener {
            val intent = Intent(this@DetailAnnonce, DetailSellerActivity::class.java)
            intent.putExtra("uidSeller", uidSeller)
            startActivity(intent)
        }
    }

    private fun optionsDialog() {
        val popupMenu = PopupMenu(this, binding.IbEdit)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Editar")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Vendido")

        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { item ->
            val itemId = item.itemId
            if (itemId == 0) {
                val intent = Intent(this, Create_new_ad::class.java)
                intent.putExtra("Edition", true)
                intent.putExtra("idAnnounce", idAnnounce)
                startActivity(intent)
            } else if (itemId == 1) {
                dialogCheckAsSold()
            }

            return@setOnMenuItemClickListener true
        }
    }

    private fun loadInfoAnnounce() {
        var ref = FirebaseDatabase.getInstance().getReference("Announcements")
        ref.child(idAnnounce)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val modelAnnounce = snapshot.getValue(ModelAnnounce::class.java)

                        uidSeller = modelAnnounce!!.uid
                        val title = modelAnnounce.title
                        val description = modelAnnounce.description
                        val direction = modelAnnounce.direction
                        val condition = modelAnnounce.condition
                        val category = modelAnnounce.category
                        val price = modelAnnounce.price
                        val status = modelAnnounce.status
                        val view = modelAnnounce.counterViews
                        announceLatitude = modelAnnounce.latitud
                        announceLongitude = modelAnnounce.longitud
                        val time = modelAnnounce.time

                        val formatDate = Constants.getDate(time)

                        if (uidSeller == firebaseAuth.uid) {
                            binding.IbEdit.visibility = View.VISIBLE
                            binding.IbDelete.visibility = View.VISIBLE

                            binding.BtnMap.visibility = View.GONE
                            binding.BtnCall.visibility = View.GONE
                            binding.BtnChat.visibility = View.GONE

                        } else {
                            binding.IbEdit.visibility = View.GONE
                            binding.IbDelete.visibility = View.GONE

                            binding.BtnMap.visibility = View.VISIBLE
                            binding.BtnCall.visibility = View.VISIBLE
                            binding.BtnChat.visibility = View.VISIBLE
                        }
                        binding.TvTitle.text = title
                        binding.TvDescr.text = description
                        binding.TvDirection.text = direction
                        binding.TvCondition.text = condition
                        binding.TvCat.text = category
                        binding.TvPrice.text = price
                        binding.TvStatus.text = status
                        binding.TvDate.text = formatDate
                        binding.TvView.text = view.toString()

                        if (status == "Disponible") {
                            binding.TvStatus.setTextColor(
                                ContextCompat.getColor(
                                    this@DetailAnnonce,
                                    R.color.navi5
                                )
                            )
                            binding.TvStatus.setTextColor(
                                ContextCompat.getColor(
                                    this@DetailAnnonce,
                                    R.color.navi5
                                )
                            )
                        } else {
                            binding.TvStatus.setTextColor(
                                ContextCompat.getColor(
                                    this@DetailAnnonce,
                                    R.color.navi1
                                )
                            )

                        }
                        loadInfoSeller()
                    } catch (_: Exception) {
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun checkAnnounceAsSold() {
        val hashMap = HashMap<String, Any>()
        hashMap["status"] = Constants.announcement_sold
        val ref = FirebaseDatabase.getInstance().getReference("Announcements")
        ref.child(idAnnounce)
            .updateChildren(hashMap)
            .addOnSuccessListener { }
            .addOnFailureListener { e ->
                Toast.makeText(this, "!!!! ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun dialogCheckAsSold() {
        val Btn_yes: MaterialButton
        val Btn_no: MaterialButton
        val dialog = Dialog(this)

        dialog.setContentView(R.layout.dialog_box_mark_as_sold)

        Btn_yes = dialog.findViewById(R.id.Btn_yes)
        Btn_no = dialog.findViewById(R.id.Btn_no)

        Btn_yes.setOnClickListener {
            checkAnnounceAsSold()
            dialog.dismiss()
        }
        Btn_no.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
        dialog.setCanceledOnTouchOutside(false)

    }

    private fun loadInfoSeller() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uidSeller)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val phone = "${snapshot.child("phone").value}"
                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val names = "${snapshot.child("names").value}"
                    val profileImage = "${snapshot.child("urlProfileImage").value}"
                    val email = "${snapshot.child("email").value}"
                    val timeRegister = snapshot.child("time").value as Long

                    val formatDate = Constants.getDate(timeRegister)

                    phoneSeller = "$phoneCode$phone"
                    binding.ItemEmail.text = email
                    binding.TVNames.text = names
                    binding.TvMember.text = formatDate

                    try {
                        Glide.with(this@DetailAnnonce)
                            .load(profileImage)
                            .placeholder(R.drawable.usuario)
                            .into(binding.imgProfile)
                    } catch (_: Exception) {
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadImgAnnounce() {
        imageSliderArrayList = ArrayList()

//        WARNING I'M NOT SURE ABOUT THE REFERENCE, COULD BE User INSTEAD OF Announcements
        val ref = FirebaseDatabase.getInstance().getReference("Announcements")
        ref.child(idAnnounce).child("Images")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    imageSliderArrayList.clear()
                    for (ds in snapshot.children) {
                        try {
                            val modelImgSlide = ds.getValue(ModelImgSlider::class.java)
                            imageSliderArrayList.add(modelImgSlide!!)
                        } catch (_: Exception) {
                        }
                    }
                    val adapterImageSlider =
                        AdapterImgSlider(this@DetailAnnonce, imageSliderArrayList)
                    binding.ImageSliderVP.adapter = adapterImageSlider

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun checkFavAnnounce() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}").child("Favorites").child(idAnnounce)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    favorite = snapshot.exists()
                    if (favorite) {
                        binding.IbFav.setImageResource(R.drawable.ic_favorite)
                    } else {
                        binding.IbFav.setImageResource(R.drawable.ic_no_fav)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun deleteAnnounce() {
        val ref = FirebaseDatabase.getInstance().getReference("Announcements")
        ref.child(idAnnounce)
            .removeValue()
            .addOnSuccessListener {
                startActivity(Intent(this@DetailAnnonce, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private val callPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { allow ->
            if (allow) {
                val numTel = phoneSeller
                if (numTel.isEmpty()) {
                    Toast.makeText(
                        this@DetailAnnonce,
                        "No hay ningún número asignado.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Constants.callIntent(this, numTel)
                }
            } else {
                Toast.makeText(
                    this@DetailAnnonce,
                    "Habilitar permiso para llamar",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
}









