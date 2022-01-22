package com.example.bazaar.fragments

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bazaar.MyApplication
import com.example.bazaar.R
import com.example.bazaar.api.model.ProductResponse
import com.example.bazaar.api.model.User
import com.example.bazaar.databinding.FragmentProductDetailBinding
import com.example.bazaar.manager.SharedPreferencesManager
import com.example.bazaar.repository.Repository
import com.example.bazaar.viewmodels.*
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch


class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var productResponse: ProductResponse
    private lateinit var removeProductViewModel: RemoveProductViewModel
    private lateinit var addOrderViewModel: AddOrderViewModel
    private lateinit var updateProductViewModel: UpdateProductViewModel

    companion object {
        fun newInstance(): ProductDetailFragment {
            return ProductDetailFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val removeProductViewModelFactory = RemoveProductViewModelFactory(this.requireContext(), Repository())
        removeProductViewModel = ViewModelProvider(this, removeProductViewModelFactory)[RemoveProductViewModel::class.java]

        val addOrderViewModelFactory = AddOrderViewModelFactory(this.requireContext(), Repository())
        addOrderViewModel = ViewModelProvider(this, addOrderViewModelFactory)[AddOrderViewModel::class.java]

        val updateProductViewModelFactory = UpdateProductViewModelFactory(this.requireContext(), Repository())
        updateProductViewModel = ViewModelProvider(this, updateProductViewModelFactory)[UpdateProductViewModel::class.java]
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        productResponse = arguments!!.getParcelable<ProductResponse>("productResponse")!!

        viewSelection()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationIcon(R.drawable.ic_toolbar_arrow)
        binding.toolbar.setNavigationOnClickListener {
            // back button pressed
            findNavController().navigateUp()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**depending on productResponse selects from owner or user view**/
    private fun viewSelection() {
        if (productResponse != null) {
            setNeutralElements()

            if (productResponse.username == MyApplication.sharedPreferences.getUserValue(
                            SharedPreferencesManager.KEY_USER, User()).username) {
                setOwnerView()
            } else {
                setUserView()
            }
        } else {
            Log.d("ProductDetailFragment", "productResponse : NULL")
        }
    }

    /**sets owner view**/
    private fun setOwnerView() {
        hideUserElements()
        setOwnerElements()
        editBtnHandler()
    }

    /**sets user view**/
    private fun setUserView() {
        hideOwnerElements()
        setUserElements()
        addOrderBtnHandler()
    }

    /**add Order button handler**/
    private fun addOrderBtnHandler() {
        binding.cartCiv.setOnClickListener {
            openOrder()
        }
    }

    /**Opens order dialog**/
    private fun openOrder() {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_order)

        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        dialog.window!!.setLayout((7 * width) / 7, (4 * height) / 5)

        val profileNameTv: TextView = dialog.findViewById(R.id.profile_name_tv) as TextView
        val productPricePerQuantityTv: TextView = dialog.findViewById(R.id.product_price_per_quantity_tv) as TextView
        val productNameTv: TextView = dialog.findViewById(R.id.product_name_tv) as TextView
        val aiTv: TextView = dialog.findViewById(R.id.ai_tv) as TextView
        val aiIv: ImageView = dialog.findViewById(R.id.ai_iv) as ImageView
        val dateTv: TextView = dialog.findViewById(R.id.date_tv) as TextView
        val amountTil: TextInputLayout = dialog.findViewById(R.id.amount_til) as TextInputLayout
        val amountEt: EditText = dialog.findViewById(R.id.amount_et) as EditText
        val amountTv: TextView = dialog.findViewById(R.id.amount_tv) as TextView
        val commentsEt: EditText = dialog.findViewById(R.id.comments_et) as EditText
        val cancelBtn: Button = dialog.findViewById(R.id.cancel_btn) as Button
        val sendMyOderBtn: Button = dialog.findViewById(R.id.send_my_oder_btn) as Button
        val cancelIv: ImageView = dialog.findViewById(R.id.cancel_iv) as ImageView

        profileNameTv.text = productResponse.username
        productPricePerQuantityTv.text = productResponse.units + " " + productResponse.price_type + "/" + productResponse.amount_type
        productNameTv.text = productResponse.title
        if (productResponse.is_active) {
            aiTv.text = "Active"
            aiTv.setTextColor(Color.parseColor("#00B5C0"))
            aiIv.setImageResource(R.drawable.ic_checkmark)
        } else {
            aiTv.text = "Inactive"
            aiTv.setTextColor(Color.parseColor("#9A9A9A"))
            aiIv.setImageResource(R.drawable.ic_inactive)
        }

        amountTv.text = productResponse.amount_type

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        cancelIv.setOnClickListener {
            dialog.dismiss()
        }

        amountEt.setOnClickListener {
            // resets error message on text input layouts
            amountTil.error = null
            amountTil.isErrorEnabled = false
        }

        sendMyOderBtn.setOnClickListener {
            analyzeSendOrderInputs(amountTil, amountEt, sendMyOderBtn)

            if (amountTil.error == null) {

                addOrderViewModel.order.value.let {
                    if (it != null) {
                        it.description = commentsEt.text.toString()
                        it.owner_username = profileNameTv.text.toString()
                        it.price_per_unit = productResponse.units.toInt()
                        it.title = productNameTv.text.toString()
                        it.units = amountEt.text.toString().toInt()
                    }
                }
                addOrderSuccessful()
                lifecycleScope.launch {
                    addOrderViewModel.addOrder()
                }

                dialog.dismiss()
            }

        }

        dialog.show()
    }

    /**called when Order was added successfully**/
    private fun addOrderSuccessful() {
        addOrderViewModel.response.observe(viewLifecycleOwner) {
        }
    }

    /**analyzes and Sends Order Inputs**/
    private fun analyzeSendOrderInputs(amountTil: TextInputLayout, amountEt: EditText, sendMyOderBtn: Button) {
        // make login button not clickable
        sendMyOderBtn.isClickable = false

        // resets error message on text input layouts
        amountTil.error = null
        amountTil.isErrorEnabled = false

        // analyzes wrong inputs
        if (amountEt.text.trim().isEmpty()) {
            amountTil.error = "Please input the amount!"
            // make login button clickable
            sendMyOderBtn.isClickable = true
            return
        }
    }

    /**hides user elements from owner**/
    private fun hideUserElements() {
        binding.mailCiv.visibility = View.GONE
        binding.cartCiv.visibility = View.GONE
        binding.phoneCiv.visibility = View.GONE
        binding.availableAmountTv.visibility = View.GONE
        binding.rateTv.visibility = View.GONE
    }

    /**sets user elements for users**/
    private fun setUserElements() {
        binding.availableAmountTv.text = productResponse.units + " " + productResponse.amount_type
    }

    /**hides owner elements from user**/
    private fun setOwnerElements() {
        binding.totalQuantityEt.text = productResponse.units + " " + productResponse.amount_type + "."
        binding.priceEt.text = productResponse.price_per_unit + " " + productResponse.price_type
        binding.soldQuantityEt.text = "0" + " " + productResponse.amount_type + "."
        binding.revenueEt.text = "0" + " " + productResponse.price_type
    }

    /**handles edit button click**/
    private fun editBtnHandler() {
        binding.editIv.setOnClickListener {
            openProductDetailDialog()
        }
    }

    /**opens product detail dialog**/
    private fun openProductDetailDialog() {

        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_product_detail)
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        val aiSc: SwitchCompat = dialog.findViewById(R.id.ai_sc) as SwitchCompat
        val modifyBtn: Button = dialog.findViewById(R.id.modify_btn) as Button
        val deleteBtn: Button = dialog.findViewById(R.id.delete_btn) as Button
        val closeBtn: Button = dialog.findViewById(R.id.close_btn) as Button

        aiSc.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                aiSc.text = "Active"
            } else {
                aiSc.text = "Inactive"
            }
        }

        modifyBtn.setOnClickListener {

            updateProductObservable()

            updateProductViewModel.updateProductRequest.value.let {
                if (it != null) {
                    it.title = productResponse.title
                    it.rating = productResponse.rating.toFloat()
                    it.amount_type = productResponse.amount_type
                    it.is_active = aiSc.text == "Active"
                    it.price_per_unit = productResponse.price_per_unit.toInt()
                    it.price_type = productResponse.price_type
                }
            }

            lifecycleScope.launch {
                updateProductViewModel.updateProduct(productResponse.product_id)
            }

            dialog.dismiss()
        }

        deleteBtn.setOnClickListener {

            removeProductObservable()

            lifecycleScope.launch {
                removeProductViewModel.removeProduct(productResponse.product_id)
            }

            dialog.dismiss()
        }

        closeBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    /**called if remove product was successful**/
    private fun removeProductObservable() {
        removeProductViewModel.removeProductResponse.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_productDetailFragment_to_timelineFragment)
        }
    }

    /**called if update product was successful**/
    private fun updateProductObservable() {
        updateProductViewModel.updateProductResponse.observe(viewLifecycleOwner) {

            if (updateProductViewModel.updateProductResponse.value!!.updated_item.is_active) {
                binding.inactiveTv.text = "Active"
                binding.inactiveTv.setTextColor(Color.parseColor("#00B5C0"))
                binding.inactiveIm.setImageResource(R.drawable.ic_checkmark)
            } else {
                binding.inactiveTv.text = "Inactive"
                binding.inactiveTv.setTextColor(Color.parseColor("#9A9A9A"))
                binding.inactiveIm.setImageResource(R.drawable.ic_inactive)
            }
        }
    }

    /**sets neutral view elements for user and owner**/
    private fun setNeutralElements() {
        binding.profileNameTv.text = productResponse.username
        binding.productNameTv.text = productResponse.title
        binding.productPricePerQuantityTv.text = productResponse.price_per_unit + " " + productResponse.price_type + "/" + productResponse.amount_type
        binding.descriptionTv.text = productResponse.description
        if (productResponse.is_active) {
            binding.inactiveTv.text = "Active"
            binding.inactiveIm.setImageResource(R.drawable.ic_checkmark)
        } else {
            binding.inactiveTv.text = "Inactive"
            binding.inactiveIm.setImageResource(R.drawable.ic_inactive)
        }

    }

    /**hides owner elements from user**/
    private fun hideOwnerElements() {
        binding.totalQuantityEt.visibility = View.GONE
        binding.totalQuantityTitleEt.visibility = View.GONE
        binding.priceEt.visibility = View.GONE
        binding.priceTitleEt.visibility = View.GONE
        binding.soldQuantityEt.visibility = View.GONE
        binding.soldQuantityTitleEt.visibility = View.GONE
        binding.revenueEt.visibility = View.GONE
        binding.revenueTitleEt.visibility = View.GONE
        binding.editIv.visibility = View.GONE
    }
}