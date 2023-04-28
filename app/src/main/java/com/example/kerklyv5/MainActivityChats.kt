package com.example.kerklyv5

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.request.RequestOptions
import com.example.kerklyv5.controlador.AdapterChat
import com.example.kerklyv5.modelo.Mensaje
import com.example.kerklyv5.notificaciones.llamarTopico
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import java.text.DateFormat
import java.util.*


class MainActivityChats : AppCompatActivity() {
    private lateinit var boton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var editText: EditText
    private lateinit var adapter: AdapterChat
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReferenceCliente: DatabaseReference
    private lateinit var databaseReferenceKerkly: DatabaseReference
    private lateinit var txt_nombreKerkly: TextView
    private lateinit var b: Bundle
    private lateinit var imageViewPerfil: ImageView
    private lateinit var imageButtonSeleccionarFoto: ImageButton
    lateinit var telefonoKerkly: String
    lateinit var nombreKerkly: String
    private final var PICK_IMAGE_REQUEST  = 600
   private lateinit var mensaje: String
   private lateinit var tituloMensaje: String
   private lateinit var tokenKerkly : String
   private lateinit var nombreCompletoCliente: String
   val llamartopico = llamarTopico()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_chats)

        boton = findViewById(R.id.boton_chat)
        editText = findViewById(R.id.editTextChat)
        recyclerView = findViewById(R.id.recycler_chat)
        txt_nombreKerkly = findViewById(R.id.txt_nombre_kerkly_chats)
        imageViewPerfil = findViewById(R.id.image_usuario)
        imageButtonSeleccionarFoto = findViewById<ImageButton>(R.id.imageButtonSelecionarFoto)
        b = intent.extras!!

         nombreKerkly = b.getString("nombreCompletoK").toString()
        nombreCompletoCliente = b.getString("nombreCompletoCliente")!!
        val correoKerkly = b.getString("correoK")
         telefonoKerkly = b.getString("telefonok").toString()
        val telefonoCliente = b!!.getString("telefonoCliente")
        val url = b!!.getString("urlFotoKerkly")
        tokenKerkly = b!!.getString("tokenKerkly").toString()

        val photoUrl = Uri.parse(url)
        Picasso.get().load(photoUrl).into(object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                System.out.println("Respuesta 1 ")
                val multi = MultiTransformation<Bitmap>(RoundedCornersTransformation(128, 0, RoundedCornersTransformation.CornerType.ALL))

                Glide.with(this@MainActivityChats).load(photoUrl)
                    .apply(RequestOptions.bitmapTransform(multi))
                    .into(imageViewPerfil)
            }
            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                TODO("Not yet implemented")
            }
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                val multi = MultiTransformation<Bitmap>(RoundedCornersTransformation(128, 0, RoundedCornersTransformation.CornerType.ALL))

                Glide.with(this@MainActivityChats).load(photoUrl)
                    .apply(RequestOptions.bitmapTransform(multi))
                    .into(imageViewPerfil)
            }

        })
        txt_nombreKerkly.text = nombreKerkly
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReferenceCliente = firebaseDatabase.getReference("UsuariosR").child(telefonoCliente.toString()).child("chats")
            .child("$telefonoCliente"+"_"+"$telefonoKerkly")
        adapter = AdapterChat(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                setScrollBar()
            }
        })
        databaseReferenceKerkly = firebaseDatabase.getReference("UsuariosR").child(telefonoKerkly.toString()).child("chats")
            .child("$telefonoKerkly"+"_"+"$telefonoCliente")

        boton.setOnClickListener {
          if (editText.text == null){
              Toast.makeText(this, "Escribe tu mensaje" , Toast.LENGTH_SHORT).show()
          }else{

           //adapter.addMensaje(Mensaje(editText.text.toString(), "00:00"))
           databaseReferenceCliente.push().setValue(Mensaje(editText.text.toString(), getTime()))
           databaseReferenceKerkly.push().setValue(Mensaje(editText.text.toString(), getTime()))
         llamartopico.llamartopico(this,tokenKerkly, editText.text.toString(), nombreCompletoCliente)
           editText.setText("")


          }

        }
        databaseReferenceCliente.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val m = snapshot.getValue(Mensaje::class.java)
               // notificacionMensajeEntrantes(m!!.mensaje)
                adapter.addMensaje(m!!)

            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                adapter.notifyDataSetChanged()
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        imageButtonSeleccionarFoto.setOnClickListener {
            SeleccionarFoto()

        }

    }
    private fun setScrollBar() {
        recyclerView.scrollToPosition(adapter.itemCount-1)
    }
    @SuppressLint("SimpleDateFormat")
    private fun getTime(): String {
       // val formatter = SimpleDateFormat("HH:mm")
        //val curDate = Date(System.currentTimeMillis())
        // Obtener la hora actual
        //val str: String = formatter.format(curDate)
        val currentDateTimeString = DateFormat.getDateTimeInstance().format(Date())
        return currentDateTimeString
    }
    fun SeleccionarFoto(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val selectedImageUri = data!!.data
            println("url foto " + selectedImageUri.toString())
            // Hacer algo con la URI de la imagen seleccionada, como cargarla en Firebase Storage
        }
    }

}