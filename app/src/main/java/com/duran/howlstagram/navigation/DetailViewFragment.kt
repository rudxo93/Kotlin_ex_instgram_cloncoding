package com.duran.howlstagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.duran.howlstagram.R
import com.duran.howlstagram.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore

class DetailViewFragment : Fragment() {

    lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_datail, container, false)

        firestore = FirebaseFirestore.getInstance()

        view.findViewById<RecyclerView>(R.id.detailviewfragment_recyclerview).adapter =
            DetailViewRecyclerViewAdapter()
        view.findViewById<RecyclerView>(R.id.detailviewfragment_recyclerview).layoutManager =
            LinearLayoutManager(activity)

        return view
    }

    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {

            // DB에 접근해서 데이터를 받아오는 쿼리 작성(시간순으로 받아온다.)
            firestore.collection("images").orderBy("itmestamp")
                .addSnapshotListener { querySnaphsot, firebaseFirestoreException ->
                    // 받자마자 값 초기화
                    contentDTOs.clear()
                    contentUidList.clear()
                    // for문을 돌려서 스냅샷에 넘어오는 데이터들을 하나하나씩 읽어들인다.
                    for (snapshot in querySnaphsot!!.documents) {
                        // ContentDTO 방식으로 캐스팅한다.
                        val item = snapshot.toObject(ContentDTO::class.java)
                        contentDTOs.add(item!!)
                        contentUidList.add(snapshot.id)
                    }
                    notifyDataSetChanged() // 값이 새로고침 됨
                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)

            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {

            return contentDTOs.size
        }

        // 서버에서 넘어온 데이터들을 맵핑시켜주는 부분
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewholder = (holder as CustomViewHolder).itemView // CustomViewHolder로 캐스팅


            // UserId
            viewholder.findViewById<TextView>(R.id.detailviewitem_profile_textview).text =
                contentDTOs[position].userId

            // Image
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl)
                .into(viewholder.findViewById(R.id.detailviewitem_imageview_content))

            //Explain of content
            viewholder.findViewById<TextView>(R.id.detailviewitem_explain_textview).text =
                contentDTOs[position].explain

            // likes
            viewholder.findViewById<TextView>(R.id.detailviewitem_favoritecounter_textview).text =
                "Likes " + contentDTOs!![position].favoriteCount

            // Proflie Image
            Glide.with(holder.itemView.context).load(contentDTOs[position].imageUrl)
                .into(viewholder.findViewById(R.id.detailviewitem_profile_image))
        }
    }
}