package com.jamirodev.myline.detail_seller

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.auth.User
import com.jamirodev.myline.ComentsActivity
import com.jamirodev.myline.Constants
import com.jamirodev.myline.Model.ModelAnnounce
import com.jamirodev.myline.Model.ModelComment
import com.jamirodev.myline.R
import com.jamirodev.myline.adapters.AdapterAnnounce
import com.jamirodev.myline.adapters.AdapterComment
import com.jamirodev.myline.databinding.ActivityDetailSellerBinding

class DetailSellerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailSellerBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var uidSeller = ""

    private lateinit var commentArrayList: ArrayList<ModelComment>
    private lateinit var adapterComment: AdapterComment

    private var adView_DetailSeller: AdView? = null
    private var my_interstitial: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailSellerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        uidSeller = intent.getStringExtra("uidSeller").toString()
        firebaseAuth = FirebaseAuth.getInstance()
        loadInfoSeller()
        loadAnnouncesSeller()
        startSDKBanner()
        startSdkInterstitial()

        binding.IbRegresar.setOnClickListener {
            visualizeInterstitial()
            onBackPressedDispatcher.onBackPressed()
        }

        binding.IvComents.setOnClickListener {
            val intent = Intent(this, ComentsActivity::class.java)
            intent.putExtra("uidSeller", uidSeller)
            startActivity(intent)
        }

        binding.RLUserInfo.setOnClickListener {
            val intent = Intent(this, ComentsActivity::class.java)
            intent.putExtra("uidSeller", uidSeller)
            startActivity(intent)
        }
        listComments()

        binding.IvSeller.setOnClickListener {
            getImage()
        }
    }

    private fun getImage() {
        val ref = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(uidSeller)
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val image = "${snapshot.child("urlProfileImage").value}"
                imageViewer(image)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }


    private fun listComments() {
        commentArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("SellerComments")
        ref.child(uidSeller).child("Comments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    commentArrayList.clear()
                    for (ds in snapshot.children) {
                        val model = ds.getValue(ModelComment::class.java)
                        commentArrayList.add(model!!)
                    }
                    adapterComment = AdapterComment(this@DetailSellerActivity, commentArrayList)
                    binding.RVCommentsAddComment.adapter = adapterComment
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun loadAnnouncesSeller() {
        val announceArrayList: ArrayList<ModelAnnounce> = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Announcements")
        ref.orderByChild("uid").equalTo(uidSeller)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    announceArrayList.clear()
                    for (ds in snapshot.children) {
                        try {
                            val modelAnnounce = ds.getValue(ModelAnnounce::class.java)
                            announceArrayList.add(modelAnnounce!!)
                        } catch (e: Exception) {
                        }
                    }
                    val adapter = AdapterAnnounce(this@DetailSellerActivity, announceArrayList)
                    binding.AnnouncesRV.adapter = adapter

                    val counterAnnounces = "${announceArrayList.size}"
                    binding.TvNumAnnounces.text = counterAnnounces
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun loadInfoSeller() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uidSeller)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val names = "${snapshot.child("names").value}"
                    val image = "${snapshot.child("urlProfileImage").value}"
                    val email = "${snapshot.child("email").value}"
                    val time = snapshot.child("time").value as Long
                    val f_date = Constants.getDate(time)

                    binding.TVNames.text = names
                    binding.TvMember.text = f_date
                    binding.ItemEmail.text = email

                    try {
                        Glide.with(this@DetailSellerActivity)
                            .load(image)
                            .placeholder(R.drawable.usuario)
                            .into(binding.IvSeller)
                    } catch (e: Exception) {

                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun startSDKBanner() {
        adView_DetailSeller = findViewById(R.id.adViewDetailSeller)
        MobileAds.initialize(this) {
        }
        val adRequest = AdRequest.Builder().build()
        adView_DetailSeller?.loadAd(adRequest)
    }

    private fun startSdkInterstitial() {
        MobileAds.initialize(this) {

        }
        loadInterstitial()
    }

    private fun loadInterstitial() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, getString(R.string.ID_Test_DetailSellerInterstitial), adRequest,
            object : InterstitialAdLoadCallback() {
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
        if (my_interstitial != null) {
            my_interstitial!!.setFullScreenContentCallback(object: FullScreenContentCallback() {
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

    private fun imageViewer(image: String) {
        val Pv: PhotoView
        val Btn_close: MaterialButton
        val dialog = Dialog(this@DetailSellerActivity)
        dialog.setContentView(R.layout.dialog_box_img_viewer)
        Pv = dialog.findViewById(R.id.PV_image)
        Btn_close = dialog.findViewById(R.id.Btn_close_viewer)


        try {
            Glide.with(applicationContext)
                .load(image)
                .placeholder(R.drawable.usuario)
                .into(Pv)
        }catch (e:Exception) { }

        Btn_close.setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.setCanceledOnTouchOutside(false)
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
        adView_DetailSeller?.resume()
        updateStatus("conectado")
    }

    override fun onPause() {
        super.onPause()
        adView_DetailSeller?.pause()
        updateStatus("desconectado")
    }

    override fun onDestroy() {
        super.onDestroy()
        adView_DetailSeller?.destroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        onBackPressedDispatcher.onBackPressed()
        visualizeInterstitial()
    }
}