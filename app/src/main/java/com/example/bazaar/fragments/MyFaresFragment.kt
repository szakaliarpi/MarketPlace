package com.example.bazaar.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bazaar.MarginItemDecoration
import com.example.bazaar.MyApplication
import com.example.bazaar.R
import com.example.bazaar.adapter.OrderAdapter
import com.example.bazaar.api.model.Orders
import com.example.bazaar.api.model.User
import com.example.bazaar.databinding.FragmentMyFaresBinding
import com.example.bazaar.manager.SharedPreferencesManager
import com.example.bazaar.repository.Repository
import com.example.bazaar.utils.ApiString
import com.example.bazaar.viewmodels.*
import kotlinx.coroutines.launch

class MyFaresFragment : Fragment(), OrderAdapter.ItemClickListener {

    private var _binding: FragmentMyFaresBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var getOrderViewModel: GetOrderViewModel
    private lateinit var updateOrderViewModel: UpdateOrderViewModel
    private lateinit var removeOrderViewModel: RemoveOrderViewModel

    private var recyclerViewDecorated = false
    private lateinit var recyclerView: RecyclerView
    private lateinit var orderAdapter: OrderAdapter

    companion object {
        fun newInstance(): MyFaresFragment {
            return MyFaresFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val getOrderViewModelFactory = GetOrderViewModelFactory(this.requireContext(), Repository())
        getOrderViewModel = ViewModelProvider(this, getOrderViewModelFactory)[GetOrderViewModel::class.java]

        val updateOrderViewModelFactory = UpdateOrderViewModelFactory(this.requireContext(), Repository())
        updateOrderViewModel = ViewModelProvider(this, updateOrderViewModelFactory)[UpdateOrderViewModel::class.java]

        val removeOrderViewModelFactory = RemoveOrderViewModelFactory(this.requireContext(), Repository())
        removeOrderViewModel = ViewModelProvider(this, removeOrderViewModelFactory)[RemoveOrderViewModel::class.java]
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMyFaresBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.toolbar.setNavigationIcon(R.drawable.ic_toolbar_arrow)
        binding.toolbar.setNavigationOnClickListener {
            // back button pressed
            findNavController().navigate(R.id.action_myFaresFragment_to_timelineFragment)
        }

        ordersViewModelRemoveObservable()
        updateOrderSuccessful()
        getOrdersViewModelProductsObservable(view)
        getOngoingSales()
        ordersViewModelErrorObservable()
        ordersViewModelFilterObservable()
        searchViewHandler()
        handleToggleButtons()

        return view
    }

    /**  Handles toggle buttons which control if we are in OngoingSales or OngoingOrders**/
    private fun handleToggleButtons() {
        binding.ongoingOrdersTbtn.isChecked = false
        binding.ongoingSalesTbtn.isClickable = false
        binding.ongoingOrdersTbtn.isClickable = true

        binding.ongoingSalesTbtn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Ongoing Sales
                if (binding.ongoingOrdersTbtn.isChecked) {
                    binding.ongoingOrdersTbtn.isChecked = false
                    binding.ongoingSalesTbtn.isClickable = false
                    binding.ongoingOrdersTbtn.isClickable = true
                    if (::orderAdapter.isInitialized) {
                        recyclerView.adapter = null
                    }
                    getOngoingSales()
                }
            }
        }

