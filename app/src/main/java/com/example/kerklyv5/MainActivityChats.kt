package com.example.kerklyv5

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.NetworkImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.request.RequestOptions
import com.example.kerklyv5.controlador.AdapterChat
import com.example.kerklyv5.modelo.Mensaje
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
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
    lateinit var telefonoKerkly: String
    lateinit var nombreKerkly: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_chats)

        boton = findViewById(R.id.boton_chat)
        editText = findViewById(R.id.editTextChat)
        recyclerView = findViewById(R.id.recycler_chat)
        txt_nombreKerkly = findViewById(R.id.txt_nombre_kerkly_chats)
        imageViewPerfil = findViewById(R.id.image_usuario)
        b = intent.extras!!

         nombreKerkly = b.getString("nombreCompletoK").toString()
        val correoKerkly = b.getString("correoK")
         telefonoKerkly = b.getString("telefonok").toString()
        val telefonoCliente = b!!.getString("telefonoCliente")
        val url = b!!.getString("urlFotoKerkly")

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
            //adapter.addMensaje(Mensaje(editText.text.toString(), "00:00"))
            databaseReferenceCliente.push().setValue(Mensaje(editText.text.toString(), getTime()))
            databaseReferenceKerkly.push().setValue(Mensaje(editText.text.toString(), getTime()))
            editText.setText("")
        }

        databaseReferenceCliente.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val m = snapshot.getValue(Mensaje::class.java)
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

    companion object{
        const val chanel_id = "chanel_ID"
    }

    fun notificacionMensajeEntrantes(){
        var builder = NotificationCompat.Builder(this, chanel_id)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle(nombreKerkly)
    }


    fun pruebaDeCambios(){
        println("holaaa")
    }

}