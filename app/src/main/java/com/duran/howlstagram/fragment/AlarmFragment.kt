package com.duran.howlstagram.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duran.howlstagram.CommentActivity
import com.duran.howlstagram.R
import com.duran.howlstagram.databinding.FragmentAlarmBinding
import com.duran.howlstagram.databinding.ItemCommentBinding
import com.duran.howlstagram.model.AlarmModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AlarmFragment: Fragment() {

    lateinit var binding: FragmentAlarmBinding
    lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_alarm, container, false)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.alarmRecyclerview.adapter = AlarmAdapter()
        binding.alarmRecyclerview.layoutManager = LinearLayoutManager(activity)

        return binding.root
    }

    inner class ItemCommentViewHolder(var binding : ItemCommentBinding) : RecyclerView.ViewHolder(binding.root)
    inner class AlarmAdapter : RecyclerView.Adapter<ItemCommentViewHolder>(){

        var alarmList = arrayListOf<AlarmModel>()

        init {
            var uid = auth.uid
            // alarms컬렉션의 destinationUid필드와 uid와 같은지 비교 후 감시
            firestore.collection("alarms").whereEqualTo("destinationUid",uid).addSnapshotListener { value, error ->
                alarmList.clear()
                for (item in value!!.documents){
                    alarmList.add(item.toObject(AlarmModel::class.java)!!)
                }
                notifyDataSetChanged()
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemCommentViewHolder {
            var view = ItemCommentBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            return ItemCommentViewHolder(view)
        }

        override fun onBindViewHolder(holder: ItemCommentViewHolder, position: Int) {
            var view = holder.binding
            var alarmModel = alarmList[position]

            // 메세지 텍스트뷰가 보이지 않게
            view.messageTextview.visibility = View.INVISIBLE
            // 알람 모델에서 분류구분에 따라 알람메세지 내용
            when(alarmModel.kind){
                // 알람 분류구분이 0이라면 -> 좋아요
                0 ->{
                    var m = alarmModel.userId + "가 좋아요를 눌렀습니다."
                    // 좋아요 알람 메세지 text
                    view.profileTextview.text = m
                }
                // 알람 분류구분이 0이라면 -> 코멘트
                1 ->{
                    var m_1 = alarmModel.userId +"가" + alarmModel.message + "라는 메세지를 남겼습니다."
                    // 댓글 알람 메세지 text
                    view.profileTextview.text = m_1
                }
                // 알람 분류구분이 0이라면 -> 팔로우
                2 ->{
                    var m_2 = alarmModel.userId +"가 나를 팔로우 하기 시작했습니다."
                    // 팔로우 알람 메세지 text
                    view.profileTextview.text = m_2
                }
            }
        }

        override fun getItemCount(): Int {
            return alarmList.size
        }
    }
}