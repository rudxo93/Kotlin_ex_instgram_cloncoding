package com.duran.howlstagram.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.duran.howlstagram.R
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

    lateinit var contentUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        contentUid = intent.getStringExtra("contentUid")!!

        commentBtnSend.setOnClickListener {
            var comment = ContentDTO.comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = commentEditMassage.text.toString()
            comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images").document(contentUid!!).collection("comments").document().set(comment)

            commentEditMassage.setText("")
        }

    }
}