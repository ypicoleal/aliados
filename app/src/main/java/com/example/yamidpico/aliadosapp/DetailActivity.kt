package com.example.yamidpico.aliadosapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.yamidpico.aliadosapp.model.Reservation
import com.google.gson.Gson

import kotlinx.android.synthetic.main.content_detail.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity() {

    private var reservation: Reservation? = null
    private val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
    private val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        reservation = intent.getStringExtra("reservation")?.let {
            Gson().fromJson(it, Reservation::class.java)
        }

        setUI()
    }

    private fun setUI() {
        reservation?.let {
            service.text = it.service
            description.text = generateDescription(it)
            finalPrice.text = "$${formatter.format(it.finalPrice)}(Adulto) $${formatter.format(it.finalPriceChild)}(Niños)"
            subtotal.text = subtotal(it)
            totalTransport.text = "$" + if (!it.transportIncluded) "0" else formatter.format(it.transportPrice * it.clients.size)
            total.text = calculateTotal(it)
            abono.text = "$" + formatter.format(it.paidAmount)
            porCobrar.text = porCobrar(it)
            porPagar.text = porPagar(it)
            porPagarAliado.text = porPagarAliado(it)
        }
    }

    private fun subtotal(reservation: Reservation): String {
        val total = reservation.clients.sumBy {
            it ?: return@sumBy 0
            when (it.birthDate) {
                "adulto" -> reservation.finalPrice
                "menor" -> reservation.finalPriceChild
                else -> 0
            }
        }
        return "$" + formatter.format(total)
    }

    private fun porPagarAliado(reservation: Reservation): String {
        val total = -reservation.clients.sumBy {
            it ?: return@sumBy 0
            when (it.birthDate) {
                "adulto" -> (reservation.finalPrice - reservation.minPrice)
                "menor" -> (reservation.finalPriceChild - reservation.minPriceChild)
                else -> 0
            }
        } + reservation.paidAmount
        return "$" + if (total < 0) "0" else formatter.format(total)
    }

    private fun porPagar(reservation: Reservation): String {
        val total = reservation.clients.sumBy {
            it ?: return@sumBy 0
            when (it.birthDate) {
                "adulto" -> (reservation.finalPrice - reservation.minPrice)
                "menor" -> (reservation.finalPriceChild - reservation.minPriceChild)
                else -> 0
            }
        } - reservation.paidAmount
        return "$" + if (total < 0) "0" else formatter.format(total)
    }

    private fun porCobrar(reservation: Reservation): String {
        val total = reservation.clients.sumBy {
            it ?: return@sumBy 0
            when (it.birthDate) {
                "adulto" -> reservation.finalPrice
                "menor" -> reservation.finalPriceChild
                else -> 0
            }
        } + if (!reservation.transportIncluded) {
            0
        } else {
            reservation.transportPrice * reservation.clients.size
        }
        return "$" + formatter.format(total - reservation.paidAmount)
    }

    private fun calculateTotal(reservation: Reservation): String {
        val total = reservation.clients.sumBy {
            it ?: return@sumBy 0
            when (it.birthDate) {
                "adulto" -> reservation.finalPrice
                "menor" -> reservation.finalPriceChild
                else -> 0
            }
        } + if (!reservation.transportIncluded) {
            0
        } else {
            reservation.transportPrice * reservation.clients.size
        }
        return "$" + formatter.format(total)
    }

    private fun generateDescription(reservation: Reservation): String {
        var text = ""
        reservation.clients.forEach {
            text += "- ${it?.fullName} - ${it?.hotel}\n"
        }
        return text
    }
}
