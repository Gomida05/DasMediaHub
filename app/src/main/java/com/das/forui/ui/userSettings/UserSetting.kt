package com.das.forui.ui.userSettings

import android.app.UiModeManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.das.forui.databinding.FragmentUserSettingBinding
import com.das.forui.R

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

        binding.themeMenuButton.setOnClickListener{
            val popupMenu = PopupMenu(requireContext(), it).apply {
                gravity = Gravity.END
                menuInflater.inflate(R.menu.theme_menu, this.menu)
            }



            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.change_toDarkMode -> {

                        changeTheme(UiModeManager.MODE_NIGHT_YES)
                        true
                    }
                    R.id.change_toLightMode -> {
                        changeTheme(UiModeManager.MODE_NIGHT_NO)
                        true
                    }
                    R.id.setDefault -> {
                        // Handle reset to default
                        changeTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()

        }

    }

    private fun changeTheme(setUiMode: Int) {
        val sharedPref: SharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("isNightModeOn", setUiMode)
            apply()
        }
        AppCompatDelegate.setDefaultNightMode(setUiMode)
    }


}