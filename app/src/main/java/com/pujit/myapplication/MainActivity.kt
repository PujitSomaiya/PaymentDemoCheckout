package com.pujit.myapplication

import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.volley.VolleyError
import com.checkout.android_sdk.PaymentForm.PaymentFormCallback
import com.checkout.android_sdk.Response.CardTokenisationFail
import com.checkout.android_sdk.Response.CardTokenisationResponse
import com.checkout.android_sdk.Utils.Environment
import com.pujit.myapplication.databinding.ActivityMainBinding



class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding:ActivityMainBinding
    private var mProgressDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding=DataBindingUtil.setContentView(this,R.layout.activity_main)
        mProgressDialog = ProgressDialog(this);
        mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog!!.setMessage("Loading...");
        mainBinding.checkoutCardForm
                .setFormListener(mFormListener)
                .setEnvironment(Environment.SANDBOX)
                .setKey("pk_test_54ba9df2-4c3e-4f82-a1e4-0cfbc951b023")
                .setDefaultBillingCountry( applicationContext.resources.configuration.locale);

    }

    private val mFormListener: PaymentFormCallback = object : PaymentFormCallback {
        override fun onFormSubmit() {
            mProgressDialog!!.show() // show loader
        }

        override fun onTokenGenerated(response: CardTokenisationResponse) {
            mProgressDialog!!.dismiss() // dismiss the loader
            mainBinding.checkoutCardForm.clearForm() // clear the form
            displayMessage("Token", response.token)
            Log.e("TOKEN", response.token )
        }

        override fun onError(response: CardTokenisationFail) {
            displayMessage("Token Error", response.errorType)
        }

        override fun onNetworkError(error: VolleyError) {
            displayMessage("Network Error", error.toString())
        }

        override fun onBackPressed() {
            displayMessage("Back", "The user decided to leave the payment page.")
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
        val alert: AlertDialog = builder.create()
        alert.show()
    }
}