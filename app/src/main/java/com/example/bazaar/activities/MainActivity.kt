package com.example.bazaar.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.bazaar.MyApplication
import com.example.bazaar.R
import com.example.bazaar.databinding.ActivityMainBinding
import com.example.bazaar.manager.SharedPreferencesManager
import com.example.bazaar.viewmodels.MainActivityViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setNavigationGraphAndNavigationUI()
    }

    /** set navigation graph and navigation uii **/
    private fun setNavigationGraphAndNavigationUI() {
        // find NavHostFragment
        val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        // find NavController
        navController = navHostFragment.navController
        // setup NavigationUI with NavController
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)
        // get NavGraph
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        // get token
        val token = MyApplication.sharedPreferences.getStringValue(
                SharedPreferencesManager.KEY_TOKEN,
                "Empty token!"
        )
        // set navGraph start destination
        navGraph.startDestination =
                if (token != "Empty token!") {
                    R.id.timelineFragment
                } else {
                    R.id.loginFragment
                }

        // set NavGraph dynamically to change start destination
        navController.graph = navGraph
    }

    /** Access to UI elements from fragments **/
    fun getBinding(): ActivityMainBinding {
        return binding
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (navController.currentDestination?.id == R.id.loginFragment) {
            finish()
        } else if (navController.currentDestination?.id == R.id.timelineFragment) {
            finish()
            // do nothing
        } else {
            super.onBackPressed()
        }
    }
}