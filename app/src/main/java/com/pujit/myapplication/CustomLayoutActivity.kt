package com.pujit.myapplication

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.volley.VolleyError
import com.checkout.android_sdk.CheckoutAPIClient
import com.checkout.android_sdk.CheckoutAPIClient.OnTokenGenerated
import com.checkout.android_sdk.Request.CardTokenisationRequest
import com.checkout.android_sdk.Response.CardTokenisationFail
import com.checkout.android_sdk.Response.CardTokenisationResponse
import com.checkout.android_sdk.Utils.Environment
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.pujit.myapplication.databinding.ActivityCustomeLayoutBinding
import com.pujit.myapplication.request.PaymentRequest
import com.pujit.myapplication.request.Source
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.util.*


class CustomLayoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomeLayoutBinding
    private val myCalendar: Calendar = Calendar.getInstance()
    private lateinit var mCheckoutAPIClient: CheckoutAPIClient
    private var mProgressDialog: ProgressDialog? = null
    private lateinit var  alert: AlertDialog

    var url = "https://api.sandbox.checkout.com/payments"
    val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_custome_layout)

        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        mProgressDialog!!.setMessage("Loading...")

        binding.edCardNumber.afterTextChangedCardNumberChange()
        binding.edExpiryDate.afterTextChangedExpireDate()
        binding.edExpiryYear.afterTextChangedExpireYear()
        binding.edCVV.afterTextChangedCVV()
        binding.edExpiryDate.setOnClickListener { datePicker() }
        binding.tilExpireDate.setOnClickListener { datePicker() }
        binding.edExpiryYear.setOnClickListener { datePicker() }
        binding.tilExpiryYear.setOnClickListener { datePicker() }

        binding.btnPay.setOnClickListener { callProcess() }

        mCheckoutAPIClient = CheckoutAPIClient(
            this,
            "pk_test_54ba9df2-4c3e-4f82-a1e4-0cfbc951b023",
            Environment.SANDBOX
        )
        mCheckoutAPIClient.setTokenListener(mTokenListener)
    }

    private fun callPaymentApi(token: String) {
        alert.dismiss()
        val postBody = PaymentRequest("INR", Source("token", token))
        println("Request: ${Gson().toJson(postBody)}")
        postRequest(url, Gson().toJson(postBody))
    }

    @Throws(IOException::class)
    fun postRequest(postUrl: String?, postBody: String?) {
        val client = OkHttpClient()
        val body: RequestBody = postBody.toString().toRequestBody()
        val request: Request = Request.Builder()
            .url(postUrl!!)
            .addHeader("Content-Type","application/json")
            .addHeader("Authorization","sk_test_6c5a9999-d05a-4af1-87d1-b778bf65e508")
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                Snackbar.make(binding.root,"Fail",Snackbar.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call, response: Response) {
                Snackbar.make(binding.root,response.code.toString()+" "+response.message,Snackbar.LENGTH_LONG).show()
            }

        })
    }

    private fun callProcess() {
        var cardNumber = binding.edCardNumber.text.toString()
        cardNumber = cardNumber.replace(" ", "")
        mProgressDialog!!.show()
        hideKeyboard(this, binding.root)
        mCheckoutAPIClient.generateToken(
            CardTokenisationRequest(
                cardNumber,
                "",
                binding.edExpiryDate.text.toString(),
                binding.edExpiryYear.text.toString(),
                binding.edCVV.text.toString()
            )
        )
    }

    private val mTokenListener: OnTokenGenerated = object : OnTokenGenerated {
        override fun onTokenGenerated(token: CardTokenisationResponse) {
            mProgressDialog!!.dismiss()
            Log.e("TOKEN", token.token )
            displayMessage("Success!", token.token)
            Handler().postDelayed({callPaymentApi(token.token)},500)
        }

        override fun onError(error: CardTokenisationFail) {
            mProgressDialog!!.dismiss()
            displayMessage("Error!", error.requestId)
        }

        override fun onNetworkError(error: VolleyError) {
            // your network error
        }
    }


    private fun displayMessage(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                //do things
            })
        alert = builder.create()
        alert.show()
    }

    private fun EditText.afterTextChangedCardNumberChange() {
        var current = ""

        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (binding.edExpiryYear.text?.length!! == 4) {
                    if (binding.edExpiryDate.text?.length!! == 2) {
                        if (binding.edCVV.text?.length!! >= 3) {
                            binding.btnPay.isEnabled = s?.length!! == 19
                        }
                    }
                }
            }

            override fun afterTextChanged(editable: Editable?) {

                if (editable.toString() != current) {
                    val userInput = editable.toString().replace(Regex("[^\\d]"), "")
                    if (userInput.length <= 16) {
                        current = userInput.chunked(4).joinToString(" ")
                        editable!!.filters = arrayOfNulls<InputFilter>(0)
                    }
                    editable!!.replace(0, editable.length, current, 0, current.length)
                }
            }
        })
    }


    private fun EditText.afterTextChangedExpireDate() {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.edCardNumber.text?.length!! == 19) {
                    if (binding.edExpiryYear.text?.length!! == 4) {
                        if (binding.edCVV.text?.length!! >= 3) {
                            binding.btnPay.isEnabled = s?.length!! == 2
                        }
                    }
                }

            }

            override fun afterTextChanged(editable: Editable?) {

            }
        })
    }

    private fun EditText.afterTextChangedExpireYear() {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.edCardNumber.text?.length!! == 19) {
                    if (binding.edExpiryDate.text?.length!! == 2) {
                        if (binding.edCVV.text?.length!! >= 3) {
                            binding.btnPay.isEnabled = s?.length!! == 4
                        }
                    }
                }

            }

            override fun afterTextChanged(editable: Editable?) {

            }
        })
    }

    private fun EditText.afterTextChangedCVV() {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.edCardNumber.text?.length!! == 19) {
                    if (binding.edExpiryDate.text?.length!! == 2) {
                        if (binding.edExpiryYear.text?.length!! == 4) {
                            binding.btnPay.isEnabled = s?.length!! >= 3
                        }
                    }
                }

            }

            override fun afterTextChanged(editable: Editable?) {

            }
        })
    }

    fun hideKeyboard(context: Context, view: View) {
        val imm =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun datePicker() {
        val date =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                myCalendar[Calendar.YEAR] = year
                myCalendar[Calendar.MONTH] = monthOfYear
                myCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                updateDate(year, monthOfYear + 1, dayOfMonth)
            }
        val datePickerDialog: DatePickerDialog
        datePickerDialog = DatePickerDialog(
            this, date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
            myCalendar[Calendar.DAY_OF_MONTH]
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun updateDate(year: Int, month: Int, date: Int) {
        var month: String = month.toString()
        if (month.length == 1) {
            month = "0$month"
        }
        binding.edExpiryDate.setText(month)
        binding.edExpiryYear.setText(year.toString())
    }


}