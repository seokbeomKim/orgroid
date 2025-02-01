package dev.seokbeomkim.orgroid.ui.calendars

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.seokbeomkim.orgroid.databinding.FragmentCalendarsBinding
import dev.seokbeomkim.orgtodo.calendar.CalendarHelper
import dev.seokbeomkim.orgtodo.calendar.CalendarItem

class CalendarsFragment : Fragment() {

    private var _binding: FragmentCalendarsBinding? = null
    private val binding get() = _binding!!

    private fun initFindButton(findButton: Button) {
        val calendarsViewModel = ViewModelProvider(this).get(CalendarsViewModel::class.java)
        val getContentByFindButton = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val (fileName, extension) = calendarsViewModel.getFileNameAndExtension(this.requireContext(), uri)
                println("File Name: $fileName")
                println("Extension: $extension")

                val fullPath = calendarsViewModel.getRealPathFromURI(this.requireContext(), uri)
                println("Full Path: $fullPath")

                calendarsViewModel.tryToParseOrgFile(fullPath)
            }
        }

        findButton.setOnClickListener {
            getContentByFindButton.launch("*/*")
        }
    }

    private fun initCalendarList(calendarRecyclerView: RecyclerView) {
        val calendarsAdapter = CalendarsRecyclerViewAdapter(CalendarHelper.getInstace().getCalendarArrayList())
        calendarRecyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        calendarRecyclerView.adapter = calendarsAdapter
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initFindButton(binding.calendarsButtonFind)
        initCalendarList(binding.calendarRecyclerView)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}