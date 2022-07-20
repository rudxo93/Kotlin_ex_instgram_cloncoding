package com.duran.howlstagram.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.duran.howlstagram.R
import com.duran.howlstagram.databinding.FragmentUserBinding
import com.duran.howlstagram.model.ContentModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserFragment: Fragment() {

    lateinit var binding: FragmentUserBinding
    lateinit var firestore: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var dUid: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        dUid = arguments?.getString("dUid")!! // activity에서 넘어온 dUid 데이터 받기

        binding.accountRecyclerview.adapter = UserFragmentRecyclerViewAdapter()
        binding.accountRecyclerview.layoutManager = GridLayoutManager(activity, 3)

        return binding.root
    }

    inner class CustomViewHolder(val imageview: ImageView): RecyclerView.ViewHolder(imageview)
    inner class UserFragmentRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        val contentModels: ArrayList<ContentModel> = arrayListOf()

        init {
            // Firebase의 image 컬렉션에서 uid필드가 dUid인 문서들만 가져온다.
            // whereEqualTo는 == 연산자에 해당된다.
            // addSnapshotListener -> Firebase에서 실시간으로 바뀐 데이터를 업테이트 하는 기능
            firestore.collection("images").whereEqualTo("uid", dUid).addSnapshotListener { value, error ->
                if(value == null) return@addSnapshotListener

                for(item in value.documents) {
                    contentModels.add(item.toObject(ContentModel::class.java)!!)
                }

                binding.accountPostTextview.text = contentModels.size.toString()

                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val width = resources.displayMetrics.widthPixels / 3
            val view = ImageView(parent.context)
            view.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val imageview = (holder as CustomViewHolder).imageview
            Glide.with(holder.itemView.context).load(contentModels[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageview)
        }

        override fun getItemCount(): Int {
            return contentModels.size
        }
    }
}