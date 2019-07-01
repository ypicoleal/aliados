package com.example.yamidpico.aliadosapp.model

import java.util.Date

data class Reservation(
    var service: String = "",
    var date: Date = Date(),
    val finalPrice: Int = 0,
    val finalPriceChild: Int = 0,
    val paidAmount: Int = 0,
    val transportIncluded: Boolean = false,
    val clients: List<Client?> = listOf(),
    var minPrice: Int = 0,
    var minPriceChild: Int = 0,
    var transportPrice: Int = 0) {

    data class Product(
        val product: String = "",
        val quantity: Int = 0,
        val price: Int = 0)
}