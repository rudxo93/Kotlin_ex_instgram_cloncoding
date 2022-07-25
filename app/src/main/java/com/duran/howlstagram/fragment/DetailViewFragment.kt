package com.duran.howlstagram.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.duran.howlstagram.CommentActivity
import com.duran.howlstagram.R
import com.duran.howlstagram.databinding.FragmentDetailViewBinding
import com.duran.howlstagram.databinding.ItemDetailBinding
import com.duran.howlstagram.model.AlarmModel
import com.duran.howlstagram.model.ContentModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class DetailViewFragment : Fragment() {

    lateinit var binding: FragmentDetailViewBinding
    lateinit var firestore: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var uid: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail_view, container, false)
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().uid!!
        auth = FirebaseAuth.getInstance()

        // recyclerview에
        binding.detailviewRecyclerveiw.adapter = DetailviewRecyclerviewAdapter()
        binding.detailviewRecyclerveiw.layoutManager = LinearLayoutManager(activity)

        return binding.root
    }

    inner class DetailViewHolder(var binding: ItemDetailBinding): RecyclerView.ViewHolder(binding.root)
    inner class DetailviewRecyclerviewAdapter(): RecyclerView.Adapter<DetailViewHolder>(){

        // content들을 배열로 가져온다.
        val contentModels = arrayListOf<ContentModel>()
        val contentUidsList = arrayListOf<String>()

        init {
            // 데이터를 계속 지켜보는 것을 Snapshot -> 리소스를 많이 사용 -> 서버 비용 증가 -> 비용줄이면서
            // 한번만 데이터를 읽어 드리는것을 Get
            // DB에 접근해서 데이터를 받아오는 쿼리 작성(시간순으로 받아온다.)
            firestore.collection("images").orderBy("timestamp").addSnapshotListener { value, error ->
                // 받자마자 값 초기화
                contentModels.clear()
                contentUidsList.clear()
                for (item in value!!.documentChanges){
                    val contentModel = item.document.toObject(ContentModel::class.java)
                    contentModels.add(contentModel)
                    contentUidsList.add(item.document.id)

                }
                // 값이 새로고침 됨
                notifyDataSetChanged()
            }
            //데이터를 불러오는 코드를 넣어주도록
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
            //행 하나에 어떤 디자인의 XML 넣을지 설정해는 코드
            val view = ItemDetailBinding.inflate(LayoutInflater.from(parent.context),parent,false)

            return DetailViewHolder(view)
        }

        // 서버에서 넘어온 데이터들을 맵핑시켜주는 부분
        override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
            // 데이터 바인딩
            val contentModel = contentModels[position]
            val viewHolder = holder.binding

            // UserId
            viewHolder.profileTextview.text = contentModel.userId
            // likes
            viewHolder.likeTextview.text = "Likes " + contentModel.favoriteCount
            //Explain of content
            viewHolder.explainTextview.text = contentModel.explain
            // Image
            Glide.with(holder.itemView.context).load(contentModels[position].imageUrl).into(viewHolder.contentImageview)

            // 좋아요 버튼 클릭 시
            viewHolder.favoriteImageview.setOnClickListener {
                // 좋아요 기능 구현
                eventFavorite(position)
            }

            // 상대방 유저페이지 이동
            viewHolder.profileImageview.setOnClickListener {
                val fragment = UserFragment()
                val bundle = Bundle()
                // Fragment -> Activity 데이터 가지고 이동
                bundle.putString("dUid", contentModel.uid) // dUid로 uid값
                bundle.putString("userId", contentModel.userId) // userId로 userId값
                // 데이터를 가지고 UserFragment로 이동
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content, fragment)?.commit()
            }

            // 코멘트로 이동 로직
           viewHolder.commentImageview.setOnClickListener {
               var intent = Intent(activity, CommentActivity::class.java)
               // images 컬렉션 안에 아이템 이름을 넘겨준다. -> dUid : 컬렉션에 아이템 이름
               intent.putExtra("dUid", contentUidsList[position]) // jAZgDwXRnZwH3DjZo2hy
               startActivity(intent)
           }

        }

        // 좋아요를 하게되면 알람모델에 정보들을 set
        fun favorteAlarm(dUid : String){
            var alarmModel = AlarmModel()
            alarmModel.destinationUid = dUid
            alarmModel.userId = auth.currentUser?.email
            alarmModel.uid = auth.uid
            alarmModel.kind = 0
            alarmModel.timestamp = System.currentTimeMillis()

            // alarms컬렉션에 alarm모델을 set해분다.
            firestore.collection("alarms").document().set(alarmModel)

        }

        override fun getItemCount(): Int {
            return contentModels.size
        }

        // 좋아요 기능 구현
        fun eventFavorite(position: Int) {
            // 선택한 컨텐츠의 uid값
            var docId = contentUidsList[position]
            // 내가 선택한 컨텐츠의 uid를 받아와서 좋아요 기능 구현
            var tsDoc = firestore.collection("images").document(docId)
            // 데이터 입력하기위해 transaction을 불러와야한다.
            firestore.runTransaction {
                transition ->
                // contentModel로 캐스팅
                var contentModel = transition.get(tsDoc).toObject(ContentModel::class.java)
                if(contentModel!!.favorites.containsKey(uid)){
                    // 좋아요 누른 상태
                    contentModel.favoriteCount = contentModel.favoriteCount - 1
                    contentModel.favorites.remove(uid)
                } else {
                    // 좋아요를 누르지 않은 상태
                    contentModel.favoriteCount = contentModel.favoriteCount + 1
                    contentModel.favorites[uid] = true
                    // 좋아요 카운트가 올라가는 else문에 넣어준다. -> 좋아요를 누르면 1이 추가가 되고 알람 발생
                    favorteAlarm(contentModel.uid!!)
                }
                transition.set(tsDoc, contentModel)
            }
        }

    }


}