        binding.ongoingOrdersTbtn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Ongoing Orders
                if (binding.ongoingSalesTbtn.isChecked) {
                    binding.ongoingSalesTbtn.isChecked = false
                    binding.ongoingOrdersTbtn.isClickable = false
                    binding.ongoingSalesTbtn.isClickable = true
                    if (::orderAdapter.isInitialized) {
                        recyclerView.adapter = null
                    }
                    getOngoingOrders()
                }
            }
        }

    }

    /** Sets filters to set OngoingOrders content**/
    private fun getOngoingOrders() {
        val mapOfFilter = mutableMapOf<String, String>()

        mapOfFilter["username"] = MyApplication.sharedPreferences.getUserValue(
                SharedPreferencesManager.KEY_USER, User()).username

        val filter = ApiString.Builder()
                .map(mapOfFilter)
                .build()

        getOrderViewModel.filter.value = filter.getString();

    }

    /** Sets filters to set OngoingSales content**/
    private fun getOngoingSales() {
        val mapOfFilter = mutableMapOf<String, String>()

        mapOfFilter["owner_username"] = "\\" + "\"" + MyApplication.sharedPreferences.getUserValue(
                SharedPreferencesManager.KEY_USER, User()).username + "\\" + "\""

        /*
        mapOfFilter["owner_username"] = MyApplication.sharedPreferences.getUserValue(
                SharedPreferencesManager.KEY_USER, User()).username
        */
        val filter = ApiString.Builder()
                .map(mapOfFilter)
                .build()

        Log.d("MyFaresFragment", "getOngoingSales: " + filter.getString())


        getOrderViewModel.filter.value = filter.getString()
    }

    /**called when filter call succeeds**/
    private fun ordersViewModelFilterObservable() {
        getOrderViewModel.filter.observe(viewLifecycleOwner) {
            getOrders()
        }
    }

    /**called when error occured**/
    private fun ordersViewModelErrorObservable() {
        updateOrderViewModel.error.observe(viewLifecycleOwner) {
            getOrders()
        }
    }

    /**calls getOrder after filter was applied**/
    private fun getOrders() {
        lifecycleScope.launch {
            getOrderViewModel.getOrder()
        }
    }

    /**called when getOrderListResponse got a value**/
    private fun getOrdersViewModelProductsObservable(view: View) {
        getOrderViewModel.getOrderListResponse.observe(viewLifecycleOwner) {
            recycleViewAndAdapterHandler(view, getOrderViewModel.getOrderListResponse.value?.orders!!.toMutableList())
        }
    }

    /**called when order got deleted**/
    private fun ordersViewModelRemoveObservable() {
        removeOrderViewModel.removeOrderResponse.observe(viewLifecycleOwner) {
            orderAdapter.notifyDataSetChanged()
        }
    }

    /**initializes recycleView and adapter**/
    private fun recycleViewAndAdapterHandler(view: View, orders: MutableList<Orders>) {
//creating and setting up adapter with recyclerView
        recyclerView = binding.recyclerViewProducts

//creating and setting up adapter with recyclerView
        orderAdapter = OrderAdapter(view, this, orders, updateOrderViewModel, removeOrderViewModel, this, requireActivity()) //setting the data and listener for adapter

        val layoutManager: RecyclerView.LayoutManager =
                LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = orderAdapter

        if (!recyclerViewDecorated) {
            recyclerView.addItemDecoration(
                    MarginItemDecoration(
                            resources.getDimensionPixelSize(R.dimen.dimen_margin_horizontal_in_dp),
                            resources.getDimensionPixelSize(R.dimen.dimen_margin_vertical_in_dp)
                    )
            )
            recyclerViewDecorated = true
        }

        orderAdapter.notifyDataSetChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.inflateMenu(R.menu.app_bar_menu_market)

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.nav_settings -> {
                    // Save profile changes
                    val bundle = Bundle()
                    bundle.putString("username", MyApplication.sharedPreferences.getUserValue(
                            SharedPreferencesManager.KEY_USER, User()
                    ).username)
                    findNavController().navigate(R.id.action_myFaresFragment_to_settingsFragment, bundle)
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

    /**called when updateOrderResponse got a value**/
    private fun updateOrderSuccessful() {
        updateOrderViewModel.updateOrderResponse.observe(viewLifecycleOwner) {

        }
    }

    /**controls search view**/
    private fun searchViewHandler() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                // filters text
                if (::orderAdapter.isInitialized) {
                    orderAdapter.filter.filter(newText)
                }

                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // task HERE
                return false
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerViewDecorated = false
        _binding = null
    }


    override fun onItemClick(position: Int) {

    }

}