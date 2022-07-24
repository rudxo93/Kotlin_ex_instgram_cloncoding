package com.duran.howlstagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.duran.howlstagram.databinding.ActivityCommentBinding
import com.duran.howlstagram.model.ContentModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommentActivity : AppCompatActivity() {

    lateinit var binding: ActivityCommentBinding
    lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
    lateinit var dUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_comment)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        // dUid : 컬렉션에 아이템 이름 -> dUid명으로 넘어온 intent안에 images컬렉션 안에 들어있는 아이템의 이름이 담겨있다.
        dUid = intent.getStringExtra("dUid")!!

        // send버튼 클릭 시
        binding.sendBtn.setOnClickListener {
            // Model의 comment에 set해준다.
            var comment = ContentModel.Comment()
            comment.uid = auth.currentUser?.uid // uid값
            comment.userId = auth.currentUser?.email // 댓글을 다는 계정 이메일
            comment.comment = binding.commentEdittext.text.toString() // 댓글 string
            comment.timestamp = System.currentTimeMillis() // 댓글 send날짜

            // images컬렉션에 현재 dUid의 문서에 접근 -> comments컬렉션 생성 후 comment를 set해준다.
            firestore.collection("images").document(dUid).collection("comments").document().set(comment)

            binding.commentEdittext.setText("")

        }
    }

}