package com.das.forui.ui.userSettings

import android.content.res.Configuration.UI_MODE_NIGHT_MASK
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.das.forui.databinding.FragmentUserSettingBinding

class UserSetting: Fragment() {
    private var _binding: FragmentUserSettingBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserSettingBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.goBack.setOnClickListener{
            findNavController().navigateUp()
        }
        binding.toggleButton.setOnClickListener{
            changeTheme()
        }
    }

    private fun changeTheme() {
        val isDarkMode =  resources.configuration.uiMode and UI_MODE_NIGHT_MASK
        if (isDarkMode == UI_MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        activity?.recreate()
    }


}