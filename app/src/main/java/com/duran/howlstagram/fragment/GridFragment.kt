package com.duran.howlstagram.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.duran.howlstagram.R
import com.duran.howlstagram.databinding.FragmentGridBinding

class GridFragment: Fragment() {

    lateinit var binding: FragmentGridBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_grid, container, false)

        return binding.root
    }
}