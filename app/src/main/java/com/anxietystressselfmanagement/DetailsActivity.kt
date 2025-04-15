package com.anxietystressselfmanagement

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

// --- DetailViewModel ---
class DetailViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _detailsList = MutableLiveData<List<Pair<String, String>>>() // Pair of (Specific Item, Date)
    val detailsList: LiveData<List<Pair<String, String>>> = _detailsList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchDetails(category: String, type: String, startDateMillis: Long, endDateMillis: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _errorMessage.value = "User not logged in."
                _isLoading.value = false
                return@launch
            }

            val fetchedDetails = mutableListOf<Pair<String, String>>()
            val startCal = Calendar.getInstance().apply { timeInMillis = startDateMillis }
            val endCal = Calendar.getInstance().apply { timeInMillis = endDateMillis }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            // Determine Firestore fields based on type
            val generalCategoryField = if (type == "Trigger") "selectedSOTD" else "signsOption"
            val specificDetailField = if (type == "Trigger") "selectedOption" else "selectedSymptom"

            try {
                withContext(Dispatchers.IO) {
                    val tempCalendar = Calendar.getInstance().apply { time = startCal.time }

                    while (!tempCalendar.after(endCal)) {
                        val dateKey = dateFormat.format(tempCalendar.time)
                        try {
                            val docRef = firestore.collection("users").document(userId)
                                .collection("dailyLogs").document(dateKey)

                            val document = docRef.get().await()

                            if (document.exists()) {
                                val generalCategoryValue = document.getString(generalCategoryField)
                                if (generalCategoryValue == category) {
                                    val specificDetail = document.getString(specificDetailField)
                                    if (!specificDetail.isNullOrBlank()) {
                                        fetchedDetails.add(Pair(specificDetail, dateKey)) // Add specific item and date
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("DetailViewModel", "Error fetching details for date $dateKey", e)
                            // Optionally set an error message or just log and continue
                        }
                        tempCalendar.add(Calendar.DAY_OF_YEAR, 1) // Move to the next day
                    }
                }
                _detailsList.value = fetchedDetails.sortedByDescending { it.second } // Sort by date descending
            } catch (e: Exception) {
                Log.e("DetailViewModel", "Error fetching details: ${e.message}", e)
                _errorMessage.value = "Failed to load details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// --- DetailAdapter ---
class DetailAdapter(private var items: List<Pair<String, String>>) : RecyclerView.Adapter<DetailAdapter.DetailViewHolder>() {

    class DetailViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val specificItemTextView: TextView = view.findViewById(R.id.specificItemTextView)
        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
        return DetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        val item = items[position]
        holder.specificItemTextView.text = item.first // Specific item (e.g., "Bullying")
        holder.dateTextView.text = item.second // Date (e.g., "2025-04-15")
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Pair<String, String>>) {
        this.items = newItems
        notifyDataSetChanged() // Consider using DiffUtil for better performance
    }
}


// --- DetailActivity ---
class DetailActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var noDataTextView: TextView
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var detailAdapter: DetailAdapter

    private val detailViewModel: DetailViewModel by viewModels()

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
        detailAdapter = DetailAdapter(emptyList())
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