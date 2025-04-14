data class InControlDetail(
    val ratingCategory: String, // e.g., "1", "2", "3", "4", "5"
    val count: Int,             // How many days had this rating
    val specificDates: List<String> // List of dates (e.g., "yyyy-MM-dd") with this rating
)