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
import com.example.bazaar.databinding.FragmentForgotPasswordBinding
import com.example.bazaar.extensionfunctions.ExtensionFunctions.isValidEmail
import com.example.bazaar.repository.Repository
import com.example.bazaar.viewmodels.ResetPasswordViewModel
import com.example.bazaar.viewmodels.ResetPasswordViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var resetPasswordViewModel: ResetPasswordViewModel

    companion object {
        fun newInstance(): ForgotPasswordFragment {
            return ForgotPasswordFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val resetPasswordViewModelFactory = ResetPasswordViewModelFactory(this.requireContext(), Repository())
        resetPasswordViewModel = ViewModelProvider(this, resetPasswordViewModelFactory)[ResetPasswordViewModel::class.java]
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        val view = binding.root
        emailMeBtnHandler()
        resetErrorMessageOnTextInputLayouts()
        resetPasswordViewModelErrorObservable()
        navigateToTimelineFragmentIfForgotPasswordSuccessful()

        return view
    }

    /** Resets error message on double click **/
    private fun resetErrorMessageOnTextInputLayouts() {
        binding.usernameEt.setOnClickListener {
            binding.usernameTil.error = null
            binding.usernameTil.isErrorEnabled = false
        }
        binding.emailEt.setOnClickListener {
            binding.emailTil.error = null
            binding.emailTil.isErrorEnabled = false
        }
    }

    /** Handles click on email button **/
    private fun emailMeBtnHandler() {
        binding.emailMeBtn.setOnClickListener {
            tryToResetPassword()
        }
    }

    /** Shows error message to user on unsuccessful Reset password attempt **/
    private fun resetPasswordViewModelErrorObservable() {
        resetPasswordViewModel.error.observe(viewLifecycleOwner) {
            // hide progressbar
            binding.progressbar.visibility = View.INVISIBLE
            // show error message
            Snackbar.make(requireView(), resetPasswordViewModel.error.value.toString(), Snackbar.LENGTH_LONG).show()
            // make email button clickable
            binding.emailMeBtn.isClickable = true
        }
    }

    /** Navigates to LoginFragment on successful password reset **/
    private fun navigateToTimelineFragmentIfForgotPasswordSuccessful() {
        resetPasswordViewModel.success.observe(viewLifecycleOwner) {
            // hide progressbar
            binding.progressbar.visibility = View.INVISIBLE
            // navigate to LoginFragment
            findNavController().navigate(R.id.action_forgotPasswordFragment_to_loginFragment)
            // make email button clickable
            binding.emailMeBtn.isClickable = true
        }
    }

    /** tries to reset the password**/
    private fun tryToResetPassword() {
        // make email button not clickable
        binding.emailMeBtn.isClickable = false

        // resets error message on text input layouts
        binding.usernameTil.error = null
        binding.usernameTil.isErrorEnabled = false
        binding.emailTil.error = null
        binding.emailTil.isErrorEnabled = false

        // analyzes wrong inputs
        if (binding.usernameEt.text.trim().isEmpty()) {
            binding.usernameTil.error = "Please input your username!"
            // make email button clickable
            binding.emailMeBtn.isClickable = true
            return
        }
        if (binding.usernameEt.text.trim().length < 3) {
            binding.usernameTil.error = "Your username must contain at least 3 characters!"
            // make email button clickable
            binding.emailMeBtn.isClickable = true
            return
        }
        if (!binding.emailEt.text.isValidEmail()) {
            binding.emailTil.error = "Please input your email!"
            // make email button clickable
            binding.emailMeBtn.isClickable = true
            return
        }

        // initializes user in reset password view model
        resetPasswordViewModel.user.value.let {
            if (it != null) {
                it.username = binding.usernameEt.text.toString()
            }
            if (it != null) {
                it.email = binding.emailEt.text.toString()
            }
        }

        // attempt to reset password in inside lifecycleScope
        lifecycleScope.launch {
            binding.progressbar.visibility = View.VISIBLE
            resetPasswordViewModel.resetPassword()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}