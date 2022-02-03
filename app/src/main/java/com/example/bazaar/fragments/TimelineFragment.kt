package com.example.bazaar.fragments

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bazaar.MarginItemDecoration
import com.example.bazaar.MyApplication
import com.example.bazaar.R
import com.example.bazaar.activities.MainActivity
import com.example.bazaar.adapter.ProductAdapter
import com.example.bazaar.api.model.ProductResponse
import com.example.bazaar.api.model.User
import com.example.bazaar.databinding.FragmentTimelineBinding
import com.example.bazaar.manager.SharedPreferencesManager
import com.example.bazaar.repository.Repository
import com.example.bazaar.utils.ApiString
import com.example.bazaar.viewmodels.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


class TimelineFragment : Fragment(), ProductAdapter.ItemClickListener {

    private var _binding: FragmentTimelineBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    companion object {
        fun newInstance(): TimelineFragment {
            return TimelineFragment()
        }
    }

    private lateinit var productsViewModel: ProductsViewModel
    private lateinit var refreshTokenViewModel: RefreshTokenViewModel
    private var recyclerViewDecorated = false

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // creates ProductsViewModel with factory
        val productsViewModelFactory = ProductsViewModelFactory(this.requireContext(), Repository())
        productsViewModel = ViewModelProvider(this, productsViewModelFactory)[ProductsViewModel::class.java]

        val refreshTokenViewModelFactory = RefreshTokenViewModelFactory(this.requireContext(), Repository())
        refreshTokenViewModel = ViewModelProvider(this, refreshTokenViewModelFactory)[RefreshTokenViewModel::class.java]

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        val view = binding.root

        Log.d("TimelineFragment", MyApplication.sharedPreferences.getStringValue(SharedPreferencesManager.KEY_TOKEN, "Empty token!").toString())

        getProducts()

        getProductsViewModelErrorObservable()
        getProductsViewModelProductsObservable(view)
        makeBottomNavigationVisible()
        arrayAdapterHandler()
        productsViewModelSortObservable()
        productsViewModelFilterObservable()
        searchViewHandler()

