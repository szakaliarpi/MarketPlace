package com.example.bazaar.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
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
import com.example.bazaar.adapter.ProductAdapter
import com.example.bazaar.api.model.ProductResponse
import com.example.bazaar.api.model.User
import com.example.bazaar.databinding.FragmentMyMarketBinding
import com.example.bazaar.manager.SharedPreferencesManager
import com.example.bazaar.repository.Repository
import com.example.bazaar.utils.ApiString
import com.example.bazaar.viewmodels.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


class MyMarketFragment : Fragment(), ProductAdapter.ItemClickListener {

    private var _binding: FragmentMyMarketBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView

    private lateinit var refreshTokenViewModel: RefreshTokenViewModel
    private lateinit var productsViewModel: ProductsViewModel
    private lateinit var productAdapter: ProductAdapter
    private var recyclerViewDecorated = false

    companion object {
        fun newInstance(): MyMarketFragment {
            return MyMarketFragment()
        }
    }

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
        _binding = FragmentMyMarketBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.toolbar.setNavigationIcon(R.drawable.ic_toolbar_arrow)
        binding.toolbar.setNavigationOnClickListener {
            // back button pressed
            findNavController().navigate(R.id.action_myMarketFragment_to_timelineFragment)
        }
        createYourFareFABHandler()
        getProductsViewModelProductsObservable(view)
        getProductsViewModelErrorObservable()
        getProducts()
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
                }

                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // task HERE
                return false
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // handle toolbar, set menu for toolbar
        binding.toolbar.inflateMenu(R.menu.app_bar_menu_market)
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.nav_settings -> {
                    // Save profile changes
                    val bundle = Bundle()
                    bundle.putString("username", MyApplication.sharedPreferences.getUserValue(
                            SharedPreferencesManager.KEY_USER, User()).username)
                    findNavController().navigate(R.id.action_myMarketFragment_to_settingsFragment, bundle)
                    true
                }
                R.id.nav_search -> {
                    // Save profile changes
                    if (binding.searchView.visibility == View.GONE) {
                        binding.searchView.visibility = View.VISIBLE
                    } else {
                        binding.searchView.visibility = View.GONE
                    }
                    true
                }
                else -> false
            }
        }

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

    /**called when products got a value**/
    private fun getProductsViewModelProductsObservable(view: View) {
        productsViewModel.products.observe(viewLifecycleOwner) {
            val viewModel: MainActivityViewModel by activityViewModels()
            viewModel.products = productsViewModel.products.value

            recycleViewAndAdapterHandler(view, productsViewModel.products.value?.products!!.toMutableList())
        }
    }

    /** Shows error message to user on unsuccessful get products **/
    private fun getProductsViewModelErrorObservable() {
        productsViewModel.error.observe(viewLifecycleOwner) {

            //refresh token
            if (productsViewModel.error.value == "302") {
                lifecycleScope.launch {
                    refreshTokenViewModel.refreshToken()
                    getProducts()
                }
            }

            //navigate back to login fragment if token is invalid without point of return
            if (productsViewModel.error.value == "301") {
                findNavController().navigate(R.id.action_timelineFragment_to_loginFragment_without_keeping)
                MyApplication.sharedPreferences.putStringValue(SharedPreferencesManager.KEY_TOKEN, "Empty token!")
                MyApplication.sharedPreferences.putUserValue(SharedPreferencesManager.KEY_USER, User())
            }

            Snackbar.make(requireView(), productsViewModel.error.value.toString(), Snackbar.LENGTH_LONG).show()
        }
    }

    /**calls getProduct with filter**/
    private fun getProducts() {
        lifecycleScope.launch {

            val mapOfFilters = mutableMapOf<String, String>()
            mapOfFilters["username"] = MyApplication.sharedPreferences.getUserValue(SharedPreferencesManager.KEY_USER, User()).username

            val filter = ApiString.Builder()
                    .map(mapOfFilters)
                    .build()

            productsViewModel.filter.value = filter.getString()

            productsViewModel.getProducts()
        }
    }

    /**createYourFare Floating action button handler**/
    private fun createYourFareFABHandler() {
        binding.createYourFragmentFab.setOnClickListener {
            findNavController().navigate(R.id.action_myMarketFragment_to_createYourFareFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerViewDecorated = false
        _binding = null
    }

    /**called if an adapter item is clicked**/
    override fun onItemClick(position: Int) {
        val bundle = Bundle()
        bundle.putParcelable("productResponse", productAdapter.getItemData(position))
        findNavController().navigate(R.id.action_myMarketFragment_to_productDetailFragment, bundle)
    }


}