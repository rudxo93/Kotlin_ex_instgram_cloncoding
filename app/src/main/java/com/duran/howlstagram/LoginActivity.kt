package com.duran.howlstagram

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.duran.howlstagram.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    // 계정 인증
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        // auth를 쓰기전에싱글톤 패턴으로 인스턴스를 받아와야한다.
        auth = FirebaseAuth.getInstance()

        // 이메일 로그인 버튼 클릭 시
        binding.emailLoginButton.setOnClickListener {
            // 계정 생성 함수
            signinAndsignup()
        }
    }

    // 계정 생성
    private fun signinAndsignup() {
        val id = binding.edittextId.text.toString()
        val password = binding.edittextPassword.text.toString()

        // 이메일 주소와 비밀번호를 createUserWithEmailAndPassword에 전달하여 신규 계정을 생성한다.
        auth.createUserWithEmailAndPassword(id, password).addOnCompleteListener { // addOnCompleteListener 리스너로 성공 유무의 값을 확인
            task ->
            if(task.isSuccessful){
                // 신규계정 생성이 성공했다면 ?
                moveMain(task.result?.user)
            } else {
                // 이미 계정이 존재한다면?
                signinEmail()
            }
        }
    }

    // 이미 계정이 존재한다면?
    private fun signinEmail() {
        val id = binding.edittextId.text.toString()
        val password = binding.edittextPassword.text.toString()

        // 앱에 로그인하게되면 주소와 비밀번호를 signInWithEmailAndPassword에 전달
        auth.signInWithEmailAndPassword(id, password).addOnCompleteListener {
                task ->
            if(task.isSuccessful) {
                moveMain(task.result?.user)
            }
        }
    }

    // 로그인 성공했다면(user가 null이 아닐경우) 메인 엑티비티로 이동
    private fun moveMain(user: FirebaseUser?) {
        if(user != null){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

}