        return view
    }

    /**controls search view**/
    private fun searchViewHandler() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                // filters text
                if (::productAdapter.isInitialized) {
                    productAdapter.filter.filter(newText)

                    // wait a little so the count is correct
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.numberOfFairsTv.text = productAdapter.productsFilterList.count().toString() + " Fairs"
                    }, 100)
                }

                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // task HERE
                return false
            }

        })
    }

    /**Initializes spinner with adapter**/
    private fun arrayAdapterHandler() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
                requireContext(),
                R.array.spinner_mode,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            binding.spinner.adapter = adapter
        }

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        // sort by time
                        val mapOfSort = mutableMapOf<String, String>()
                        mapOfSort["creation_time"] = "-1"

                        val sorter = ApiString.Builder()
                                .map(mapOfSort)
                                .build()

                        productsViewModel.sort.value = sorter.getString()
                        true
                    }
                    1 -> {
                        // sort by usename
                        if (productsViewModel.sort.value != null) {
                            val mapOfSort = mutableMapOf<String, String>()
                            mapOfSort["username"] = "1"

                            val sorter = ApiString.Builder()
                                    .map(mapOfSort)
                                    .build()

                            productsViewModel.sort.value = sorter.getString()
                        }

                        true
                    }
                    2 -> {
                        // sort by title
                        val mapOfSort = mutableMapOf<String, String>()
                        mapOfSort["title"] = "1"

                        val sorter = ApiString.Builder()
                                .map(mapOfSort)
                                .build()

                        productsViewModel.sort.value = sorter.getString()
                        true
                    }
                    else -> false
                }
            }

        }
    }

    /** called when sort was successful **/
    private fun productsViewModelSortObservable() {
        productsViewModel.sort.observe(viewLifecycleOwner) {
            getProducts()
        }
    }

    /** called when filter was successful **/
    private fun productsViewModelFilterObservable() {
        productsViewModel.filter.observe(viewLifecycleOwner) {
            getProducts()
        }
    }

    /** Shows error message to user on unsuccessful get products **/
    private fun getProductsViewModelErrorObservable() {
        productsViewModel.error.observe(viewLifecycleOwner) {
            // show error message

            if (productsViewModel.error.value == "302") {
                lifecycleScope.launch {
                    refreshTokenViewModel.refreshToken()
                    getProducts()
                }
            }

            if (productsViewModel.error.value == "301") {
                findNavController().navigate(R.id.action_timelineFragment_to_loginFragment_without_keeping)
                MyApplication.sharedPreferences.putStringValue(SharedPreferencesManager.KEY_TOKEN, "Empty token!")
                MyApplication.sharedPreferences.putUserValue(SharedPreferencesManager.KEY_USER, User())
            }

            Snackbar.make(requireView(), productsViewModel.error.value.toString(), Snackbar.LENGTH_LONG).show()
        }
    }

    /** called when products data was obtained successfully **/
    private fun getProductsViewModelProductsObservable(view: View) {
        productsViewModel.products.observe(viewLifecycleOwner) {

            val viewModel: MainActivityViewModel by activityViewModels()
            viewModel.products = productsViewModel.products.value

            binding.numberOfFairsTv.text = viewModel.products?.item_count.toString() + " Fairs"

            recycleViewAndAdapterHandler(view, productsViewModel.products.value?.products!!.toMutableList())
        }
    }

    /** attempt to get products **/
    private fun getProducts() {
        // attempt to get products inside lifecycleScope
        lifecycleScope.launch {
            productsViewModel.getProducts()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initializes toolbar, with menu
        binding.toolbar.inflateMenu(R.menu.app_bar_menu)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.nav_search -> {
                    if (binding.searchView.visibility == View.GONE) {
                        binding.searchView.visibility = View.VISIBLE
                    } else {
                        binding.searchView.visibility = View.GONE
                    }

                    true
                }
                R.id.nav_filter -> {
                    showFilterDialog()
                    true
                }
                R.id.nav_settings -> {
                    val bundle = Bundle()
                    bundle.putString("username", MyApplication.sharedPreferences.getUserValue(
                            SharedPreferencesManager.KEY_USER, User()).username)
                    findNavController().navigate(R.id.action_timelineFragment_to_settingsFragment, bundle)
                    true
                }
                else -> false
            }
        }

    }

    /** shows filter dialog **/
    private fun showFilterDialog() {
        val dialog = Dialog(requireActivity())
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_timeline_filter)
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        val titleEt: EditText = dialog.findViewById(R.id.title_et)
        val priceEt: EditText = dialog.findViewById(R.id.price_et)
        val unitEt: EditText = dialog.findViewById(R.id.unit_et)
        val unitsEt: EditText = dialog.findViewById(R.id.units_et)
        val amountEt: EditText = dialog.findViewById(R.id.amount_et)

        val filterBtn: Button = dialog.findViewById(R.id.filter_btn) as Button
        filterBtn.setOnClickListener {

            val mapOfFilter = mutableMapOf<String, String>()

            if (titleEt.text.trim().isNotEmpty()) {
                mapOfFilter["title"] = titleEt.text.toString()
            }
            if (priceEt.text.trim().isNotEmpty()) {
                mapOfFilter["price_per_unit"] = priceEt.text.toString()
            }
            if (unitEt.text.trim().isNotEmpty()) {
                mapOfFilter["price_type"] = unitEt.text.toString()
            }
            if (unitsEt.text.trim().isNotEmpty()) {
                mapOfFilter["units"] = unitsEt.text.toString()
            }
            if (amountEt.text.trim().isNotEmpty()) {
                mapOfFilter["amount_type"] = amountEt.text.toString()
            }

            val filter = ApiString.Builder()
                    .map(mapOfFilter)
                    .build()

            if (filter.getString().trim().isNotEmpty()) {
                productsViewModel.filter.value = filter.getString()
            } else {
                productsViewModel.filter.value = ""
            }

            dialog.dismiss()
        }

        val closeBtn: Button = dialog.findViewById(R.id.close_btn) as Button
        closeBtn.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    /**initializes recycleView and adapter**/
    private fun recycleViewAndAdapterHandler(view: View, products: MutableList<ProductResponse>) {
        //creating and setting up adapter with recyclerView
        recyclerView = binding.recyclerViewProducts

        //creating and setting up adapter with recyclerView
        productAdapter = ProductAdapter(view, this, products) //setting the data and listener for adapter

        val layoutManager: RecyclerView.LayoutManager =
                LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = productAdapter

        //gets called only when it wasn't applied before
        if (!recyclerViewDecorated) {
            recyclerView.addItemDecoration(
                    MarginItemDecoration(
                            resources.getDimensionPixelSize(R.dimen.dimen_margin_horizontal_in_dp),
                            resources.getDimensionPixelSize(R.dimen.dimen_margin_vertical_in_dp)
                    )
            )
            recyclerViewDecorated = true
        }

        productAdapter.notifyDataSetChanged()
    }

    /**make buttom navigation visible**/
    private fun makeBottomNavigationVisible() {
        (activity as MainActivity).getBinding().bottomNavigation.visibility = View.VISIBLE
    }

    /**called if an adapter item is clicked**/
    override fun onItemClick(position: Int) {
        val bundle = Bundle()
        bundle.putParcelable("productResponse", productAdapter.getItemData(position))
        findNavController().navigate(R.id.action_timelineFragment_to_productDetailFragment, bundle)
    }



}