package com.example.kerklyv5.pasarelaPagos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.kerklyv5.R
import com.stripe.android.PaymentConfiguration
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodCreateParams
import com.stripe.android.payments.paymentlauncher.PaymentLauncher
import com.stripe.android.payments.paymentlauncher.PaymentResult

class MainActivityPagoEnOxxo : AppCompatActivity() {
    private lateinit var paymentIntentClientSecret: String
    lateinit var paymentMethodCreateParams: PaymentMethodCreateParams
    private lateinit var email:String
    private lateinit var name:String
    private val paymentLauncher: PaymentLauncher by lazy {
        val paymentConfiguration = PaymentConfiguration.getInstance(applicationContext)
        PaymentLauncher.Companion.create(
            this,
            paymentConfiguration.publishableKey,
            paymentConfiguration.stripeAccountId,
            ::onPaymentResult
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_pago_en_oxxo)


// Recuperar el ClientSecret del intent
        paymentIntentClientSecret = intent.getStringExtra("ClientSecret").toString()
        email = intent.getStringExtra("email").toString()
        name = intent.getStringExtra("name").toString()
        val billingDetails = PaymentMethod.BillingDetails(email = email, name = name)

        // Crea un PaymentMethodCreateParams para OXXO con los billingDetails
        paymentMethodCreateParams = PaymentMethodCreateParams.createOxxo(billingDetails)

        // Iniciar el proceso de pago
        startCheckout()

    }

    private fun startCheckout() {
        val confirmParams = ConfirmPaymentIntentParams
            .createWithPaymentMethodCreateParams(
                paymentMethodCreateParams = paymentMethodCreateParams,
                clientSecret = paymentIntentClientSecret
            )
        paymentLauncher.confirm(confirmParams)
    }
    private fun onPaymentResult(paymentResult: PaymentResult) {
        when (paymentResult) {
            is PaymentResult.Completed -> {
                // The OXXO voucher was displayed successfully.
                // The customer can now pay the OXXO voucher at the OXXO convenience store.
                showToast("The OXXO voucher was displayed successfully.")
            }
            is PaymentResult.Canceled -> {
                // handle cancel flow
                showToast("cancelado")
            }
            is PaymentResult.Failed -> {
                // handle failures
                // (for example, the customer may need to choose a new payment
                // method)
                showToast("fallo: ")
            }
        }
    }

    private fun showToast(mensaje:String){
        Toast.makeText(this,mensaje, Toast.LENGTH_LONG).show()
    }

}