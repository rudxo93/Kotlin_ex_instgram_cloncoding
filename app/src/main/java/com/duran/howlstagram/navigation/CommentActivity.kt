package com.duran.howlstagram.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.duran.howlstagram.R
import com.duran.howlstagram.navigation.model.AlarmDTO
import com.duran.howlstagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class CommentActivity : AppCompatActivity() {

    private val commentBtnSend: Button by lazy {
        findViewById(R.id.comment_btn_send)
    }
    private val commentEditMassage: EditText by lazy {
        findViewById(R.id.comment_edit_message)
    }
    private val commentRecyclerView: RecyclerView by lazy {
        findViewById(R.id.comment_recyclerview)
    }

    lateinit var contentUid: String
    lateinit var destinationUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        contentUid = intent.getStringExtra("contentUid")!!
        destinationUid = intent.getStringExtra("destinationUid")!!

        commentRecyclerView.adapter = CommentrecyclerviewAdapter()
        commentRecyclerView.layoutManager = LinearLayoutManager(this)

        commentBtnSend.setOnClickListener {
            var comment = ContentDTO.comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = commentEditMassage.text.toString()
            comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images").document(contentUid!!).collection("comments").document().set(comment)
            commentAlarm(destinationUid, commentEditMassage.text.toString())
            commentEditMassage.setText("")
        }
    }

    fun commentAlarm(destinationUid: String, message: String){
        val alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
        alarmDTO.kind = 1
        alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
        alarmDTO.timestamp = System.currentTimeMillis()
        alarmDTO.message = message
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)
    }

    inner class CommentrecyclerviewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        val comments: ArrayList<ContentDTO.comment> = arrayListOf()
        init {
            FirebaseFirestore.getInstance().collection("images").document(contentUid).collection("comments").orderBy("tiemstamp").addSnapshotListener { value, error ->
                comments.clear()
                if(value == null) return@addSnapshotListener

                for (snapshot in value.documents){
                    comments.add(snapshot.toObject(ContentDTO.comment::class.java)!!)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)

            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View): RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return comments.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val view = holder.itemView
            view.findViewById<TextView>(R.id.commentviewitem_textview_comment).text = comments[position].comment
            view.findViewById<TextView>(R.id.commentviewitem_textview_profile).text = comments[position].userId

            FirebaseFirestore.getInstance().collection("profileImages").document(comments[position].uid!!).get().addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val url = task.result["image"]
                    Glide.with(holder.itemView.context).load(url).apply(RequestOptions().circleCrop()).into(view.findViewById(R.id.commentviewitem_imageview_profile))
                }
            }
        }
    }
}