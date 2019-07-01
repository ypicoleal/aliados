package com.example.yamidpico.aliadosapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.crashlytics.android.Crashlytics
import com.example.yamidpico.aliadosapp.adapter.ReservationAdapter
import com.example.yamidpico.aliadosapp.base.BaseAdapter
import com.example.yamidpico.aliadosapp.model.Reservation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), ValueEventListener, BaseAdapter.OnItemClickListener {

    private val calendarReference = FirebaseDatabase.getInstance().getReference("calendar")
    private val serviceReference = FirebaseDatabase.getInstance().getReference("services")
    private var uuid = ""
    private val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    private var mAuth: FirebaseAuth? = null

    private val adapter = ReservationAdapter(this)
    private var servicesLoaded = false
    private var serviceList = listOf<String>()

    private val serviceDataListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            servicesLoaded = true
            serviceList = snapshot.children.map { it.value.toString() }
        }

        override fun onCancelled(p0: DatabaseError) = Unit
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Fabric.with(this, Crashlytics())

        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

        add.setOnClickListener {
            if (serviceList.size > 1) {
                createDialog()
            } else if (serviceList.isNotEmpty()) {
                startActivity(Intent(this, ReservationActivity::class.java).apply {
                    putExtra("service", serviceList[0])
                })
            }
        }

        reservationList.adapter = adapter
    }

    private fun createDialog() {
        AlertDialog.Builder(this)
            .setTitle("Servicio")
            .setItems(serviceList.toTypedArray()) { _, which ->
                startActivity(Intent(this, ReservationActivity::class.java).apply {
                    putExtra("service", serviceList[which])
                })
            }
            .create()
            .show()
    }

    override fun onStart() {
        super.onStart()

        mAuth?.currentUser?.uid?.let { uuid = it }
        calendarReference.addValueEventListener(this)
        serviceReference.addValueEventListener(serviceDataListener)
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        val reservationList = snapshot.children.map { service ->
            service.children.map { calendar ->
                calendar.child("reservations").children.filter { it.key == uuid }.map { client ->
                    client.children.map { reservation ->
                        reservation.getValue(Reservation::class.java)?.also {
                            it.service = service.key ?: ""
                            it.date = formatter.parse(calendar.key)
                            it.minPrice = calendar.child("minPrice").value.toString().toInt()
                            it.minPriceChild = calendar.child("minPriceChild").value.toString().toInt()
                            it.transportPrice = calendar.child("transportPrice").value.toString().toInt()
                        } ?: Reservation()
                    }
                }.flatten()
            }.flatten()
        }.flatten()

        adapter.updateItems(reservationList)
    }

    override fun onCancelled(error: DatabaseError) = Unit

    override fun onClick(position: Int) {
        startActivity(
            Intent(this, DetailActivity::class.java)
                .putExtra("reservation", Gson().toJson(adapter.reservations[position]))
        )
    }
}
