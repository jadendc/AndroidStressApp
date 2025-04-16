import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anxietystressselfmanagement.R

class DetailsAdapter(private var items: List<Pair<String, String>>) : RecyclerView.Adapter<DetailsAdapter.DetailViewHolder>() {

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
        notifyDataSetChanged()
    }
}