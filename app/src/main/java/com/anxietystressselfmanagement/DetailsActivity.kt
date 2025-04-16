package com.anxietystressselfmanagement

import DetailsAdapter
import DetailsViewModel
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast

class DetailActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var noDataTextView: TextView
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var detailAdapter: DetailsAdapter

    private val detailViewModel: DetailsViewModel by viewModels()

    companion object {
        const val EXTRA_CATEGORY = "com.anxietystressselfmanagement.EXTRA_CATEGORY"
        const val EXTRA_TYPE = "com.anxietystressselfmanagement.EXTRA_TYPE" // "Trigger" or "Sign"
        const val EXTRA_START_DATE = "com.anxietystressselfmanagement.EXTRA_START_DATE"
        const val EXTRA_END_DATE = "com.anxietystressselfmanagement.EXTRA_END_DATE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        toolbar = findViewById(R.id.toolbar_detail)
        recyclerView = findViewById(R.id.detailRecyclerView)
        loadingIndicator = findViewById(R.id.loadingIndicator_detail)
        noDataTextView = findViewById(R.id.noDataTextView_detail)

        setupToolbar()
        setupRecyclerView()

        val category = intent.getStringExtra(EXTRA_CATEGORY)
        val type = intent.getStringExtra(EXTRA_TYPE)
        val startDateMillis = intent.getLongExtra(EXTRA_START_DATE, -1)
        val endDateMillis = intent.getLongExtra(EXTRA_END_DATE, -1)

        if (category == null || type == null || startDateMillis == -1L || endDateMillis == -1L) {
            Log.e("DetailActivity", "Missing required intent extras.")
            Toast.makeText(this, "Error: Could not load details.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        supportActionBar?.title = "$category Details" // Set title

        observeViewModel()
        detailViewModel.fetchDetails(category, type, startDateMillis, endDateMillis)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupRecyclerView() {
        detailAdapter = DetailsAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = detailAdapter
    }

    private fun observeViewModel() {
        detailViewModel.detailsList.observe(this, Observer { details ->
            if (details.isNotEmpty()) {
                detailAdapter.updateData(details)
                recyclerView.visibility = View.VISIBLE
                noDataTextView.visibility = View.GONE
            } else {
                recyclerView.visibility = View.GONE
                noDataTextView.visibility = View.VISIBLE
                noDataTextView.text = "No specific details found for this category in the selected period."
            }
        })

        detailViewModel.isLoading.observe(this, Observer { isLoading ->
            loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) {
                recyclerView.visibility = View.GONE
                noDataTextView.visibility = View.GONE
            }
        })

        detailViewModel.errorMessage.observe(this, Observer { error ->
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                recyclerView.visibility = View.GONE
                noDataTextView.visibility = View.VISIBLE
                noDataTextView.text = "Error loading details."
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish() // Close this activity and return to the previous one (Dashboard)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}