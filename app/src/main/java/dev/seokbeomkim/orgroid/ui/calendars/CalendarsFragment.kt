package dev.seokbeomkim.orgroid.ui.calendars

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorListener
import dev.seokbeomkim.orgroid.calendar.CalendarHelper
import dev.seokbeomkim.orgroid.databinding.FragmentCalendarsBinding


class CalendarsFragment : Fragment() {

    private var _binding: FragmentCalendarsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CalendarsViewModel by lazy {
        ViewModelProvider(this).get(CalendarsViewModel::class.java)
    }

    private fun initFindButton(findButton: Button) {
        val getContentByFindButton =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    val (_, _) = viewModel.getFileNameAndExtension(
                        this.requireContext(),
                        uri
                    )
                    val fullPath = viewModel.getRealPathFromURI(this.requireContext(), uri)
                    if (viewModel.tryToParseOrgFile(this.requireContext(), fullPath)) {
                        showDialogToCreateCalendar(this.requireContext())
                    } else {
                        AlertDialog.Builder(this.requireContext())
                            .setTitle("Error")
                            .setMessage("Failed to parse the org file. Please check the file.")
                            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                            .show()
                    }
                }
            }

        findButton.setOnClickListener {
            getContentByFindButton.launch("*/*")
        }
    }

    fun updateCalendarList() {
        initCalendarList(binding.calendarRecyclerView)
    }

    fun showDialogToEditCalendar(
        context: Context,
        calendarId: Long,
        calendarName: String,
        calendarColor: Int
    ) {
        val dialogToEditCalendar =
            layoutInflater.inflate(dev.seokbeomkim.orgroid.R.layout.dialog_create_calendar, null)

        val calendarColorHolder = intArrayOf(calendarColor)

        dialogToEditCalendar.findViewById<EditText>(dev.seokbeomkim.orgroid.R.id.edit_text_calendar_name)
            .setText(calendarName)
        dialogToEditCalendar.findViewById<View>(dev.seokbeomkim.orgroid.R.id.view_calendar_color)
            .setBackgroundColor(calendarColor)

        dialogToEditCalendar.findViewById<View>(dev.seokbeomkim.orgroid.R.id.view_calendar_color)
            .setOnClickListener {
                val builder = ColorPickerDialog.Builder(context)
                    .setTitle("Select a calendar color")
                    .setPositiveButton(
                        getString(dev.seokbeomkim.orgroid.R.string.confirm)
                    ) { dialog, selectedColor -> dialog.dismiss() }
                    .setNegativeButton(
                        getString(dev.seokbeomkim.orgroid.R.string.cancel)
                    ) { dialog, _ -> dialog.dismiss() }
                    .attachAlphaSlideBar(false)
                    .attachBrightnessSlideBar(true) // the default value is true.
                    .setBottomSpace(12)

                val colorPickerView = builder.getColorPickerView()
                colorPickerView.setInitialColor(calendarColorHolder[0])
                colorPickerView.setColorListener(ColorListener { color, _ ->
                    Log.d("Orgroid", "Color changed: $color")
                    dialogToEditCalendar.findViewById<View>(dev.seokbeomkim.orgroid.R.id.view_calendar_color)
                        .setBackgroundColor(color)
                    calendarColorHolder[0] = color
                })

                builder.show()
            }

        val builder = AlertDialog.Builder(context)
            .setView(dialogToEditCalendar)
            .setPositiveButton(
                "Edit",
                { dialog, _ ->
                    viewModel.editCalendar(
                        this.requireContext(),
                        calendarId,
                        dialogToEditCalendar.findViewById<EditText>(dev.seokbeomkim.orgroid.R.id.edit_text_calendar_name).text.toString(),
                        calendarColorHolder[0]
                    )
                    dialog.dismiss()
                    updateCalendarList()
                })
            .setNegativeButton(
                "Cancel",
                { dialog, _ -> dialog.dismiss() }
            )
            .create()

        builder.show()
    }

    private fun showDialogToCreateCalendar(context: Context) {

        val dialogToCreateCalendar =
            layoutInflater.inflate(dev.seokbeomkim.orgroid.R.layout.dialog_create_calendar, null)

        val selectedColor =
            dialogToCreateCalendar.findViewById<View>(dev.seokbeomkim.orgroid.R.id.view_calendar_color).background as android.graphics.drawable.ColorDrawable

        dialogToCreateCalendar.findViewById<View>(dev.seokbeomkim.orgroid.R.id.view_calendar_color)
            .setOnClickListener {
                val builder = ColorPickerDialog.Builder(context)
                    .setTitle("Select a calendar color")
                    .setPositiveButton(
                        getString(dev.seokbeomkim.orgroid.R.string.confirm)
                    ) { dialog, selectedColor -> dialog.dismiss() }
                    .setNegativeButton(
                        getString(dev.seokbeomkim.orgroid.R.string.cancel)
                    ) { dialog, _ -> dialog.dismiss() }
                    .attachAlphaSlideBar(false)
                    .attachBrightnessSlideBar(true) // the default value is true.
                    .setBottomSpace(12)

                val colorPickerView = builder.getColorPickerView()
                colorPickerView.setInitialColor(selectedColor.color)
                colorPickerView.setColorListener(ColorListener { color, _ ->
                    Log.d("Orgroid", "Color changed: $color")
                    dialogToCreateCalendar.findViewById<View>(dev.seokbeomkim.orgroid.R.id.view_calendar_color)
                        .setBackgroundColor(color)
                })

                builder.show()
            }

        val builder = AlertDialog.Builder(context)
            .setView(dialogToCreateCalendar)
            .setPositiveButton(
                "Create",
                { dialog, _ ->
                    val calendarName =
                        dialogToCreateCalendar.findViewById<EditText>(dev.seokbeomkim.orgroid.R.id.edit_text_calendar_name).text.toString()
                    val calendarColor = selectedColor.color

                    viewModel.createCalendar(
                        this.requireContext(),
                        calendarName,
                        calendarColor
                    )
                    dialog.dismiss()
                    updateCalendarList()
                })
            .setNegativeButton(
                "Cancel",
                { dialog, _ -> dialog.dismiss() }
            )
            .create()

        builder.show()
    }

    private fun initCalendarList(calendarRecyclerView: RecyclerView) {
        val calendarsAdapter =
            CalendarsRecyclerViewAdapter(CalendarHelper.getInstance().getCalendarArrayList())
        calendarRecyclerView.layoutManager = LinearLayoutManager(this.requireContext())
        calendarRecyclerView.adapter = calendarsAdapter

        if (calendarsAdapter.itemCount == 0) {
            binding.textView.text =
                getString(dev.seokbeomkim.orgroid.R.string.calendars_label_no_calendars)
        } else {
            binding.textView.text =
                getString(dev.seokbeomkim.orgroid.R.string.calendars_label_calendar_list)
        }
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