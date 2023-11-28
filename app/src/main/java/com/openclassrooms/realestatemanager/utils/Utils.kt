package com.openclassrooms.realestatemanager.utils

import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Created by Philippe on 21/02/2018.
 */
object Utils {
    /**
     * Conversion d'un prix d'un bien immobilier (Dollars vers Euros)
     * NOTE : NE PAS SUPPRIMER, A MONTRER DURANT LA SOUTENANCE
     * @param dollars
     * @return
     */
    fun convertDollarToEuro(dollars: Long): Int {
        return (dollars * 0.85 ).roundToInt()
    }

    fun convertEuroToDollar(euros: Int): Int {
        return (euros * 1.18).roundToInt()
    }

    val todayDate: String
        /**
         * Conversion de la date d'aujourd'hui en un format plus approprié
         * NOTE : NE PAS SUPPRIMER, A MONTRER DURANT LA SOUTENANCE
         * @return
         */
        get() {
            val dateFormat: DateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return dateFormat.format(Date())
        }

    /**
     * Vérification de la connexion réseau
     * NOTE : NE PAS SUPPRIMER, A MONTRER DURANT LA SOUTENANCE
     * @return
     */
    fun isInternetConnected(): Boolean {
        return try {
            val sock = Socket()
            sock.connect(InetSocketAddress("8.8.8.8", 53), 1500)
            sock.close()
            true
        } catch (e: IOException) {
            false
        }
    }

    fun calculateMonthlyPayment(
        purchasePrice: Double,
        downPayment: Double,
        interestRate: Double,
        loanDuration: Int
    ): Double {
        val loanAmount = purchasePrice - downPayment
        val monthlyInterestRate = interestRate / 12 / 100
        val numberOfPayments = loanDuration * 12
        val mathPower = (1 + monthlyInterestRate).pow(numberOfPayments.toDouble())
        return loanAmount * (monthlyInterestRate * mathPower / (mathPower - 1))
    }
}