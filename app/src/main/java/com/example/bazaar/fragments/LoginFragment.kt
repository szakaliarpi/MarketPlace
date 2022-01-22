package com.example.bazaar.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bazaar.R
import com.example.bazaar.activities.MainActivity
import com.example.bazaar.databinding.FragmentLoginBinding
import com.example.bazaar.repository.Repository
import com.example.bazaar.viewmodels.LoginViewModel
import com.example.bazaar.viewmodels.LoginViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var loginViewModel: LoginViewModel

    companion object {
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // creates LoginViewModel with factory
        val factory = LoginViewModelFactory(this.requireContext(), Repository())
        loginViewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]
    }

    /** Overriding onResume to hide bottom navigation and reset user**/
    override fun onResume() {
        super.onResume()
        makeBottomNavigationGone()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root

        loginBtnHandler()
        loginViewModelErrorObservable()
        resetErrorMessageOnTextInputLayouts()
        navigateToTimelineFragmentIfLogInSuccessful()
        signupBtnHandler()
        clickHereBtnHandler()

        return view
    }

    /** Navigates to ForgotPasswordFragment when clicked **/
    private fun clickHereBtnHandler() {
        binding.clickHereBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }
    }

    /** Navigates to SignupFragment when clicked **/
    private fun signupBtnHandler() {
        binding.signupBtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    /** Shows error message to user on unsuccessful login attempt **/
    private fun loginViewModelErrorObservable() {
        loginViewModel.error.observe(viewLifecycleOwner) {
            // hide progressbar
            binding.progressbar.visibility = View.INVISIBLE
            // show error message
            Snackbar.make(requireView(), loginViewModel.error.value.toString(), Snackbar.LENGTH_LONG).show()
            // make login button clickable
            binding.loginBtn.isClickable = true
        }
    }

    /** Tries to log in when clicked **/
    private fun loginBtnHandler() {
        binding.loginBtn.setOnClickListener {
            tryToLogIn()
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
    }

    /** Tries to log in **/
    private fun tryToLogIn() {
        // make login button not clickable
        binding.loginBtn.isClickable = false

        // resets error message on text input layouts
        binding.usernameTil.error = null
        binding.usernameTil.isErrorEnabled = false
        binding.passwordTil.error = null
        binding.passwordTil.isErrorEnabled = false

        // analyzes wrong inputs
        if (binding.usernameEt.text.trim().isEmpty()) {
            binding.usernameTil.error = "Please input your username!"
            // make login button clickable
            binding.loginBtn.isClickable = true
            return
        }
        if (binding.usernameEt.text.trim().length < 3) {
            binding.usernameTil.error = "Your username must contain at least 3 characters!"
            // make login button clickable
            binding.loginBtn.isClickable = true
            return
        }
        if (binding.passwordEt.text.trim().isEmpty()) {
            binding.passwordTil.error = "Please input your password!"
            // make login button clickable
            binding.loginBtn.isClickable = true
            return
        }
        if (binding.passwordEt.text.trim().length < 3) {
            binding.passwordTil.error = "Your password must contain at least 3 characters!"
            // make login button clickable
            binding.loginBtn.isClickable = true
            return
        }

        // initializes user in login view model
        loginViewModel.user.value.let {
            if (it != null) {
                it.username = binding.usernameEt.text.toString()
            }
            if (it != null) {
                it.password = binding.passwordEt.text.toString()
            }
        }

        // attempt to log in inside lifecycleScope
        lifecycleScope.launch {
            binding.progressbar.visibility = View.VISIBLE
            loginViewModel.login()
        }

    }


    /** Navigates to TimeLineFragment on successful log in **/
    private fun navigateToTimelineFragmentIfLogInSuccessful() {
        loginViewModel.token.observe(viewLifecycleOwner) {
            // hide progressbar
            binding.progressbar.visibility = View.INVISIBLE
            // navigate to TimeLineFragment
            findNavController().navigate(R.id.action_loginFragment_to_timelineFragment)
            // make login button clickable
            binding.loginBtn.isClickable = true
        }
    }

    /** Hides bottom navigation **/
    private fun makeBottomNavigationGone() {
        (activity as MainActivity).getBinding().bottomNavigation.visibility = View.GONE
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}