package com.duran.howlstagram

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.duran.howlstagram.databinding.ActivityMainBinding
import com.duran.howlstagram.fragment.AlarmFragment
import com.duran.howlstagram.fragment.DetailViewFragment
import com.duran.howlstagram.fragment.GridFragment
import com.duran.howlstagram.fragment.UserFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener  {

    lateinit var binding: ActivityMainBinding
    lateinit var auth: FirebaseAuth
    lateinit var message: FirebaseMessaging
    lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        auth = FirebaseAuth.getInstance()
        message = FirebaseMessaging.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)
        // 메인 엑티비티에 detail fragment 적용
        binding.bottomNavigation.selectedItemId = R.id.action_home

        // 사진 경로를 가져올 수 있는 권한 요청
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        // Firebase Database에 pushToken을 저장한다.
        // 로그인이 되자마자 바로 DB에 저장된다.
        saveMyPushToken()
    }

    // Firebase Database에 pushToken을 저장한다.
    fun saveMyPushToken() {
        // 토큰 생성
        message.token.addOnCompleteListener { // addOnCompleteListener 리스너로 성공 유무의 값을 확인
            task ->
            if (task.isSuccessful) {
                // task에서 받아오는 토큰id
                var token = task.result
                // DB에 넣어준다.
                var map = mutableMapOf<String, Any>()
                map["token"] = token

                // pushtokens컬렉션 안에 토큰값(map)을 넣어준다.
                firestore.collection("pushtokens").document(auth.uid!!).set(map)
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            //  home 아이템 -> 상세페이지
            R.id.action_home -> {

                // home에서는 logo만 보이고 뒤로가기 버튼과 username은 숨겨준다.
                binding.toolbarUsername.visibility = View.INVISIBLE
                binding.toolbarBtnBack.visibility = View.INVISIBLE
                binding.toolbarLogo.visibility = View.VISIBLE

                val fragment = DetailViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit()
                return true
            }
            // search 아이템 ->
            R.id.action_search -> {
                val fragment = GridFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit()
                return true
            }
            // addPhoto 아이템 -> photo upload
            R.id.action_add_photo -> {
                // 외부 스토리지 경로를 가져올 수 있는 권한이 있는지 체크 후 addPhoto 액티비티를 호출할 수 있는 코드를 넣어준다.
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // 사진 읽기(READ_EXTERNAL_STORAGE)가 있을때
                    startActivity(Intent(this, AddPhotoActivity::class.java))
                } else {
                    // 사진 읽기(READ_EXTERNAL_STORAGE)가 없을때
                    Toast.makeText(this, getString(R.string.donothave_permission), Toast.LENGTH_SHORT).show()
                }
                return true
            }
            // favoriteAlarm 아이템 -> 좋아요 알람
            R.id.action_favorite_alarm -> {
                val fragment = AlarmFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit()
                return true
            }
            // account 아이템 -> 계정 정보
            R.id.action_account -> {
                val fragment = UserFragment()
                // Activity -> Fragment 데이터 가지고 이동
                val bundle = Bundle()
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                bundle.putString("dUid", uid) // 데이터 넣기
                fragment.arguments = bundle // fragment의 arguments에 데이터를 담은 bundle을 넘겨준다.
                supportFragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit()
                return true
            }
        }
        return false
    }

}