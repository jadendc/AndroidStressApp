package com.anxietystressselfmanagement

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class StrategyActionDetailActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var noDataTextView: TextView
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var detailAdapter: StrategyActionDetailAdapter

    private val detailViewModel: StrategyActionDetailViewModel by viewModels()

    companion object {
        private const val EXTRA_START_DATE = "com.anxietystressselfmanagement.EXTRA_START_DATE_SA"
        private const val EXTRA_END_DATE = "com.anxietystressselfmanagement.EXTRA_END_DATE_SA"

        fun newIntent(context: Context, startDateMillis: Long, endDateMillis: Long): Intent {
            return Intent(context, StrategyActionDetailActivity::class.java).apply {
                putExtra(EXTRA_START_DATE, startDateMillis)
                putExtra(EXTRA_END_DATE, endDateMillis)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_strategy_action_detail)

        toolbar = findViewById(R.id.toolbar_sa_detail)
        recyclerView = findViewById(R.id.saDetailRecyclerView)
        loadingIndicator = findViewById(R.id.loadingIndicator_sa_detail)
        noDataTextView = findViewById(R.id.noDataTextView_sa_detail)

        setupToolbar()
        setupRecyclerView()

        val startDateMillis = intent.getLongExtra(EXTRA_START_DATE, -1)
        val endDateMillis = intent.getLongExtra(EXTRA_END_DATE, -1)

        if (startDateMillis == -1L || endDateMillis == -1L) {
            Log.e("StratActDetailActivity", "Missing required date extras.")
            Toast.makeText(this, "Error: Date range not provided.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val startDate = dateFormat.format(Date(startDateMillis))
        val endDate = dateFormat.format(Date(endDateMillis))
        supportActionBar?.title = "Log ($startDate - $endDate)"

        observeViewModel()
        // Only fetch if not already loading (e.g., on config change)
        if (detailViewModel.isLoading.value != true) {
            detailViewModel.fetchStrategyActionDetails(startDateMillis, endDateMillis)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupRecyclerView() {
        detailAdapter = StrategyActionDetailAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = detailAdapter
    }

    private fun observeViewModel() {
        detailViewModel.detailsList.observe(this, Observer { details ->
            details ?: return@Observer // Ignore null list updates
            Log.d("StratActDetailActivity", "detailsList Observer triggered. Count: ${details.size}")

            detailAdapter.updateData(details) // Update adapter always

            if (details.isNotEmpty()) {
                recyclerView.visibility = View.VISIBLE
                noDataTextView.visibility = View.GONE
            } else {
                recyclerView.visibility = View.GONE
                noDataTextView.visibility = View.VISIBLE
                noDataTextView.text = "No Strategy or Action entries found for this period."
            }
        })

        detailViewModel.isLoading.observe(this, Observer { isLoading ->
            // This observer only handles the ProgressBar
            isLoading ?: return@Observer
            Log.d("StratActDetailActivity", "isLoading Observer triggered. State: $isLoading")
            loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE

            if (isLoading) {
                recyclerView.visibility = View.GONE
                noDataTextView.visibility = View.GONE
            }
        })

        detailViewModel.errorMessage.observe(this, Observer { error ->
            // This observer handles displaying errors
            error ?: return@Observer
            Log.e("StratActDetailActivity", "errorMessage Observer triggered. Error: $error")
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            recyclerView.visibility = View.GONE // Hide list on error
            noDataTextView.visibility = View.VISIBLE // Show noData view
            noDataTextView.text = "Error loading details." // Set error text
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}