package com.duran.howlstagram.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.firebase.storage.FirebaseStorage

class UserFragment: Fragment() {

    lateinit var binding: FragmentUserBinding
    lateinit var firestore: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var storeage: FirebaseStorage
    lateinit var dUid: String
    lateinit var currentUid: String

    val myPhotoResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        // 사진을 받아오면서 바로 업로드한다.
        val imageUrl = result.data!!.data
        // 사진을 저장할 경로
        val storageRef = storeage.reference.child("userProfileImages").child(currentUid)
        storageRef.putFile(imageUrl!!).continueWithTask {
            // 스토리지에 사진만 업로드
            return@continueWithTask storageRef.downloadUrl
        }.addOnCompleteListener {
            imageUri ->
            // 데이터베이스 누가 뭘 올렸는지 정리한 데이터 저장
            Log.e("tab", "여기서 에러인가?")
            val map = HashMap<String, Any>()
            map["image"] = imageUri.result.toString()

            firestore.collection("profileImages").document(currentUid).set(map)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        // activity에서 넘어온 dUid 데이터 받기
        dUid = arguments?.getString("dUid")!!
        // 내 계정인지 상대방 계정인지 판단
        currentUid = auth.currentUser!!.uid

        binding.accountRecyclerview.adapter = UserFragmentRecyclerViewAdapter()
        binding.accountRecyclerview.layoutManager = GridLayoutManager(activity, 3)

        val mainActivity = activity as? MainActivity

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
            // 본인 계정 프로필 사진 view 클릭 시
            binding.accountIvProfile.setOnClickListener {
                val picker = Intent(Intent.ACTION_PICK)
                picker.type = "image/*"
                myPhotoResultLauncher.launch(picker)
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
                mainActivity.binding.bottomNavigation.selectedItemId = R.id.action_home
            }

            // 본인 계정이 아니라면 마이 페이지라면 accountBtnFollowSignout 버튼을 signout으로
            binding.accountBtnFollowSignout.text = activity?.getText(R.string.follow)
        }

        // 프로필 이미지 가져오기
        getProfileImage()

        return binding.root
    }

    fun getProfileImage(){
        // Firebase의 profileImages컬렉션에 있는 프로필이미지를 가져온다.
        firestore.collection("profileImages").document(dUid).addSnapshotListener { value, error ->
            if(value?.data != null) {
                val url = value.data!!["image"]
                Glide.with(requireActivity()).load(url).apply(RequestOptions().circleCrop()).into(binding.accountIvProfile)
            }
        }
    }

    inner class CustomViewHolder(val imageview: ImageView): RecyclerView.ViewHolder(imageview)
    @SuppressLint("NotifyDataSetChanged")
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