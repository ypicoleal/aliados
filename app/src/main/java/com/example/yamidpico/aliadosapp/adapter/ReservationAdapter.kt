package com.example.yamidpico.aliadosapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.yamidpico.aliadosapp.R
import com.example.yamidpico.aliadosapp.base.BaseAdapter
import com.example.yamidpico.aliadosapp.model.Reservation
import kotlinx.android.synthetic.main.item_reservation.view.*
import java.text.SimpleDateFormat
import java.util.*

class ReservationAdapter(listener: BaseAdapter.OnItemClickListener): BaseAdapter(listener) {

    private val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    var reservations = listOf<Reservation>()

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): BaseViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_reservation, parent, false)
        return BaseViewHolder(view)
    }

    fun updateItems(reservations: List<Reservation>) {
        this.reservations = reservations
        notifyDataSetChanged()
    }

    override fun getItemCount() = reservations.size

    override fun onBindViewHolder(viewHolder: BaseViewHolder, position: Int) {
        val reservation = reservations[position]

        viewHolder.itemView.apply {
            title.text = reservation.service
            subtitle.text = formatter.format(reservation.date) + " " + reservation.clients.size + " personas"
        }
    }

}