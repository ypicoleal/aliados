package com.example.yamidpico.aliadosapp.model

data class Calendar(val minPrice: Int = 0,
                    val minPriceChild: Int = 0,
                    val suggestPrice: Int = 0,
                    val suggestPriceChild: Int = 0,
                    val transportPrice: Int = 0,
                    val capacity: Long = 0,
                    val boats: List<String> = listOf(),
                    var date: String = "")
