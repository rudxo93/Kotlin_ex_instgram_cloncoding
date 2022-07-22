package com.duran.howlstagram.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.duran.howlstagram.databinding.ItemImageviewBinding
import com.duran.howlstagram.model.ContentModel
import com.duran.howlstagram.model.FollowModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class UserFragment: Fragment() {

    lateinit var binding: FragmentUserBinding
    lateinit var firestore: FirebaseFirestore
    lateinit var auth: FirebaseAuth
    lateinit var storeage: FirebaseStorage
    lateinit var dUid: String
    lateinit var currentUid: String

    var myPhotoResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        // 사진을 받아오면서 바로 업로드한다.
        var imageUrl = result.data?.data!!
        // Firebase Storage에 userProfileImages 폴더를 만들어서 사진을 업로드한다.
        var storageRef = storeage.reference.child("userProfileImages").child(currentUid)

        // 여기서부터 안넘어온다.....
        storageRef.putFile(imageUrl).continueWithTask {
            // 스토리지에 사진만 업로드
            return@continueWithTask storageRef.downloadUrl
        }.addOnCompleteListener {
            imageUri ->
            // 데이터베이스 누가 뭘 올렸는지 정리한 데이터 저장
            var map = HashMap<String, Any>()
            map["image"] = imageUri.toString()

            firestore.collection("profileImages").document(dUid).set(map)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        // activity에서 넘어온 dUid 데이터 받기
        // 현재 로그인한 사용자의 UID값
        dUid = arguments?.getString("dUid")!!
        // 내 계정인지 상대방 계정인지 판단
        // 지금 사용자의 Uid 정보가 currentUid에 들어간다
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
            // 본인 계정 프로필 사진 view 클릭 시
            binding.accountIvProfile.setOnClickListener {
                var picker = Intent(Intent.ACTION_PICK)
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

            // follow버튼을 눌렀을때 팔로우와 팔로잉에 대한 함수 호출
            binding.accountBtnFollowSignout.setOnClickListener {
                reqeustFollwerAndFollowing()
            }
            // 본인 계정이 아니라면 마이 페이지라면 accountBtnFollowSignout 버튼을 signout으로
            binding.accountBtnFollowSignout.text = activity?.getText(R.string.follow)
        }

        // 프로필 이미지 가져오기
        getProfileImage()

        return binding.root
    }

    // 팔로우와 팔로잉 결과
    fun reqeustFollwerAndFollowing() {
        // 팔로잉 정보 -> 내가 따르고 있는 사람
        // Firebase Database에 users라는 컬렉션에 사용자 UID명으로 저장한다. -> 데이터베이스 경로 접근
        val tsDocFollowing = firestore.collection("users").document(currentUid)
        // runTransaction
        // -> 여러 클라이언트의 데이터 중복 접근 방지,  firestore에 저장하고 싶은 경로를 클래스로 만든뒤에 runTransaction을 호출
        // 한 사람이 Document에 접근해서 데이터를 쓰는 동안
        // 다른 사용자는 그동안 데이터베이스로 접근할 수 없다.
        firestore.runTransaction {
                transition ->
            // followingModel = transition.get(데이터베이스 경로).toObject(데이터모델)
            // 데이터베이스 경로에
            var followingModel = transition.get(tsDocFollowing).toObject(FollowModel::class.java)
            // followingModel == null -> 현재 팔로우를 한적이 없다.
            if(followingModel == null){
                // 팔로우 수가 0이다.
                followingModel = FollowModel()
                followingModel.followingCount = 1
                followingModel.followings[dUid] = arguments?.getString("userId")!!
                // transition.set(데이터베이스 경로, followingModel)
                // users의 현재 사용자 이름으로된 데이터 베이스 경로에 followingCount와 followings값을 set해준다.
                transition.set(tsDocFollowing,followingModel)
                return@runTransaction
                // FollowModel에 followings Map에 키값이 있는지 없는지 -> 있다면 true 없다면 false
                // FollowModel에 followings Map에 dUid키값을 가져온다(true)
                // 현재 dUid에는 팔로우 하고자 하는 계정의 주소가 담겨있다
            }else if(followingModel.followings.containsKey(dUid)){
                // 팔로우 취소
                followingModel.followingCount = followingModel.followingCount - 1
                // 팔로우를 취고하고자 하는 계정의 주소를 삭제한다.
                followingModel.followings.remove(dUid)
            }else{ // followingModel이 null이 아니거나 followingModel에 followings Map에 dUid키값이 없다면(false)
                // 팔로우 카운터 +1
                followingModel.followingCount = followingModel.followingCount + 1
                // followings Map에 userId값을 dUid키값에 넣는다.
                // 내가 팔로우 하고자 하는 계정의 이메일
                followingModel.followings[dUid] = arguments?.getString("userId")!!

            }
            // transition.set(데이터베이스 경로, followingModel)
            transition.set(tsDocFollowing,followingModel)
            return@runTransaction
        }

        // 팔로우 정보 -> 나를 따르는 사람
        // Firebase Database에 users라는 컬렉션에 dUid에 접근
        val tsDocFollower = firestore.collection("users").document(dUid)
        // 계정정보의
        val sId = auth.currentUser?.email
        firestore.runTransaction {
                transition ->
            // followModel = transition.get(데이터베이스 경로).toObject(데이터모델)
            var followModel = transition.get(tsDocFollower).toObject(FollowModel::class.java)

            if(followModel == null){
                // followModel == null -> 아무도 팔로우를 하지 않았다
                followModel = FollowModel()
                followModel.followerCount = 1
                followModel.followers[currentUid] = sId!!
                transition.set(tsDocFollower,followModel)
                return@runTransaction
            }else if(followModel.followers.containsKey(currentUid)){
                //로그인된 아이디가 누구를 스토킹을 이미 했을때
                followModel.followerCount = followModel.followerCount - 1
                followModel.followers.remove(currentUid)

            }else{
                //아직 아무도 스토킹을 하지 않았을때(인기가 있어서 상대방이 쫒아다닐때)
                followModel.followerCount = followModel.followerCount + 1
                followModel.followers[currentUid] = sId!!
            }
            transition.set(tsDocFollower,followModel)
            return@runTransaction
        }
    }


    fun getProfileImage(){
        // Firebase의 profileImages컬렉션에 있는 프로필이미지를 가져온다.
        firestore.collection("profileImages").document(dUid).addSnapshotListener { value, error ->
            if(value?.data != null) {
                var url = value.data!!["image"]
                Glide.with(requireActivity()).load(url).apply(RequestOptions().circleCrop()).into(binding.accountIvProfile)
            }
        }
    }

    inner class CellImageViewHolder(val binding: ItemImageviewBinding): RecyclerView.ViewHolder(binding.root)
    @SuppressLint("NotifyDataSetChanged")
    inner class UserFragmentRecyclerViewAdapter: RecyclerView.Adapter<CellImageViewHolder>(){
        var contentModels: ArrayList<ContentModel> = arrayListOf()

        init {
            // Firebase의 image 컬렉션에서 uid필드가 dUid인 문서들만 가져온다.
            // whereEqualTo는 == 연산자에 해당된다.
            // addSnapshotListener -> Firebase에서 실시간으로 바뀐 데이터를 업테이트 하는 기능
            firestore.collection("images").whereEqualTo("uid", dUid).addSnapshotListener { value, error ->
                for(item in value!!.documentChanges){
                    if(item.type == DocumentChange.Type.ADDED){
                        contentModels.add(item.document.toObject(ContentModel::class.java)!!)
                    }
                }
                binding.accountPostTextview.text = contentModels.size.toString()

                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellImageViewHolder {
            var width = resources.displayMetrics.widthPixels / 3
            var view = ItemImageviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            view.cellImageview.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CellImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: CellImageViewHolder, position: Int) {
            var contentModel = contentModels[position]
            Glide.with(holder.itemView.context).load(contentModel.imageUrl).into(holder.binding.cellImageview)
        }

        override fun getItemCount(): Int {
            return contentModels.size
        }
    }
}