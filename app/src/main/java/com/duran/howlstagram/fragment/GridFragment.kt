package com.duran.howlstagram.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.duran.howlstagram.R
import com.duran.howlstagram.databinding.FragmentGridBinding
import com.duran.howlstagram.databinding.ItemImageviewBinding
import com.duran.howlstagram.model.ContentModel
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore

class GridFragment: Fragment() {

    lateinit var binding: FragmentGridBinding
    lateinit var firestore: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_grid, container, false)
        firestore = FirebaseFirestore.getInstance()

        binding.gridRecyclerview.adapter = GridFragmentRecyclerviewAdapter()
        binding.gridRecyclerview.layoutManager = GridLayoutManager(activity, 3)

        return binding.root
    }

    //
    inner class CellImageViewHolder(val binding : ItemImageviewBinding) : RecyclerView.ViewHolder(binding.root)
    inner class GridFragmentRecyclerviewAdapter : RecyclerView.Adapter<CellImageViewHolder>(){
        var contentModels: ArrayList<ContentModel> = arrayListOf()
        init {
            // images 컬렉션에 접근해서 데이터를 계속 지켜본다.
            firestore.collection("images").addSnapshotListener { value, error ->
                // images컬렉션 안에 있는 item들을 전부 가져온다. -> images안에 10개의 데이터가 있다면 -> item 안에 현재 10개가 들어있다.
                // Log.e("item 갯수", "${item}") -> 로그값 확인
                // 스냅샷 리스너를 통해 수신된 문서들은 documentChanges에 추가된다.
                // *** 스냅샷 리스너를 추가하면 처음에 Firestore에 들어있던 문서들이 ADDED타입으로 documentChanges에 추가된다.
                for(item in value!!.documentChanges){
                    // 이때 특정 변경사항만 이용하려면 조건문을 이용해 문서의 type을 비교한다.
                    // DocumentChange.Type.ADDED -> 추가된 문서
                    // DocumentChange.Type.MODIFIED -> 수정된 문서
                    // DocumentChange.Type.REMOVED -> 삭제된 문서
                    if(item.type == DocumentChange.Type.ADDED){
                        contentModels.add(item.document.toObject(ContentModel::class.java))
                    }
                }

                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellImageViewHolder {
            // displayMetrics의 widthPixels 가로 해상도를 3으로 나누어준다.
            var width = resources.displayMetrics.widthPixels / 3

            var view = ItemImageviewBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            view.cellImageview.layoutParams = LinearLayoutCompat.LayoutParams(width,width)
            return CellImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: CellImageViewHolder, position: Int) {
            var contentModel = contentModels[position]
            Glide.with(holder.itemView.context).load(contentModel.imageUrl).into(holder.binding.cellImageview)

            //상대방 유저페이지 이동
            holder.binding.cellImageview.setOnClickListener {
                var userFragment = UserFragment()
                var bundle = Bundle()
                bundle.putString("dUid",contentModel.uid)
                bundle.putString("userId",contentModel.userId)
                userFragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.main_content,userFragment)?.commit()

            }
        }

        override fun getItemCount(): Int {
            return contentModels.size
        }

    }
}