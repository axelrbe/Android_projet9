package com.openclassrooms.realestatemanager.utils

import org.junit.Test
import org.junit.jupiter.api.Assertions
import kotlin.math.roundToInt

class UtilsTest {

    @Test
    @Throws(Exception::class)
    fun calculateMonthlyPayment() {
        val mensualite = Utils.calculateMonthlyPayment(200000.0, 30000.0, 3.0, 20).roundToInt()
        Assertions.assertEquals(943, mensualite)
    }
}