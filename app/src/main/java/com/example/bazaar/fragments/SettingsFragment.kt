package com.example.bazaar.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bazaar.MyApplication
import com.example.bazaar.R
import com.example.bazaar.api.model.User
import com.example.bazaar.databinding.FragmentSettingsBinding
import com.example.bazaar.manager.SharedPreferencesManager
import com.example.bazaar.repository.Repository
import com.example.bazaar.viewmodels.GetUserInfoViewModel
import com.example.bazaar.viewmodels.GetUserInfoViewModelFactory
import com.example.bazaar.viewmodels.UpdateUserDataViewModel
import com.example.bazaar.viewmodels.UpdateUserDataViewModelFactory
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var getUserInfoViewModel: GetUserInfoViewModel
    private lateinit var updateUserDataViewModel: UpdateUserDataViewModel


    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // creates LoginViewModel with factory
        val factory = GetUserInfoViewModelFactory(this.requireContext(), Repository())
        getUserInfoViewModel = ViewModelProvider(this, factory)[GetUserInfoViewModel::class.java]

        val factory2 = UpdateUserDataViewModelFactory(this.requireContext(), Repository())
        updateUserDataViewModel = ViewModelProvider(this, factory2)[UpdateUserDataViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root

        val username = arguments!!.getString("username")!!

        resetErrorMessageOnTextInputLayouts()

        viewSelection(username)
        tryToGetUserInfo(username)
        tryToGetUserInfoSuccessful()
        updateUserDataSuccessful()

        // back arrow
        binding.toolbar.setNavigationIcon(R.drawable.ic_toolbar_arrow)

        binding.toolbar.setNavigationOnClickListener {
            // back button pressed
            findNavController().navigateUp()
        }

        return view
    }

    /** Selects owner or user view depending on the username in shared preferences **/
    private fun viewSelection(username: String) {
        if (username == MyApplication.sharedPreferences.getUserValue(SharedPreferencesManager.KEY_USER, User()).username) {
            handleOwnerView()
        } else {
            handleUserView()
        }
    }

    /** handles user view **/
    private fun handleUserView() {
        binding.emailEt.keyListener = null
        binding.usernameEt.keyListener = null
        binding.phoneEt.keyListener = null
        binding.publishBtn.text = "Send a chat message"
    }

    /** handles owner view **/
    private fun handleOwnerView() {
        binding.emailEt.keyListener = null
        binding.publishBtn.setOnClickListener {
            tryToPublish()
        }
    }

    /**resets error messages on text input layouts **/
    private fun resetErrorMessageOnTextInputLayouts() {
        binding.usernameEt.setOnClickListener {
            binding.usernameTil.error = null
            binding.usernameTil.isErrorEnabled = false
        }
        binding.phoneTil.setOnClickListener {
            binding.phoneTil.error = null
            binding.phoneTil.isErrorEnabled = false
        }
    }

    /** tries to publish changes to user data**/
    private fun tryToPublish() {
        // make publish button not clickable
        binding.publishBtn.isClickable = false

        // resets error message on text input layouts
        binding.usernameTil.error = null
        binding.usernameTil.isErrorEnabled = false
        binding.phoneTil.error = null
        binding.phoneTil.isErrorEnabled = false


        // analyzes wrong inputs
        if (binding.usernameEt.text.trim().isEmpty()) {
            binding.usernameTil.error = "Please input your username!"
            // make publish button clickable
            binding.publishBtn.isClickable = true
            return
        }
        if (binding.usernameEt.text.trim().length < 3) {
            binding.usernameTil.error = "Your username must contain at least 3 characters!"
            // make publish button clickable
            binding.publishBtn.isClickable = true
            return
        }
        if (binding.phoneEt.text.trim().isEmpty()) {
            binding.phoneTil.error = "Please input your phone number!"
            // make publish button clickable
            binding.publishBtn.isClickable = true
            return
        }

        updateUserDataViewModel.updateUserDataRequest.value.let {
            if (it != null) {
                it.username = binding.usernameEt.text.toString()
            }
            if (it != null) {
                it.phone_number = binding.phoneEt.text.toString().toLong()
            }
        }

        // attempt to update user data in inside lifecycleScope
        lifecycleScope.launch {
            updateUserDataViewModel.updateUserData()
        }

    }

    /** called when user data was updated successfully **/
    private fun updateUserDataSuccessful() {
        updateUserDataViewModel.updateUserDataRequest.observe(viewLifecycleOwner) {
            // make publish button clickable
            binding.publishBtn.isClickable = true
        }
    }

    /** tries to get user information **/
    private fun tryToGetUserInfo(username: String) {
        lifecycleScope.launch {
            getUserInfoViewModel.getUserInfo(username)
        }
    }

    /** called when user info was obtained successfully**/
    private fun tryToGetUserInfoSuccessful() {
        getUserInfoViewModel.userResponse.observe(viewLifecycleOwner) {
            binding.emailEt.setText(getUserInfoViewModel.userResponse.value?.data?.get(0)?.email.toString())
            binding.phoneEt.setText(getUserInfoViewModel.userResponse.value?.data?.get(0)?.phone_number.toString())
            binding.usernameEt.setText(getUserInfoViewModel.userResponse.value?.data?.get(0)?.username.toString())
            binding.profileNameTv.text = getUserInfoViewModel.userResponse.value?.data?.get(0)?.username.toString()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.inflateMenu(R.menu.app_bar_menu_settings)

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.nav_settings -> {
                    // Save profile changes
                    Toast.makeText(requireContext(), "Click Settings Icon.", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}