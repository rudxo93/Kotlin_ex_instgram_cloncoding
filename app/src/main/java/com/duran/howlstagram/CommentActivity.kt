package com.duran.howlstagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duran.howlstagram.databinding.ActivityCommentBinding
import com.duran.howlstagram.databinding.ItemCommentBinding
import com.duran.howlstagram.model.AlarmModel
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

        binding.commentRecyclerview.adapter = CommentAdapter()
        binding.commentRecyclerview.layoutManager = LinearLayoutManager(this)

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
            // 댓글 send시 알람이벤트
            commentAlarm(dUid, binding.commentEdittext.text.toString())
            binding.commentEdittext.setText("")

        }

    }

    // 댓글 send시 알람이벤트
    fun commentAlarm(dUid: String, message: String) {
        var alarmModel = AlarmModel()
        alarmModel.destinationUid = dUid
        alarmModel.uid = auth.uid
        alarmModel.userId = auth.currentUser?.email
        alarmModel.kind = 1
        alarmModel.message = message
        alarmModel.timestamp = System.currentTimeMillis()

        // alarms컬렉션 안에 alarm모델을set해준다.
        firestore.collection("alarms").document().set(alarmModel)
    }

    inner class ItemCommentViewHolder(var binding: ItemCommentBinding): RecyclerView.ViewHolder(binding.root)
    inner class CommentAdapter: RecyclerView.Adapter<ItemCommentViewHolder>(){
        var comments = arrayListOf<ContentModel.Comment>()

        init {
            // Database의 images컬렉션에 dUid의 문서 접근 -> comments컬렉션을 실시간 감시
            firestore.collection("images").document(dUid).collection("comments").addSnapshotListener { value, error ->
                // comments 초기화
                comments.clear()

                // 값이 null이라면 다시 감시
                if(value == null) return@addSnapshotListener

                // item을 value의 문서만큼 반복문
                for(item in value.documents){
                    comments.add(item.toObject(ContentModel.Comment::class.java)!!)
                }
                // 리스트의 크기와 아이템이 둘 다 변경되는 경우 사용
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemCommentViewHolder {
            var view = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ItemCommentViewHolder(view)
        }

        override fun onBindViewHolder(holder: ItemCommentViewHolder, position: Int) {
            var view = holder.binding
            view.profileTextview.text = comments[position].userId
            view.messageTextview.text = comments[position].comment
        }

        override fun getItemCount(): Int {
            return comments.size
        }
    }

}