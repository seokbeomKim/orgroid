package dev.seokbeomkim.orgroid.ui.calendars

import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.findFragment
import androidx.recyclerview.widget.RecyclerView
import dev.seokbeomkim.orgroid.calendar.CalendarHelper
import dev.seokbeomkim.orgroid.calendar.CalendarItem
import dev.seokbeomkim.orgroid.databinding.CalendarsRecyclerViewRowBinding

class CalendarsRecyclerViewAdapter(val items: ArrayList<CalendarItem>) :
    RecyclerView.Adapter<CalendarsRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: CalendarsRecyclerViewRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                println("setOnClickListener: position = $adapterPosition")
            }

            binding.calendarRowEditBtn.setOnClickListener {
                val helper = CalendarHelper.getInstance()
                val contentValues =
                    helper.getCalendarContentValuesById(itemView.context, items[adapterPosition].id)

                val fragment = binding.root.findFragment<CalendarsFragment>()
                fragment.showDialogToEditCalendar(
                    itemView.context,
                    items[adapterPosition].id,
                    contentValues?.get(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME).toString(),
                    contentValues?.get(CalendarContract.Calendars.CALENDAR_COLOR).toString().toInt()
                )

                println("title: ${items[adapterPosition].displayName}")
            }

            binding.calendarRowRemoveBtn.setOnClickListener {
                println("remove button clicked: position = $adapterPosition")

                val fragment = binding.root.findFragment<CalendarsFragment>()
                fragment.showDialogToRemoveCalendar(itemView.context, items[adapterPosition].id)
                fragment.updateCalendarList()
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = CalendarsRecyclerViewRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            println("item[position] = ${items[position].displayName}")
            titleTextView.text = "title: " + items[position].displayName
            descriptionTextView.text = "description: " + items[position].accountName
        }
    }

    override fun getItemCount(): Int {
        println("items.size = ${items.size}")
        return items.size
    }
}