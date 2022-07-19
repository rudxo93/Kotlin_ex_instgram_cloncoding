package com.duran.howlstagram

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.duran.howlstagram.databinding.ActivityAddPhotoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddPhotoBinding
    lateinit var auth: FirebaseAuth
    lateinit var storage: FirebaseStorage
    lateinit var photoUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_photo)
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        // 엑티비티를 실행하자마자 화면이 열릴수 있도록 open code
        var intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        photoResult.launch(intent)

        // addPhoto버튼 클릭 시
        binding.addphotoUploadBtn.setOnClickListener {
            contentUpload()
        }
    }

    fun contentUpload() {
        // 이미지 저장 날싸와 시간(파일명이 중복되지 않도록 날짜와 시간으로)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        // 파일 저장 이름 만들기 -> IMAGE_저장날짜.png
        val imageFileName = "IMAGE_" + timeStamp + ".png"

        // Firebase Storage에 images라는 폴더를 만들어서 사진을 업로드한다.
        var storagePath = storage.reference.child("images").child(imageFileName)

        // 파일 업로드
        storagePath.putFile(photoUri).addOnSuccessListener {
            Toast.makeText(this, getString(R.string.upload_success), Toast.LENGTH_SHORT).show()
        }
    }

    // 선택한 이미지를 받는 부분
    var photoResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result ->
        // 사진 받는 부분
        photoUri = result.data?.data!! // 이미지의 경로가 넘어온다. photoUri에 경로를 담아준다.
        binding.uploadImageview.setImageURI(photoUri) // imageview에 선택한 이미지를 넣어준다.
    }
}