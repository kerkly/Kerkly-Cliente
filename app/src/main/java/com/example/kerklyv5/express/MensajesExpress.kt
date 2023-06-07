package com.example.kerklyv5.express

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.kerklyv5.express.FormaPagoExrpess
import com.example.kerklyv5.R
import com.example.kerklyv5.interfaces.AceptarPresupuestoInterface
import com.example.kerklyv5.modelo.Pdf
import com.example.kerklyv5.url.Url
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import retrofit.Callback
import retrofit.RestAdapter
import retrofit.RetrofitError
import java.io.BufferedReader
import retrofit.client.Response
import java.io.InputStreamReader

class MensajesExpress : AppCompatActivity() {
    private lateinit var txt_dest: TextView
    private lateinit var txt_remi: TextView
    private lateinit var txt_fecha: TextView
    private lateinit var dialog: Dialog
    private var total = 0.0
    private lateinit var boton: MaterialButton
    private var problema = "Mi problema"
    private var cliente = ""
    private var direccion = "Mi direccion"
    private var telefono = ""

    private var folio = 0
    private lateinit var pdf_img: ImageView
    private lateinit var imgP: ImageView
    private var header: ArrayList<String> = ArrayList<String>()
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseDatabase: FirebaseDatabase
    var lista: MutableList<MutableList<String>>? = null
    private lateinit var b: Bundle
    private lateinit var pagoTotal: String
    private lateinit var mensaje_txt: TextView
    private lateinit var tipoUsuario: String
    private lateinit var telefonoKerkly: String
    private lateinit var nombreCompletoKerkly: String
    private lateinit var direccionKerkly: String
    private lateinit var correoKerkly: String



    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mensajes_express)

        firebaseDatabase = FirebaseDatabase.getInstance()
        txt_dest = findViewById(R.id.txt_destinatario)
        txt_remi = findViewById(R.id.txt_remitente)
        txt_fecha = findViewById(R.id.txt_fechaMensaje)
        mensaje_txt = findViewById(R.id.txt_cuerpoMensaje)

        dialog = Dialog(this)

        b = intent.extras!!
        pdf_img = findViewById(R.id.pdf_img)
        tipoUsuario = b.getString("tipoServicio").toString()
        if (tipoUsuario =="NoRegistrado"){
            nombreCompletoKerkly = b.getString("nombreCompletoKerkly").toString()
            telefonoKerkly = b.getString("telefonoKerkly").toString()
            pagoTotal = b.getString("Pago total").toString()
            val nombre = b.get("NombreClienteNR").toString()
            txt_remi.text = "Para: $nombre"
            txt_fecha.text = b.get("Fecha").toString()

            txt_dest.text = "De: $nombreCompletoKerkly"
            folio = b.getInt("Folio")
            cliente = nombre

            problema = b.getString("Problema").toString()

            var calle = b.getString("Calle")
            val num = b.getInt("Numero exterior")
            var colonia = b.getString("Colonia")
            var ref = b.getString("Referencia")
            var cp = b.getString("CP")

            var ext = ""

            if (num == 0) {
                ext = "S/N"
            } else {
                ext = num.toString()
            }
            if (calle == null){
                calle = "S/N"
            }

            if (colonia == null){
                colonia = "S/N"
            }
            if (cp ==null){
                cp = "S/N"
            }
            if (ref ==null){
                ref = "S/N"
            }
            direccionKerkly = b.getString("direccionKerly").toString()
            correoKerkly = b.getString("correoKerly").toString()
            direccion = "$calle $colonia $ext $cp $ref"
            telefono = b.getString("Telefono").toString()
            total = b.getDouble("Pago total")
            header.add("Item")
            header.add("Concepto")
            header.add("Pago")
            println("foliooo ---> " +  "$calle $colonia $ext $cp $ref")
            databaseReference = firebaseDatabase.getReference("UsuariosR").child(telefonoKerkly).child("Presupuestos NR").child("Presupuesto NR $folio")
            databaseReference.addValueEventListener(postListener)
        }
        pdf_img.setOnClickListener {
            val intent  = Intent(applicationContext, FormaPagoExrpess::class.java)
            b.putBoolean("Express", true)
            intent.putExtras(b)
            startActivity(intent)
        }

    }

    val postListener = object:ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            System.out.println(snapshot.value)
          // var coleccion = snapshot.child("UsuariosR").child(telefonoKerkly).child("Presupuestos Normal").child("Presupuesto Normal $folio").value as MutableList<*>
            var sn = snapshot.value as MutableList<*>
            val c = sn[1] as HashMap<*,*>
            var l1 = ArrayList<ArrayList<String>>()
            lista = l1.toMutableList()
            lista!!.clear()


            for(i in 1 until sn.size) {
                var dicc = sn[i] as HashMap<*,*>
                var l: ArrayList<String>? = ArrayList<String>()
                var l2 = l!!.toMutableList()
                l.add(i.toString())
                l.add(dicc.get("descripcion") as String)
                l.add(dicc.get("pago") as String)
                lista?.add(l)

            }
            Log.d("coleccion", lista.toString())
            pdf()
        }

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }

    }

    fun pdf() {
        dialog.setContentView(R.layout.presupuesto_listo)
        dialog.show()
    }

    fun aceptar(view: View) {
        generarPDF()

    }


    fun rechazar(view: View) {
        dialog.dismiss()
    }

    fun generarPDF() {
        imgP = dialog.findViewById(R.id.imageViePdf)
        val p = Pdf(cliente, direccion, folio, correoKerkly, tipoUsuario, nombreCompletoKerkly)
        p.telefono = telefono
        p.cabecera = header
       // p.correo = correo
        p.problema = problema
        p.direccion = direccion
        p.folio = folio
        p.total = total
        p.total = total
        p.lista = lista
        p.generarPdf()

        Toast.makeText(this, "Se descargo el archivo pdf", Toast.LENGTH_SHORT).show()
        dialog.dismiss()
    }

}