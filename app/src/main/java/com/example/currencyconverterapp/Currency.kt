package com.example.currencyconverterapp

data class Currency(
    val code: String,       // PLN, EUR, GBP, UAH
    val maxAmount: Double   // maximum amount that can be sent
)