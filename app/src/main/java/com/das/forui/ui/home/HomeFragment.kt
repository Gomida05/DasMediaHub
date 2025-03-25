package com.das.forui.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.das.forui.MainActivity
import com.das.forui.R
import com.das.forui.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }



    override fun onStart() {
        super.onStart()

        binding.gosearch.setOnClickListener {
            findNavController().navigate(R.id.nav_searcher)
        }
        binding.profile.setOnClickListener {

//            AlertDialog.Builder(context)
//                .setTitle("Do you want to start the foregroundService or bannerAds?")
//                .setNegativeButton("BannerAds") { _, _ ->
//                    (activity as MainActivity).startBanner(
//                        true
//                    )
//                }
//                    .show()
            Toast.makeText(
                requireContext(),
                "Coming soon!",
                Toast.LENGTH_SHORT
            )
                .show()
        }
        binding.downloadList.setOnClickListener {
            findNavController().navigate(R.id.nav_Downloads)
        }
    }


    override fun onResume() {
        super.onResume()
        (activity as MainActivity).showBottomNav()
    }

override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}