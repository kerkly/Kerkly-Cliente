package com.example.kerklyv5.express

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import com.example.kerklyv5.MainActivityAceptarServicio
import com.example.kerklyv5.R
import com.example.kerklyv5.modelo.Pdf
import com.example.kerklyv5.pasarelaPagos.CheckoutActivity
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MensajesExpress : AppCompatActivity() {
    private lateinit var txt_dest: TextView
    private lateinit var txt_remi: TextView
    private lateinit var txt_fecha: TextView
    private lateinit var dialog: Dialog
    private var total = 0.0
    private lateinit var boton: MaterialButton
    private var problema = ""
    private var cliente = ""
    private var telefono = ""

    private var folio = 0
    private lateinit var btnContinuar: Button
    private lateinit var imgP: ImageView
    private var header: ArrayList<String> = ArrayList<String>()
    private lateinit var databaseReferenceNR: DatabaseReference
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
    lateinit var txtPruebaSinR: TextView
    private lateinit var imageViewPDF: PDFView


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mensajes_express)

        firebaseDatabase = FirebaseDatabase.getInstance()
        txt_dest = findViewById(R.id.txt_destinatario)
        txt_remi = findViewById(R.id.txt_remitente)
        txt_fecha = findViewById(R.id.txt_fechaMensaje)
        mensaje_txt = findViewById(R.id.txt_cuerpoMensaje)
        txtPruebaSinR = findViewById(R.id.txtPruebaSinR)
        imageViewPDF = findViewById(R.id.pdfView)

        dialog = Dialog(this)

        b = intent.extras!!
        btnContinuar = findViewById(R.id.BtnSiguiente)
        tipoUsuario = b.getString("tipoServicio").toString()
        nombreCompletoKerkly = b.getString("nombreCompletoKerkly").toString()
        telefonoKerkly = b.getString("telefonoKerkly").toString()
        direccionKerkly = b.getString("direccionKerkly").toString()
        correoKerkly = b.getString("correoKerkly").toString()
        telefono = b.getString("Telefono").toString()
        problema = b.getString("Problema").toString()

        pagoTotal = b.getString("Pago total").toString()
        if (tipoUsuario =="NoRegistrado"){
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

            total = b.getDouble("Pago total")
            header.add("Item")
            header.add("Concepto")
            header.add("Pago")
            //println("foliooo ---> " +  "$calle $colonia $ext $cp $ref")
            databaseReferenceNR = firebaseDatabase.getReference("UsuariosR").child(telefonoKerkly).child("Presupuestos NR").child("Presupuesto NR $folio")
            databaseReferenceNR.addValueEventListener(postListener)
        }

        if (tipoUsuario == "Registrado"){
            txtPruebaSinR.isInvisible = true
            val nombre = b.get("NombreCliente").toString()
            txt_remi.text = "Para: $nombre"
            txt_fecha.text = b.get("Fecha").toString()
            txt_dest.text = "De: $nombreCompletoKerkly"
            folio = b.getInt("Folio")
            cliente = nombre
            total = b.getDouble("Pago total")
            header.add("Item")
            header.add("Concepto")
            header.add("Pago")
            databaseReference = firebaseDatabase.getReference("UsuariosR").child(telefonoKerkly).child("Presupuestos Normal").child("Presupuesto Normal 65")
            databaseReference.addValueEventListener(postListener)
        }
        btnContinuar.setOnClickListener {
            if (tipoUsuario =="NoRegistrado"){
               // finish()
               /* val intent  = Intent(applicationContext, FormaPagoExrpess::class.java)
                b.putBoolean("Express", true)
                intent.putExtras(b)
                startActivity(intent)*/
                val intent  = Intent(applicationContext, CheckoutActivity::class.java)
                b.putBoolean("Express", true)
                intent.putExtras(b)
                startActivity(intent)
            }
            if (tipoUsuario == "Registrado"){
               // finish()
                val intent = Intent(this, MainActivityAceptarServicio::class.java)
                //intent.putExtra("Ap_Kerkly", ap_kerkly)
                intent.putExtra("Nombre_completo_Kerkly", nombreCompletoKerkly)
                intent.putExtra("IdContrato", folio)
                intent.putExtra("telefonoCliente", telefono)
                intent.putExtra("nombreCompletoCliente",cliente)
                intent.putExtra("telefonokerkly", telefonoKerkly)
                intent.putExtra("tipoServicio", tipoUsuario)
                intent.putExtra("problema", problema)
                startActivity(intent)
            }
        }
    }
    val postListener = object:ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            System.out.println("presupuesto " +snapshot.value)
          // var coleccion = snapshot.child("UsuariosR").child(telefonoKerkly).child("Presupuestos Normal").child("Presupuesto Normal $folio").value as MutableList<*>
           if (snapshot.value == null){
               Toast.makeText(this@MensajesExpress,"Lo sentimos hubo un problema $telefonoKerkly",Toast.LENGTH_SHORT).show()
           }else{
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
           // Log.d("coleccion", lista.toString())
            pdf()
        }
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
      val p= Pdf(cliente, direccionKerkly, folio, correoKerkly, tipoUsuario, nombreCompletoKerkly, imageViewPDF)
        p.telefono = telefonoKerkly
        p.cabecera = header
        p.correo = correoKerkly
        p.problema = problema
        p.direccion = direccionKerkly
        p.folio = folio
        p.total = total
        p.lista = lista
        p.generarPdf()
        Toast.makeText(this, "Descargado", Toast.LENGTH_SHORT).show()
        dialog.dismiss()

    }

    override fun onBackPressed() {
        if (imageViewPDF.visibility == View.VISIBLE){
            imageViewPDF.visibility = View.GONE
        }else{
            super.onBackPressed()
        }

    }


}