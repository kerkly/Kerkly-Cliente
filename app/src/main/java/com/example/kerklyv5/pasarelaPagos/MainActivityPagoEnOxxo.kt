package com.example.kerklyv5.pasarelaPagos

import android.R.attr.port
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kerklyv5.R
import com.google.gson.JsonSyntaxException
import com.stripe.Stripe
import com.stripe.android.PaymentConfiguration
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.PaymentMethodCreateParams
import com.stripe.android.payments.paymentlauncher.PaymentLauncher
import com.stripe.android.payments.paymentlauncher.PaymentResult
import com.stripe.exception.SignatureVerificationException
import com.stripe.model.Event
import com.stripe.model.EventDataObjectDeserializer
import com.stripe.model.StripeObject
import com.stripe.net.Webhook


class MainActivityPagoEnOxxo : AppCompatActivity() {
    private lateinit var paymentIntentClientSecret: String
    lateinit var paymentMethodCreateParams: PaymentMethodCreateParams
    private lateinit var email:String
    private lateinit var name:String
    private lateinit var secretKey:String
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
        secretKey = intent.getStringExtra("secretKey").toString()
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
               // val oxxoVoucherCode = paymentResult.voucherCode
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


  /*  private fun Server(){
        Stripe.apiKey = secretKey

        // This is your Stripe CLI webhook secret for testing your endpoint locally.

        // This is your Stripe CLI webhook secret for testing your endpoint locally.
        val endpointSecret = "whsec_03edc7bb48c4ce645ff92651d4554e2a934532afa6fe4833cdec3aa9dfba29cf"
        port(4242)

        post("/webhook") { request, response ->
            val payload: String = request.body()
            val sigHeader: String = request.headers("Stripe-Signature")
            var event: Event? = null
            event = try {
                Webhook.constructEvent(
                    payload, sigHeader, endpointSecret
                )
            } catch (e: JsonSyntaxException) {
                // Invalid payload
                response.status(400)
                return@post ""
            } catch (e: SignatureVerificationException) {
                // Invalid signature
                response.status(400)
                return@post ""
            }

            // Deserialize the nested object inside the event
            val dataObjectDeserializer: EventDataObjectDeserializer =
                event.getDataObjectDeserializer()
            var stripeObject: StripeObject? = null
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    dataObjectDeserializer.getObject().isPresent
                } else {
                    TODO("VERSION.SDK_INT < N")
                }
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stripeObject = dataObjectDeserializer.getObject().get()
                }
            } else {
                // Deserialization failed, probably due to an API version mismatch.
                // Refer to the Javadoc documentation on `EventDataObjectDeserializer` for
                // instructions on how to handle this case, or return an error here.
            }
            when (event!!.getType()) {
                "payment_intent.succeeded" -> {}
                else -> System.out.println("Unhandled event type: " + event.getType())
            }
            response.status(200)
            ""
        }

    }
*/
}