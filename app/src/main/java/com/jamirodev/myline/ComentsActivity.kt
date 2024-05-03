package com.jamirodev.myline

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jamirodev.myline.Model.ModelComment
import com.jamirodev.myline.adapters.AdapterComment
import com.jamirodev.myline.databinding.ActivityComentsBinding
import com.jamirodev.myline.databinding.DialogBAddCommentBinding

class ComentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComentsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var uidSeller = ""

    private lateinit var commentArrayList: ArrayList<ModelComment>
    private lateinit var adapterComment: AdapterComment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityComentsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        uidSeller = intent.getStringExtra("uidSeller").toString()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Subiendo comentario")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.IbBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.RLAddComment.setOnClickListener {
            dialogComment()
        }
        binding.TxtAddComment.setOnClickListener {
            dialogComment()
        }
        listComments()
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
                    adapterComment = AdapterComment(this@ComentsActivity, commentArrayList)
                    binding.RVCommentsAddComment.adapter = adapterComment
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private var comment = ""
    private fun dialogComment() {
        val add_comment_binding = DialogBAddCommentBinding.inflate(LayoutInflater.from(this))

        val builder = AlertDialog.Builder(this)
        builder.setView(add_comment_binding.root)
        val alertDialog = builder.create()
        alertDialog.show()
        alertDialog.setCanceledOnTouchOutside(false)
        add_comment_binding.IbClose.setOnClickListener { alertDialog.dismiss() }
        add_comment_binding.BtnComment.setOnClickListener {
            comment = add_comment_binding.EtAddComment.text.toString()
            if (comment.isEmpty()) {
                Toast.makeText(this, "Agrega un comentario", Toast.LENGTH_SHORT).show()
            } else {
                alertDialog.dismiss()
                addComment()
            }
        }
    }

    private fun addComment() {
        progressDialog.setMessage("Comentando")
        progressDialog.show()

        val time = "${Constants.getTimeDis()}"
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "${time}"
        hashMap["time"] = "${time}"
        hashMap["uid"] = "${firebaseAuth.uid}"  //User visitor UId
        hashMap["uid_seller"] = uidSeller
        hashMap["comment"] = "${comment}"

        val ref = FirebaseDatabase.getInstance().getReference("SellerComments")
        ref.child(uidSeller).child("Comments").child(time)
            .setValue(hashMap)
            .addOnSuccessListener { progressDialog.dismiss() }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    
}







