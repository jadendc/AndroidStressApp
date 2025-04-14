data class FeelingDetail(
    val feelingCategory: String, // e.g., "Very Happy", "Neutral", "Sad"
    val count: Int,              // How many days had this feeling
    val specificDates: List<String>  // List of dates (e.g., "yyyy-MM-dd") with this feeling
)