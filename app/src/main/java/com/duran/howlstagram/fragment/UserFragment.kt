package com.duran.howlstagram.fragment

import android.content.Intent
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
import com.duran.howlstagram.LoginActivity
import com.duran.howlstagram.MainActivity
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
    lateinit var currentUid: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        // activity에서 넘어온 dUid 데이터 받기
        dUid = arguments?.getString("dUid")!!
        // 내 계정인지 상대방 계정인지 판단
        currentUid = auth.currentUser!!.uid

        binding.accountRecyclerview.adapter = UserFragmentRecyclerViewAdapter()
        binding.accountRecyclerview.layoutManager = GridLayoutManager(activity, 3)

        var mainActivity = activity as? MainActivity

        if (currentUid == dUid) {
            // my page
            mainActivity?.binding?.toolbarLogo?.visibility = View.VISIBLE
            mainActivity?.binding?.toolbarUsername?.visibility = View.INVISIBLE
            mainActivity?.binding?.toolbarBtnBack?.visibility = View.INVISIBLE

            // 본인 계정의 마이 페이지라면 accountBtnFollowSignout 버튼을 signout으로
            binding.accountBtnFollowSignout.text = activity?.getText(R.string.signout)
            // 본인 계정일 때 signout버튼 클릭한다면 엑티비티 종료, 로그인 엑티비티를 호출, Firebase auth값을 signout
            binding.accountBtnFollowSignout.setOnClickListener {
                auth.signOut()
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
            }
        } else {
            // other user page
            mainActivity?.binding?.toolbarLogo?.visibility = View.INVISIBLE
            mainActivity?.binding?.toolbarUsername?.visibility = View.VISIBLE
            mainActivity?.binding?.toolbarBtnBack?.visibility = View.VISIBLE

            // 다른 사람의 user페이지 일 경우 누구의 user페이지인지 보여즈고 back버튼 활성화
            mainActivity?.binding?.toolbarUsername?.text = arguments?.getString("userId")
            // 뒤로가기 버튼 클릭 시 home으로 이동
            mainActivity?.binding?.toolbarBtnBack?.setOnClickListener {
                mainActivity?.binding?.bottomNavigation.selectedItemId = R.id.action_home
            }

            // 본인 계정이 아니라면 마이 페이지라면 accountBtnFollowSignout 버튼을 signout으로
            binding.accountBtnFollowSignout.text = activity?.getText(R.string.follow)
        }

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