package com.duran.howlstagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.duran.howlstagram.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val btn_email_login: AppCompatButton by lazy {
        findViewById<AppCompatButton>(R.id.email_login_button)
    }
    private val et_email: EditText by lazy {
        findViewById<EditText>(R.id.email_edittext)
    }
    private val et_password: EditText by lazy {
        findViewById<EditText>(R.id.password_edittext)
    }

    var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        //Signup or Signin by email 버튼 클릭 시 회원가입 함수
        btn_email_login.setOnClickListener {
            signinAndSignup()
        }
    }

    // 회원가입 함수
    fun signinAndSignup() {
        // 이메일 입력하는 부분과 비밀번호를 입력하는 부분을 넣어준다.
        auth?.createUserWithEmailAndPassword(et_email.text.toString(), et_password.text.toString())
            ?.addOnCompleteListener { // 회원가입한 결과값을 받아온다.// 회원가입한 결과값을 받아온다.
                // 람다형식
                    task ->
                if (task.isSuccessful) {    //계정 만들기에 성공했을 때
                    //Creating a user account
                    moveMainPage(task.result?.user)
                } else {
                    //Login if you have account
                    if (task.exception?.message.equals("The email address is already in use by another account."))
                        signinEmail() // 회원가입도 아니고 에러 메세지가 아닐 경우 로그인
                    //Show the error message
                    else
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    // 로그인 함수
    fun signinEmail() {
        // 이메일 입력하는 부분과 비밀번호를 입력하는 부분을 넣어준다.
        auth?.signInWithEmailAndPassword(et_email.text.toString(), et_password.text.toString())
            ?.addOnCompleteListener { // 회원가입한 결과값을 받아온다.
                    task ->
                if (task.isSuccessful) { // 아이디와 비밀번호가 일치할 때
                    // Login
                    moveMainPage(task.result.user)
                } else { // 아이디와 비밀번호가 불일치 할 때
                    // Show the error message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    // 로그인 성공 시 다음 페이지 이동 함수
    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) { // Firebase 유저 상태를 넘겨준다. / 유저 상태가 있을 경우 다음페이지로 이동
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}