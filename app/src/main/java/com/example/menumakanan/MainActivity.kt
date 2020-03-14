package com.example.menumakanan

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.menumakanan.adapter.RestaurantAdapter
import com.example.menumakanan.util.RestaurantUtil
import com.example.menumakanan.viewmodel.MainActivityViewModel
import com.firebase.ui.auth.AuthUI
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener,
    FilterDialogFragment.FilterListener,
    RestaurantAdapter.OnRestaurantSelectedListener {

    companion object {
        const val TAG = "MainActivity"
        const val RC_SIGN_IN = 9001
        const val LIMIT = 50
    }

    private lateinit var mFirestore: FirebaseFirestore
    private var mQuery: Query? = null

    private var mFilterDialog: FilterDialogFragment? = null
    private lateinit var mAdapter: RestaurantAdapter

    private lateinit var mViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        mViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        FirebaseFirestore.setLoggingEnabled(true)

        initFirestore()
        initRecyclerView()

        mFilterDialog = FilterDialogFragment()

        filter_bar.setOnClickListener(this)
        button_clear_filter.setOnClickListener(this)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add_items -> {
                onAddItemsClicked()
            }

            R.id.menu_sign_out -> {
                AuthUI.getInstance().signOut(this)
                startSignIn()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initRecyclerView() {
        if (mQuery == null) {
            Log.w(TAG,
            "No query, not iniializing Recyclerview")
        }

        mAdapter = object :RestaurantAdapter(mQuery, this@MainActivity) {
            override fun onDataChanged() {
                if (itemCount == 0) {
                    recycler_restaurants.visibility = View.GONE
                    view_empty.visibility = View.VISIBLE
                } else {
                    recycler_restaurants.visibility = View.VISIBLE
                    view_empty.visibility = View.GONE
                }
            }

            override fun onError(e: FirebaseFirestoreException) {
                // Show a snackbar on errors
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Error: check logs for info.", Snackbar.LENGTH_LONG
                ).show()
            }
        }

        recycler_restaurants.layoutManager = LinearLayoutManager(this)
        recycler_restaurants.adapter = mAdapter

    }

    override fun onStart() {
        super.onStart()

        if (shouldStartSignIn()) {
            startSignIn()
            return
        }

        onFilter(mViewModel.filters)

        mAdapter.startListening()

    }

    override fun onStop() {
        super.onStop()
        mAdapter.stopListening()
    }

    private fun initFirestore() {
        mFirestore = FirebaseFirestore.getInstance()

        mQuery = mFirestore.collection("restaurants")
            .orderBy("avgRating", Query.Direction.DESCENDING)
            .limit(LIMIT.toLong())

    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.filter_bar -> {
                onFilterClicked()
            }

            R.id.button_clear_filter -> {
                onClearFilterClicked()
            }
        }
    }


    private fun onAddItemsClicked() {

        val restaurants = mFirestore.collection("restaurants")

        for (i in 0..9) {

            val restaurant = RestaurantUtil.getRandom(this)
            restaurants.add(restaurant)

        }

    }

    override fun onFilter(filters: Filters) {
        // basic query
        var query = mFirestore.collection("restaurants") as Query

        // filter category
        if (filters.hasCategory()) {
            query = query.whereEqualTo("category", filters.category)
        }

        // filter city
        if (filters.hasCity()) {
            query = query.whereEqualTo("city", filters.city)
        }

        // filter price
        if (filters.hasPrice()) {
            query = query.whereEqualTo("price", filters.price)
        }

        // sort by
        if (filters.hasSortBy()) {
            query = query.orderBy(filters.sortBy!!, filters.sortDirection!!)
        }

        // limit items
        query = query.limit(LIMIT.toLong())

        // update query
        mQuery = query
        mAdapter.setQuery(query)

        //set header
        text_current_search.text = Html.fromHtml(filters.getSearchDescription(this))
        text_current_sort_by.text = filters.getOrderDescription(this)

        //save filter
        mViewModel.filters = filters
    }

    override fun onRestaurantSelected(restaurant: DocumentSnapshot) {
        val intent = Intent(this, RestaurantDetailActivity::class.java)
        intent.putExtra(RestaurantDetailActivity.KEY_RESTAURANT_ID, restaurant.id)
        startActivity(intent)
    }

    private fun onFilterClicked() {
        mFilterDialog?.show(supportFragmentManager, FilterDialogFragment.TAG)
    }

    private fun onClearFilterClicked() {
        mFilterDialog?.resetFilters()

        onFilter(Filters.default)

    }

    private fun shouldStartSignIn(): Boolean {
        return (!mViewModel.isSigningIn && FirebaseAuth.getInstance().currentUser == null)
    }

    private fun startSignIn() {

        val intent = AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(Collections.singletonList(
                AuthUI.IdpConfig.EmailBuilder().build()
            )).setIsSmartLockEnabled(false)
            .build()

        startActivityForResult(intent, RC_SIGN_IN)
        mViewModel.isSigningIn = true

    }


}
