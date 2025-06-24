package com.anxietystressselfmanagement

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StrategyActionDetailAdapter(
    private var items: List<StrategyActionEntry>
) : RecyclerView.Adapter<StrategyActionDetailAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView: TextView = view.findViewById(R.id.itemDateTextView)
        val strategyTextView: TextView = view.findViewById(R.id.itemStrategyTextView)
        val actionTextView: TextView = view.findViewById(R.id.itemActionTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_strategy_action_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.dateTextView.text = item.date

        // Show Strategy if available, otherwise hide the TextView
        if (!item.strategy.isNullOrBlank()) {
            holder.strategyTextView.text = "Strategy: ${item.strategy}"
            holder.strategyTextView.visibility = View.VISIBLE
        } else {
            holder.strategyTextView.visibility = View.GONE
        }

        // Show Action if available, otherwise hide the TextView
        if (!item.action.isNullOrBlank()) {
            holder.actionTextView.text = "Action: ${item.action}"
            holder.actionTextView.visibility = View.VISIBLE
        } else {
            holder.actionTextView.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<StrategyActionEntry>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}