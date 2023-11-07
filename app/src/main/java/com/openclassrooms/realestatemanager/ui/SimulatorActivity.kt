package com.openclassrooms.realestatemanager.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.databinding.ActivitySimulatorBinding
import com.openclassrooms.realestatemanager.utils.Utils
import kotlin.math.roundToInt

class SimulatorActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySimulatorBinding
    private var isDollar: Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimulatorBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.simulatorArrowBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        displayMonthlyPayment()
        switchBetweenEuroAndUsd()
    }

    private fun switchBetweenEuroAndUsd() {
        binding.simulatorEuroBtn.setOnClickListener {
            binding.simulatorDollarBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.grayedText))
            binding.simulatorEuroBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.thirdPurple))
            isDollar = false
        }
        binding.simulatorDollarBtn.setOnClickListener {
            binding.simulatorDollarBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.thirdPurple))
            binding.simulatorEuroBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.grayedText))
            isDollar = true
        }
    }

    private fun displayMonthlyPayment() {
        binding.simulatorSimulateBtn.setOnClickListener {
            binding.simulatorResultContainer.visibility = View.VISIBLE

            val amount = binding.simulatorAmountEditText.text.toString().toDouble()
            val contribution = binding.simulatorContributionEditText.text.toString().toDouble()
            val rate = binding.simulatorRateEditText.text.toString().toDouble()
            val duration = binding.simulatorDurationEditText.text.toString().toInt()
            val monthlyPayment = Utils.calculateMonthlyPayment(amount, contribution, rate, duration).roundToInt()
            val totalPayment = monthlyPayment * duration * 12
            val monthlyPaymentInEuro = Utils.convertDollarToEuro(monthlyPayment.toLong()).toString()
            val totalPaymentInEuro = Utils.convertDollarToEuro(totalPayment.toLong()).toString()

            if (isDollar) {
                (getString(R.string.simulator_result_first_part) + totalPayment + "$ " + getString(R.string.simulator_result_second_part) + " " + monthlyPayment + "$ " + getString(
                    R.string.simulator_result_third_part
                )).also {
                    binding.simulatorResultTextView.text = it
                }
            } else {
                (getString(R.string.simulator_result_first_part) + totalPaymentInEuro + "€ " + getString(R.string.simulator_result_second_part) + " " + monthlyPaymentInEuro + "€ " + getString(
                    R.string.simulator_result_third_part
                )).also {
                    binding.simulatorResultTextView.text = it
                }
            }
        }
    }
}