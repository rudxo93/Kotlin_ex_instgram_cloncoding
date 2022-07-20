package com.duran.howlstagram.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.duran.howlstagram.R
import com.duran.howlstagram.databinding.FragmentDetailViewBinding
import com.duran.howlstagram.databinding.ItemDetailBinding
import com.duran.howlstagram.model.ContentModel
import com.google.firebase.firestore.FirebaseFirestore


class DetailViewFragment : Fragment() {

    lateinit var binding: FragmentDetailViewBinding
    lateinit var firestore: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail_view, container, false)
        firestore = FirebaseFirestore.getInstance()

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
            // Proflie Image
            Glide.with(holder.itemView.context).load(contentModels[position].imageUrl).into(viewHolder.profileImageview)
        }

        override fun getItemCount(): Int {
            return contentModels.size
        }

    }


}