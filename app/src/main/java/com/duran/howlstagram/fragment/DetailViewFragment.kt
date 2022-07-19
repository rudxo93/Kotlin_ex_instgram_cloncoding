package com.duran.howlstagram.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.duran.howlstagram.R
import com.duran.howlstagram.databinding.FragmentDetailViewBinding


class DetailViewFragment : Fragment() {

    lateinit var binding: FragmentDetailViewBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail_view, container, false)

        return binding.root
    }

}