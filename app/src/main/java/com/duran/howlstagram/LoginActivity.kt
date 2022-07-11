package com.duran.howlstagram

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class LoginActivity : AppCompatActivity() {

    private val btnEmailLogin: AppCompatButton by lazy {
        findViewById(R.id.email_login_button)
    }
    private val etEmail: EditText by lazy {
        findViewById(R.id.email_edittext)
    }
    private val etPassword: EditText by lazy {
        findViewById(R.id.password_edittext)
    }
    private val googleLoginBtn: AppCompatButton by lazy {
        findViewById(R.id.google_sign_in_button)
    }

    private var auth: FirebaseAuth? = null
    private var googleSignInClient: GoogleSignInClient? = null
    private var GOOGLE_LOGIN_CODE = 9001 // 구글 로그인할때 사용할 request code

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        //Signup or Signin by email 버튼 클릭 시 회원가입 함수
        btnEmailLogin.setOnClickListener {
            signinAndSignup()
        }

        // 구글 로그인 버튼 클릭 시
        googleLoginBtn.setOnClickListener {
            // First step
            googleLogin()
        }

        // 구글 로그인 옵션 빌드
       val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("416715023570-dp8naj20g669vq1pn8t0cs3nee6rj229.apps.googleusercontent.com") // 구글API키
            .requestEmail() // 구글 아이디
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso) // 옵션값을 구글로그인 클라이언트에 세팅
        printHashKey()
    }

    fun printHashKey() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey: String  = String(Base64.encode(md.digest(), 0))
                Log.i("TAG", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("TAG", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("TAG", "printHashKey()", e)
        }
    }

    // 구글 로그인 함수
    private fun googleLogin(){
        val signInIntent: Intent = googleSignInClient!!.signInIntent
        startForResult.launch(signInIntent)
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result: ActivityResult ->

        if(result.resultCode == RESULT_OK){
            val intent: Intent = result.data!!
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(intent)

            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(ContentValues.TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(ContentValues.TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: String) {
        val credential = GoogleAuthProvider.getCredential(account, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { // 회원가입한 결과값을 받아온다.
                    task ->
                if (task.isSuccessful) { // 아이디와 비밀번호가 일치할 때
                    // Login
                    moveMainPage(task.result?.user)
                } else { // 아이디와 비밀번호가 불일치 할 때
                    // Show the error message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    // 회원가입 함수
    private fun signinAndSignup() {
        // 이메일 입력하는 부분과 비밀번호를 입력하는 부분을 넣어준다.
        auth?.createUserWithEmailAndPassword(etEmail.text.toString(), etPassword.text.toString())
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
    private fun signinEmail() {
        // 이메일 입력하는 부분과 비밀번호를 입력하는 부분을 넣어준다.
        auth?.signInWithEmailAndPassword(etEmail.text.toString(), etPassword.text.toString())
            ?.addOnCompleteListener { // 회원가입한 결과값을 받아온다.
                    task ->
                if (task.isSuccessful) { // 아이디와 비밀번호가 일치할 때
                    // Login
                    moveMainPage(task.result?.user)
                } else { // 아이디와 비밀번호가 불일치 할 때
                    // Show the error message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    // 로그인 성공 시 다음 페이지 이동 함수
    private fun moveMainPage(user: FirebaseUser?) {
        if (user != null) { // Firebase 유저 상태를 넘겨준다. / 유저 상태가 있을 경우 다음페이지로 이동
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}