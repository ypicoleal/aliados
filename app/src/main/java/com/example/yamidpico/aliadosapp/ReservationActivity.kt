package com.example.yamidpico.aliadosapp

import android.app.Activity
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.DatePicker
import com.example.yamidpico.aliadosapp.adapter.ClientListAdapter
import com.example.yamidpico.aliadosapp.email.EmailManager
import com.example.yamidpico.aliadosapp.model.Calendar
import com.example.yamidpico.aliadosapp.model.Client
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_reservation.*
import kotlinx.android.synthetic.main.client_form.view.*
import kotlinx.android.synthetic.main.content_reservation.*
import kotlinx.android.synthetic.main.reservation_calendar.*
import kotlinx.android.synthetic.main.reservation_form.*
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent

class ReservationActivity : AppCompatActivity(), ChildEventListener, ClientListAdapter.OnDeleteClickListener,
    DatePickerDialog.OnDateSetListener {

    private var mAuth: FirebaseAuth? = null
    private var calendarReference = FirebaseDatabase.getInstance().getReference("calendar")
    private val serviceReference by lazy { calendarReference.child(service) }

    private lateinit var service: String

    private lateinit var currentDate: Date

    private val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    private val contentView by lazy {
        View.inflate(this, R.layout.client_form, null).also {
            it.birthdate.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) this.datePickerDialog.show()
            }
        }
    }

    private val emailManager by lazy { EmailManager() }

    private val datePickerDialog by lazy {
        val today = java.util.Calendar.getInstance()
        DatePickerDialog(
            this,
            this,
            today.get(java.util.Calendar.YEAR),
            today.get(java.util.Calendar.MONTH),
            today.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    private val dialog by lazy {

        BottomSheetDialog(this).apply {
            setContentView(contentView)

            contentView.clientAdd.setOnClickListener {
                val client = Client(
                    contentView.clientName.text.toString(),
                    contentView.birthdate.text.toString(),
                    contentView.citizenship.text.toString(),
                    contentView.hotel.text.toString(),
                    contentView.document.text.toString(),
                    contentView.email.text.toString()
                )
                clientList.add(client)
                adapter.notifyDataSetChanged()
                dismiss()

                contentView.clientName.text = null
                contentView.birthdate.text = null
                contentView.citizenship.text = null
                contentView.hotel.text = null
                contentView.document.text = null
                contentView.email.text = null
            }
        }
    }

    private val clientList = mutableListOf<Client>()
    private val adapter = ClientListAdapter(this).apply { updateItems(clientList) }

    private var calendarItem: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reservation)

        mAuth = FirebaseAuth.getInstance()

        service = intent.getStringExtra("service")

        setupCalendarView()

        exit.setOnClickListener { finish() }

        serviceReference.addChildEventListener(this)

        addReservation.setOnClickListener { setupForm() }

        addClient.setOnClickListener { dialog.show() }

        clients.adapter = adapter
        clients.isNestedScrollingEnabled = false

        addBtn.setOnClickListener { saveReservation() }
    }

    override fun onDeleteClick(position: Int) {
        clientList.removeAt(position)
        adapter.notifyDataSetChanged()
    }

    private fun setupForm() {
        reservationForm.visibility = View.VISIBLE
        reservationCalendar.visibility = View.GONE
        addBtn.visibility = View.VISIBLE
        addReservation.hide()

        date.text = formatter.format(currentDate)
    }

    private fun setupCalendarView() {
        currentDate = java.util.Calendar.getInstance().time
        val date = calendar.firstDayOfCurrentMonth
        val monthFormatter = SimpleDateFormat("MMMM / y", Locale.getDefault())

        calendar_indicator.text = monthFormatter.format(date).capitalize()

        val names = Array(7) { "" }
        names[0] = "Lun" // monday
        names[1] = "Mar" // tuesday
        names[2] = "Mie" // wednesday
        names[3] = "Jue" // thursday
        names[4] = "Vie" // friday
        names[5] = "Sab" // saturday
        names[6] = "Dom" // sunday
        calendar.setDayColumnNames(names)

        calendar.setListener(object : CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                currentDate = dateClicked
                val events = calendar.getEvents(dateClicked)
                if (events != null && events.isNotEmpty()) {
                    setupEvent(events[0])
                } else {
                    details.visibility = View.GONE
                    no_service.visibility = View.VISIBLE
                    addReservation.hide()
                }
            }

            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                calendar_indicator.text = monthFormatter.format(firstDayOfNewMonth).capitalize()
                Log.d(javaClass.simpleName, "Month was scrolled to: $firstDayOfNewMonth")
            }
        })

        calendar_prev.setOnClickListener {
            calendar.scrollLeft()
        }

        calendar_next.setOnClickListener {
            calendar.scrollRight()
        }
    }

    private fun setupEvent(event: Event) {
        details.visibility = View.VISIBLE
        no_service.visibility = View.GONE
        addReservation.show()

        calendarItem = event.data as? Calendar ?: return

        sugestPriceText.text = calendarItem?.suggestPrice.toString()
        sugestPriceChildText.text = calendarItem?.suggestPriceChild.toString()
        minPriceText.text = calendarItem?.minPrice.toString()
        minPriceChildText.text = calendarItem?.minPriceChild.toString()
        selectedBoats.text = calendarItem?.boats?.joinToString()
        capacityText.text = calendarItem?.capacity.toString()
    }

    private fun saveReservation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, Array(1) { android.Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1)
            return
        }
        val uid = mAuth?.currentUser?.uid ?: return
        if (!formIsValid()) return
        loading.visibility = View.VISIBLE
        serviceReference.child(formatter.format(currentDate)).child("reservations").child(uid).push().setValue(
            hashMapOf(
                "finalPrice" to finalPrice.text.toString().toInt(),
                "finalPriceChild" to finalPriceChild.text.toString().toInt(),
                "paidAmount" to paidAmount.text.toString().toIntOrNull(),
                "transportIncluded" to transport.isChecked,
                "clients" to clientList
            )
        ) { _, reference ->
            Log.e("tales5", reference.key)
            clientList.forEach {
                emailManager.sendMessage(it.email, "Codigo de reserva", getString(R.string.email_body, formatter.format(currentDate)), "${reference.key}/${it.document}")
            }
            finish()
        }
    }

    private fun formIsValid(): Boolean {
        val calendar = calendarItem ?: return false

        var errorText = ""
        var isValid = true
        when {
            finalPrice.text.isEmpty() -> {
                errorText = "Debe ingresar el precio final"
                isValid = false
            }
            finalPriceChild.text.isEmpty() -> {
                errorText = "Debe ingresar el precio final para niños"
                isValid = false
            }
            finalPrice.text.toString().toInt() < calendar.minPrice -> {
                errorText = "El precio final no puede ser menor al precio minimo de ${calendarItem?.minPrice}"
                isValid = false
            }
            clientList.size <= 0 -> {
                errorText = "Debe ingresar al menos un cliente"
                isValid = false
            }
        }

        if (!isValid) Snackbar.make(finalPrice, errorText, Snackbar.LENGTH_LONG).show()

        return isValid
    }


    override fun onCancelled(error: DatabaseError) = Unit

    override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) = Unit

    override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
        var reserved = 0
        dataSnapshot.child("reservations").children.forEach {
            reserved += it.child("clients").childrenCount.toInt()
        }


        val calendarObj = dataSnapshot.getValue(Calendar::class.java).also { it?.date = dataSnapshot.key.toString() }

        calendar.removeEvents(formatter.parse(calendarObj?.date).time)
        val ev1 = Event(
            ContextCompat.getColor(this@ReservationActivity, R.color.orange),
            formatter.parse(calendarObj?.date).time,
            calendarObj
        )
        calendar.addEvent(ev1)

        if (formatter.format(currentDate) == calendarObj?.date) {
            setupEvent(ev1)
        }

    }

    override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
        var reserved = 0
        dataSnapshot.child("reservations").children.forEach {
            reserved += it.child("clients").childrenCount.toInt()
        }


        val calendarObj = dataSnapshot.getValue(Calendar::class.java).also { it?.date = dataSnapshot.key.toString() }

        val ev1 = Event(
            ContextCompat.getColor(this@ReservationActivity, R.color.orange),
            formatter.parse(calendarObj?.date).time,
            calendarObj
        )
        calendar.addEvent(ev1)

        if (formatter.format(currentDate) == calendarObj?.date) {
            setupEvent(ev1)
        }

    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val date = GregorianCalendar(year, month, dayOfMonth).time
        contentView.birthdate.setText(formatter.format(date))
        contentView.citizenship.requestFocus()
    }

    override fun onChildRemoved(dataSnapshot: DataSnapshot) = Unit


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                val contents = data?.getStringExtra("SCAN_RESULT")
                Log.e("tales5", "pascuales: $contents")
            }
        }
    }
}
