package com.example.bazaar.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bazaar.R
import com.example.bazaar.databinding.FragmentRegisterBinding
import com.example.bazaar.repository.Repository
import com.example.bazaar.viewmodels.RegisterViewModel
import com.example.bazaar.viewmodels.RegisterViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    companion object {
        fun newInstance(): RegisterFragment {
            return RegisterFragment()
        }
    }

    private lateinit var registerViewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val registerViewModelFactory = RegisterViewModelFactory(this.requireContext(), Repository())
        registerViewModel = ViewModelProvider(this, registerViewModelFactory)[RegisterViewModel::class.java]
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root

        clickHereBtnHandler()
        registerBtnHandler()
        resetErrorMessageOnTextInputLayouts()
        navigateToTLoginFragmentIfRegisterSuccessful()
        loginViewModelErrorObservable()

        return view
    }

    /** Shows error message to user on unsuccessful Register attempt **/
    private fun loginViewModelErrorObservable() {
        registerViewModel.error.observe(viewLifecycleOwner) {
            // hide progressbar
            binding.progressbar.visibility = View.INVISIBLE
            // show error message
            Snackbar.make(requireView(), registerViewModel.error.value.toString(), Snackbar.LENGTH_LONG).show()
            // make login button clickable
            binding.registerBtn.isClickable = true
        }
    }

    /** Navigates to LoginFragment on successful register **/
    private fun navigateToTLoginFragmentIfRegisterSuccessful() {
        registerViewModel.success.observe(viewLifecycleOwner) {
            // hide progressbar
            binding.progressbar.visibility = View.INVISIBLE
            // navigate to TimeLineFragment
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            // make login button clickable
            binding.registerBtn.isClickable = true
        }
    }

    /** Resets error message on double click **/
    private fun resetErrorMessageOnTextInputLayouts() {
        binding.usernameEt.setOnClickListener {
            binding.usernameTil.error = null
            binding.usernameTil.isErrorEnabled = false
        }
        binding.passwordEt.setOnClickListener {
            binding.passwordTil.error = null
            binding.passwordTil.isErrorEnabled = false
        }
        binding.emailTil.setOnClickListener {
            binding.emailTil.error = null
            binding.emailTil.isErrorEnabled = false
        }
        binding.phoneTil.setOnClickListener {
            binding.phoneTil.error = null
            binding.phoneTil.isErrorEnabled = false
        }
    }

    /**  Handles clicks on register button**/
    private fun registerBtnHandler() {
        binding.registerBtn.setOnClickListener {
            tryToRegister()
        }
    }

    /**  Handles clicks on clickHere button**/
    private fun clickHereBtnHandler() {
        binding.clickHereBtn.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    /** Tries to Register **/
    private fun tryToRegister() {
        // make register button not clickable
        binding.registerBtn.isClickable = false

        // resets error message on text input layouts
        binding.usernameTil.error = null
        binding.usernameTil.isErrorEnabled = false
        binding.passwordTil.error = null
        binding.passwordTil.isErrorEnabled = false
        binding.emailTil.error = null
        binding.emailTil.isErrorEnabled = false
        binding.phoneTil.error = null
        binding.phoneTil.isErrorEnabled = false

        // analyzes wrong inputs
        if (binding.usernameEt.text.trim().isEmpty()) {
            binding.usernameTil.error = "Please input your username!"
            // make register button clickable
            binding.registerBtn.isClickable = true
            return
        }
        if (binding.usernameEt.text.trim().length < 3) {
            binding.usernameTil.error = "Your username must contain at least 3 characters!"
            // make register button clickable
            binding.registerBtn.isClickable = true
            return
        }
        if (binding.passwordEt.text.trim().isEmpty()) {
            binding.passwordTil.error = "Please input your password!"
            // make register button clickable
            binding.registerBtn.isClickable = true
            return
        }
        if (binding.passwordEt.text.trim().length < 3) {
            binding.passwordTil.error = "Your password must contain at least 3 characters!"
            // make register button clickable
            binding.registerBtn.isClickable = true
            return
        }
        if (!binding.emailEt.text.isValidEmail()) {
            binding.emailTil.error = "Please input your email!"
            // make register button clickable
            binding.registerBtn.isClickable = true
            return
        }
        if (binding.phoneEt.text.trim().isEmpty()) {
            binding.phoneTil.error = "Please input your phone number!"
            // make register button clickable
            binding.registerBtn.isClickable = true
            return
        }

        // initializes user in register view model
        registerViewModel.user.value.let {
            if (it != null) {
                it.username = binding.usernameEt.text.toString()
            }
            if (it != null) {
                it.password = binding.passwordEt.text.toString()
            }
            if (it != null) {
                it.email = binding.emailEt.text.toString()
            }
            if (it != null) {
                it.phone_number = binding.phoneEt.text.toString()
            }
        }

        // attempt to register in inside lifecycleScope
        lifecycleScope.launch {
            binding.progressbar.visibility = View.VISIBLE
            registerViewModel.register()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}