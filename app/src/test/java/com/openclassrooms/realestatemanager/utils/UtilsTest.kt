package com.openclassrooms.realestatemanager.utils

import org.junit.Test
import org.junit.jupiter.api.Assertions
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class UtilsTest {
    @Test
    @Throws(Exception::class)
    fun calculateMonthlyPayment() {
        val monthlyPayment = Utils.calculateMonthlyPayment(200000.0, 30000.0, 3.0, 20).roundToInt()
        Assertions.assertEquals(943, monthlyPayment)
    }
    @Test
    fun testConvertDollarToEuro() {
        val dollars: Long = 100
        val expectedEuros: Int = (dollars * 0.94345).roundToInt()
        val actualEuros: Int = Utils.convertDollarToEuro(dollars)
        Assertions.assertEquals(expectedEuros, actualEuros)
    }
    @Test
    fun testConvertEuroToDollar() {
        val euros = 100
        val expectedDollars: Int = (euros * 1.32030).roundToInt()
        val actualDollars: Int = Utils.convertEuroToDollar(euros)
        Assertions.assertEquals(expectedDollars, actualDollars)
    }
    @Test
    fun testTodayDate() {
        val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val expectedDate: String = dateFormat.format(Date())
        val actualDate: String = Utils.todayDate
        Assertions.assertEquals(expectedDate, actualDate)
    }
}