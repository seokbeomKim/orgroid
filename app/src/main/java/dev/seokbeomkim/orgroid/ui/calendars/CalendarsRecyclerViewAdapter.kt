package dev.seokbeomkim.orgroid.ui.calendars

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.seokbeomkim.orgroid.databinding.CalendarsRecyclerViewRowBinding
import dev.seokbeomkim.orgtodo.calendar.EventItem

class CalendarsRecyclerViewAdapter(val items: ArrayList<EventItem>)
    : RecyclerView.Adapter<CalendarsRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: CalendarsRecyclerViewRowBinding)
        : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
//                pitemClickListener?.OnItemClick(items[adapterPosition].url)
                println("setOnClickListener: position = $adapterPosition")
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CalendarsRecyclerViewAdapter.ViewHolder {
        val view = CalendarsRecyclerViewRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarsRecyclerViewAdapter.ViewHolder, position: Int) {
        holder.binding.apply {
            println("item[position] = ${items[position].getTitle()}")
            titleTextView.text = "title: " + items[position].getTitle()
            descriptionTextView.text = "description: " + items[position].getDescription()
        }
    }

    override fun getItemCount(): Int {
        println("items.size = ${items.size}")
        return items.size
    }
}