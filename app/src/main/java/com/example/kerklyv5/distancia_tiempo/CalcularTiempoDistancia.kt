package com.example.kerklyv5.distancia_tiempo

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class CalcularTiempoDistancia(val mContext: Context) : AsyncTask<String?, Void?, String?>() {
    private var progressDialog: ProgressDialog? = null
    private val geo1: Geo

    override fun onPreExecute() {
        super.onPreExecute()
        progressDialog = ProgressDialog(mContext)
        progressDialog?.setMessage("Loading")
        progressDialog?.setCancelable(false)
        progressDialog?.show()
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        progressDialog?.dismiss()

        if (result != null) {
            geo1.setDouble(result)
        } else {
            Toast.makeText(mContext, "Error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun doInBackground(vararg params: String?): String? {
        try {
            val url = URL(params[0])
            val con = url.openConnection() as HttpURLConnection

            con.requestMethod = "GET"
            con.connect()

            val statusCode = con.responseCode

            if (statusCode == HttpURLConnection.HTTP_OK) {
                val br = BufferedReader(InputStreamReader(con.inputStream))
                val sb = StringBuilder()
                var line: String? = br.readLine()

                while (line != null) {
                    sb.append(line)
                    line = br.readLine()
                }

                val json = sb.toString()
                val root = JSONObject(json)
                val arrayRows = root.getJSONArray("rows")
                val objectRows = arrayRows.getJSONObject(0)
                val arrayElements = objectRows.getJSONArray("elements")
                val objectElements = arrayElements.getJSONObject(0)
                val objectDuration = objectElements.getJSONObject("duration")
                val objectDistance = objectElements.getJSONObject("distance")

                return objectDuration.getString("value") + "," + objectDistance.getString("value")
            }
        } catch (e: Exception) {
            Log.e("Error", "$e")
        }

        return null
    }

    interface Geo {
        fun setDouble(min: String?)
    }

    init {
        geo1 = mContext as? Geo ?: throw ClassCastException("Context must implement Geo interface")
    }
}
