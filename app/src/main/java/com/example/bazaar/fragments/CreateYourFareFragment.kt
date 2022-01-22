package com.example.bazaar.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bazaar.R
import com.example.bazaar.databinding.FragmentCreateYourFareBinding
import com.example.bazaar.repository.Repository
import com.example.bazaar.viewmodels.AddProductViewModel
import com.example.bazaar.viewmodels.AddProductViewModelFactory
import kotlinx.coroutines.launch


class CreateYourFareFragment : Fragment() {


    private var _binding: FragmentCreateYourFareBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var addProductViewModel: AddProductViewModel

    companion object {
        fun newInstance(): CreateYourFareFragment {
            return CreateYourFareFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // creates addProductViewModel with factory
        val addProductViewModelFactory = AddProductViewModelFactory(this.requireContext(), Repository())
        addProductViewModel = ViewModelProvider(this, addProductViewModelFactory)[AddProductViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCreateYourFareBinding.inflate(inflater, container, false)
        val view = binding.root
        resetErrorMessageOnTextInputLayouts()
        launchMyFareBtnHandler()
        toolbarHandler()
        priceAmountSpinnerHandler()
        availableAmountSpinnerHandler()
        switchHandler()

        addProductSuccessful()
        return view
    }

    /**  Handles clicks on launchMyFare button**/
    private fun launchMyFareBtnHandler() {
        binding.launchMyFareBtn.setOnClickListener {
            tryToLaunchMyFare()
        }
    }

    /** Resets error message on double click **/
    private fun resetErrorMessageOnTextInputLayouts() {
        binding.titleEt.setOnClickListener {
            binding.titleTil.error = null
            binding.titleTil.isErrorEnabled = false
        }
        binding.priceAmountEt.setOnClickListener {
            binding.priceAmountTil.error = null
            binding.priceAmountTil.isErrorEnabled = false
        }
        binding.shortDescriptionEt.setOnClickListener {
            binding.shortDescriptionTil.error = null
            binding.shortDescriptionTil.isErrorEnabled = false
        }
    }

    /**  Tries to launch myFareFragment with lifecycleScope **/
    private fun tryToLaunchMyFare() {
        // make launch my fare button not clickable
        binding.launchMyFareBtn.isClickable = false

        // resets error message on text input layouts
        binding.titleTil.error = null
        binding.titleTil.isErrorEnabled = false
        binding.priceAmountTil.error = null
        binding.priceAmountTil.isErrorEnabled = false
        binding.shortDescriptionTil.error = null
        binding.shortDescriptionTil.isErrorEnabled = false

        // analyzes wrong inputs
        if (binding.titleEt.text.trim().isEmpty()) {
            binding.titleTil.error = "Please input the title!"
            // make launchMyFare button clickable
            binding.launchMyFareBtn.isClickable = true
            return
        }
        if (binding.priceAmountEt.text.trim().isEmpty()) {
            binding.priceAmountTil.error = "Please input the price!"
            // make launchMyFare button clickable
            binding.launchMyFareBtn.isClickable = true
            return
        }
        if (binding.shortDescriptionEt.text.trim().isEmpty()) {
            binding.shortDescriptionTil.error = "Please input the description!"
            // make launchMyFare button clickable
            binding.launchMyFareBtn.isClickable = true
            return
        }

        // initializes product in view model
        addProductViewModel.product.value.let {
            if (it != null) {
                it.title = binding.titleEt.text.toString()
                it.price_type = binding.priceAmountSp.selectedItem.toString()
                it.description = binding.shortDescriptionEt.text.toString()
                it.amount_type = binding.totalAmountSp.selectedItem.toString()
                it.rating = 0f
                it.units = binding.totalAmountEt.text.toString()
                it.price_per_unit = binding.priceAmountEt.text.toString().toInt()
                it.is_active = binding.switchAiSc.isChecked
            }
        }

        // attempt to add product inside lifecycleScope
        lifecycleScope.launch {
            addProductViewModel.addProduct()
        }

        // make launch my fare button clickable
        binding.launchMyFareBtn.isClickable = true
    }

    /** If product was added successfully**/
    private fun addProductSuccessful() {
        addProductViewModel.response.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_createYourFareFragment_to_myMarketFragment)
            binding.launchMyFareBtn.isClickable = true
        }
    }

    /** Initializes toolbar**/
    private fun toolbarHandler() {
        binding.toolbar.setNavigationIcon(R.drawable.ic_toolbar_arrow)
        binding.toolbar.setNavigationOnClickListener {
            // back button pressed
            findNavController().navigate(R.id.action_createYourFareFragment_to_myMarketFragment)
        }
    }

    /** Initialize priceAmount spinner**/
    private fun priceAmountSpinnerHandler() {
        ArrayAdapter.createFromResource(
                requireContext(),
                R.array.price_amount_array,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            binding.priceAmountSp.adapter = adapter
        }
    }

    /** Initializes availableAmount spinner **/
    private fun availableAmountSpinnerHandler() {
        ArrayAdapter.createFromResource(
                requireContext(),
                R.array.available_amount_array,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            binding.totalAmountSp.adapter = adapter
        }
    }

    /**  Controls switch **/
    private fun switchHandler() {
        binding.switchAiSc.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.switchAiTv.text = "Active"
            } else {
                binding.switchAiTv.text = "Inactive"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}