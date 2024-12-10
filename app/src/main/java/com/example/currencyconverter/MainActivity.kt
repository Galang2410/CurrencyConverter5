package com.example.currencyconverter

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {
    private var baseCurrency = "EUR"
    private var convertedCurrency = "USD"
    private var currencyRates = mutableMapOf<String, Double>()

    private lateinit var amountInput: EditText
    private lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        amountInput = findViewById(R.id.amountInput)
        resultText = findViewById(R.id.resultText)
        val convertButton: Button = findViewById(R.id.convertButton)
        val swapButton: Button = findViewById(R.id.swapButton)

        fetchExchangeRates() // Ambil data dari API

        // Tombol untuk memicu konversi
        convertButton.setOnClickListener {
            if (amountInput.text.isNotEmpty()) {
                convertCurrency() // Lakukan konversi jika input valid
            } else {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol untuk menukar currency
        swapButton.setOnClickListener {
            swapCurrencies()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun convertCurrency() {
        if (baseCurrency == convertedCurrency) {
            resultText.text = "${amountInput.text} $baseCurrency"
            return
        }

        try {
            val input = amountInput.text.toString().toDouble()
            val baseRate = currencyRates[baseCurrency] ?: return
            val targetRate = currencyRates[convertedCurrency] ?: return

            val result = (targetRate / baseRate) * input
            resultText.text = "%.2f $convertedCurrency".format(result)
        } catch (e: Exception) {
            Log.e("Conversion", "Error converting: ${e.message}")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun fetchExchangeRates() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val API = "https://api.currencyfreaks.com/latest?apikey=a5ddf94800f54361a6d3ad1210b4658c"
                val response = URL(API).readText()
                val jsonObject = JSONObject(response)
                val rates = jsonObject.getJSONObject("rates")

                // Update nilai tukar ke dalam map
                currencyRates.clear()
                rates.keys().forEach { currency ->
                    currencyRates[currency] = rates.getDouble(currency)
                }

                val currencies = rates.keys().asSequence().toList()

                withContext(Dispatchers.Main) {
                    updateCurrencySpinners(currencies) // Perbarui spinner
                }
            } catch (e: Exception) {
                Log.e("API", "Error fetching rates: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "Error fetching exchange rates: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateCurrencySpinners(currencies: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val fromCurrencySpinner: Spinner = findViewById(R.id.fromCurrencySpinner)
        val toCurrencySpinner: Spinner = findViewById(R.id.toCurrencySpinner)

        fromCurrencySpinner.adapter = adapter
        toCurrencySpinner.adapter = adapter

        fromCurrencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                baseCurrency = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        toCurrencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                convertedCurrency = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun swapCurrencies() {
        val temp = baseCurrency
        baseCurrency = convertedCurrency
        convertedCurrency = temp

        val fromSpinner: Spinner = findViewById(R.id.fromCurrencySpinner)
        val toSpinner: Spinner = findViewById(R.id.toCurrencySpinner)

        // Update posisi spinner
        fromSpinner.setSelection((fromSpinner.adapter as ArrayAdapter<String>).getPosition(baseCurrency))
        toSpinner.setSelection((toSpinner.adapter as ArrayAdapter<String>).getPosition(convertedCurrency))
    }
}
