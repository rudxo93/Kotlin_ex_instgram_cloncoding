package com.duran.howlstagram

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import com.duran.howlstagram.databinding.ActivityMainBinding
import com.duran.howlstagram.fragment.AlarmFragment
import com.duran.howlstagram.fragment.DetailViewFragment
import com.duran.howlstagram.fragment.GridFragment
import com.duran.howlstagram.fragment.UserFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener  {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.bottomNavigation.setOnNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            //  home 아이템 -> 상세페이지
            R.id.action_home -> {
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
                supportFragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit()
                return true
            }
        }
        return false
    }

}