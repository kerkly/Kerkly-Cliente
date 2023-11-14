package com.example.kerklyv5

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.request.RequestOptions
import com.example.kerklyv5.controlador.AdapterChat
import com.example.kerklyv5.modelo.Mensaje
import com.example.kerklyv5.modelo.MensajeCopia
import com.example.kerklyv5.notificaciones.llamarTopico
import com.example.kerklyv5.url.Instancias
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DateFormat
import java.util.*


class MainActivityChats : AppCompatActivity() {
    private lateinit var boton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var editText: EditText
    private lateinit var adapter: AdapterChat
    private lateinit var txt_nombreKerkly: TextView
    private lateinit var b: Bundle
    private lateinit var imageViewPerfil: ImageView
    private lateinit var imageButtonSeleccionarArchivo: ImageButton
    lateinit var telefonoKerkly: String
    lateinit var nombreKerkly: String
   private lateinit var tokenKerkly : String
    private lateinit var tokenCliente : String
   private lateinit var nombreCompletoCliente: String
   val llamartopico = llamarTopico()
    private lateinit var telefonoCliente: String
    private lateinit var childEventListener: ChildEventListener
    private lateinit var childEventListener2: ChildEventListener
    private val REQUEST_CODE_FILE = 1
    private lateinit var progressBar: ProgressBar
    private lateinit var uriArchivo: Uri
    private lateinit var imagenCompleta: ImageView
    private lateinit var pdfRenderer: PdfRenderer
     private var  REQUEST_CODE = 0
    private lateinit var PantallaCompletaPdf: PDFView
    private lateinit var instancias: Instancias
    private lateinit var databaseReferenceCliente: DatabaseReference
    private lateinit var databaseReferenceKerkly:DatabaseReference
    private lateinit var fotoCliente:String
    private lateinit var uidCliente:String
    private lateinit var uidKerkly:String
    private lateinit var Noti:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_chats)
        instancias = Instancias()
        boton = findViewById(R.id.boton_chat)
        editText = findViewById(R.id.editTextChat)
        recyclerView = findViewById(R.id.recycler_chat)
        txt_nombreKerkly = findViewById(R.id.txt_nombre_kerkly_chats)
        imageViewPerfil = findViewById(R.id.image_usuario)
        imageButtonSeleccionarArchivo = findViewById<ImageButton>(R.id.imageButtonSelecionarArchivo)
        imagenCompleta = findViewById(R.id.fullscreenImageView)
        PantallaCompletaPdf = findViewById(R.id.pdfView)
        b = intent.extras!!

         nombreKerkly = b.getString("nombreCompletoK").toString()
        nombreCompletoCliente = b.getString("nombreCompletoCliente")!!
       // val correoKerkly = b.getString("correoK")
         telefonoKerkly = b.getString("telefonok").toString()
         telefonoCliente = b!!.getString("telefonoCliente").toString()
         fotoCliente = b!!.getString("urlFotoKerkly").toString()
        tokenKerkly = b!!.getString("tokenKerkly").toString()
        tokenCliente = b!!.getString("tokenCliente").toString()
        uidCliente = b!!.getString("uidCliente").toString()
        uidKerkly = b!!.getString("uidKerkly").toString()
        Noti = b.getString("Noti").toString()
        println("uid Cliente $uidCliente uidKerkly $uidKerkly")
        println("aquii---> Nombre k $nombreKerkly Cliente $nombreCompletoCliente telek $telefonoKerkly")
        val photoUrl = Uri.parse(fotoCliente)
        Picasso.get().load(photoUrl).into(object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                System.out.println("Respuesta 1 ")
                val multi = MultiTransformation<Bitmap>(RoundedCornersTransformation(128, 0, RoundedCornersTransformation.CornerType.ALL))

                Glide.with(this@MainActivityChats).load(photoUrl)
                    .apply(RequestOptions.bitmapTransform(multi))
                    .into(imageViewPerfil)
            }
            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                println(e!!.message)
            }
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                val multi = MultiTransformation<Bitmap>(RoundedCornersTransformation(128, 0, RoundedCornersTransformation.CornerType.ALL))

                Glide.with(this@MainActivityChats).load(photoUrl)
                    .apply(RequestOptions.bitmapTransform(multi))
                    .into(imageViewPerfil)
            }

        })
        txt_nombreKerkly.text = nombreKerkly
        databaseReferenceCliente = instancias.referenciaChatscliente(uidCliente,uidKerkly)
        databaseReferenceKerkly = instancias.referenciaChatsKerkly(uidKerkly,uidCliente)
        //databaseReferenceCliente = firebaseDatabase.getReference("UsuariosR").child(telefonoCliente.toString()).child("chats").child("$telefonoCliente"+"_"+"$telefonoKerkly")
        adapter = AdapterChat(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                setScrollBar()
            }
        })
        //databaseReferenceKerkly = firebaseDatabase.getReference("UsuariosR").child(telefonoKerkly.toString()).child("chats").child("$telefonoKerkly"+"_"+"$telefonoCliente")

        boton.setOnClickListener {
          if (editText.text == null){
              Toast.makeText(this, "Escribe tu mensaje" , Toast.LENGTH_SHORT).show()
          }else{
           databaseReferenceCliente.push().setValue(Mensaje(editText.text.toString(), getTime(),"","",""))
           databaseReferenceKerkly.push().setValue(Mensaje(editText.text.toString(), getTime(),"","",""))
              llamartopico.chats(this,tokenKerkly, editText.text.toString(), nombreCompletoCliente,
                  nombreKerkly,telefonoKerkly,telefonoCliente,fotoCliente,tokenCliente, uidCliente,uidKerkly)
           editText.setText("")
          }
        }
     childEventListener =  databaseReferenceCliente.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val m = snapshot.getValue(Mensaje::class.java)
               // notificacionMensajeEntrantes(m!!.mensaje)
               // Toast.makeText(applicationContext, "mensaje nuevo" , Toast.LENGTH_SHORT).show()
                adapter.addMensaje(m!!)
                setScrollBar()
                if (m!!.tipo_usuario == "Kerkly"){
                    mensajeVistoCliente(snapshot.key!!)
                }
                val mGestureDetector = GestureDetector(applicationContext,
                    object : GestureDetector.SimpleOnGestureListener() {
                        override fun onSingleTapUp(e: MotionEvent): Boolean {
                            return true
                        }
                    })
                recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener{
                    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {

                        try {
                            val child = recyclerView.findChildViewUnder(e.x, e.y)
                            if (child != null && mGestureDetector.onTouchEvent(e)) {
                                val position = recyclerView.getChildAdapterPosition(child)
                               // Toast.makeText(applicationContext, "click en ${adapter.lista[position].mensaje}",Toast.LENGTH_SHORT).show()
                                if (adapter.lista[position].tipo_usuario == "cliente"){
                                    if (adapter.lista[position].tipoArchivo == "imagen"){
                                        imagenCompleta.visibility = View.VISIBLE
                                        val url = Uri.parse(adapter.lista[position].archivo)
                                        // println("url imagen " +adapter.lista[position].archivo)
                                        Glide.with(applicationContext)
                                            .load(url)
                                            .into(imagenCompleta)
                                    }

                                    //Toast.makeText(applicationContext, "es mensaje de kerkly ${adapter.lista[position].mensaje}",Toast.LENGTH_SHORT).show()
                                }
                                if (adapter.lista[position].tipo_usuario == "Kerkly"){
                                    val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    if (ContextCompat.checkSelfPermission(applicationContext, permission) != PackageManager.PERMISSION_GRANTED) {
                                        permisoParaAccerAlAlmacenamiento()
                                    }else{
                                    if (adapter.lista[position].tipoArchivo == "pdf"){
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                            if (!Environment.isExternalStorageManager()) {
                                                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                                startActivity(intent)
                                            }else{
                                                showOptionsDialogPDF(adapter.lista[position].mensaje)
                                            }

                                        }else{
                                            showOptionsDialogPDF(adapter.lista[position].mensaje)
                                        }
                                    }
                                    if (adapter.lista[position].tipoArchivo == "imagen"){
                                        showOptionsDialog(adapter.lista[position].mensaje, adapter.lista[position].archivo)
                                    }
                                    }
                                }
                                return true
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return false
                    }

                    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

                    }

                    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

                    }

                })

            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val m = snapshot.getValue(Mensaje::class.java)
                adapter.addMensajeClear()
                adapter.addMensaje(m!!)
                adapter.notifyDataSetChanged()
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                adapter.notifyDataSetChanged()
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }
            override fun onCancelled(error: DatabaseError) {

            }

        })

       childEventListener2 = databaseReferenceKerkly.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val m = snapshot.getValue(MensajeCopia::class.java)
                    // notificacionMensajeEntrantes(m!!.mensaje)
                    if (m!!.tipo_usuario == "Kerkly"){
                        mensajeVistoKerkly(snapshot.key!!)
                    }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                adapter.notifyDataSetChanged()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        progressBar= findViewById(R.id.progressBar)
        imageButtonSeleccionarArchivo.setOnClickListener {
            SeleccionarArchivo()

        }


        permisoParaAccerAlAlmacenamiento()


    }


    private fun permisoParaAccerAlAlmacenamiento() {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, realizar las acciones necesarias aquí
               // Toast.makeText(this,"Permiso concedido",Toast.LENGTH_SHORT).show()
              // println("permiso $REQUEST_CODE")
            } else {
                // Permiso denegado, manejar la situación de permiso denegado
              //  Toast.makeText(this,"Permiso denegado",Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showOptionsDialogPDF(Nombrearchivo: String) {
        val options = arrayOf("Ver PDF", "Descargar PDF")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Opciones")
        builder.setItems(options) { dialog: DialogInterface, which: Int ->
            when (which) {
                0 -> {
                            PantallaCompletaPdf.visibility = View.VISIBLE
                            progressBar.visibility = View.VISIBLE
                            //descargar pdf
                           val pdfRef = instancias.StorageReference(uidKerkly,uidCliente,Nombrearchivo)
                            val localFile = File.createTempFile("$Nombrearchivo", "pdf")
                            val ruta = getRuta(Nombrearchivo)

                            val uploadTask = pdfRef.getFile(ruta!!)
                            // Registra un Listener para obtener la URL del archivo una vez cargado
                            uploadTask.addOnProgressListener {taskSnapshot ->
                                // Calcula el progreso en porcentaje
                                val progress = 100.0 * taskSnapshot!!.bytesTransferred / taskSnapshot!!.totalByteCount
                                // Actualiza la barra de progreso
                                progressBar.progress = progress.toInt()
                            }
                            pdfRef.getFile(localFile)
                                .addOnSuccessListener {
                                    // El archivo PDF se descargó exitosamente, puedes realizar las operaciones necesarias aquí
                                    // localFile contiene la ubicación del archivo descargado en el dispositivo
                                    val destinationFile = File(ruta!!.toURI())
                                    localFile.copyTo(destinationFile, overwrite = true)
                                    Toast.makeText(applicationContext, "Archivo Descargado",Toast.LENGTH_SHORT).show()
                                    progressBar.visibility = View.GONE
                                    PantallaCompletaPdf.fromFile(File(destinationFile.toURI()))
                                        .defaultPage(0)
                                        .enableSwipe(true)
                                        .swipeHorizontal(false)
                                        .load()

                                    /*GlobalScope.launch(Dispatchers.IO) {
                                        val parcelFileDescriptor: ParcelFileDescriptor =
                                            ParcelFileDescriptor.open(destinationFile, ParcelFileDescriptor.MODE_READ_ONLY)
                                        pdfRenderer = PdfRenderer(parcelFileDescriptor)

                                        showPdfPage(0) // Muestra la primera página del PDF

                                    }*/
                                    // showPdf(destinationFile)
                                }
                                .addOnFailureListener { exception ->
                                    // Ocurrió un error al descargar el archivo PDF
                                    // Maneja el error de acuerdo a tus necesidades
                                    Toast.makeText(applicationContext, "Ocurrió un error al descargar el archivo PDF",Toast.LENGTH_SHORT).show()
                                    progressBar.visibility = View.GONE
                                }
                    }


                1 -> {
                    // Acción para "Descargar PDF"
                    //imagenCompleta.visibility = View.VISIBLE
                    progressBar.visibility = View.VISIBLE
                    //descargar pdf
                  val pdfRef = instancias.StorageReference(uidKerkly,uidCliente,Nombrearchivo)
                    val localFile = File.createTempFile("$Nombrearchivo", "pdf")
                    val ruta = getRuta(Nombrearchivo)
                    println("ruta $ruta")
                    val uploadTask = pdfRef.getFile(ruta!!)
                    // Registra un Listener para obtener la URL del archivo una vez cargado
                    uploadTask.addOnProgressListener {taskSnapshot ->
                        // Calcula el progreso en porcentaje
                        val progress = 100.0 * taskSnapshot!!.bytesTransferred / taskSnapshot!!.totalByteCount
                        // Actualiza la barra de progreso
                        progressBar.progress = progress.toInt()
                    }
                    pdfRef.getFile(localFile)
                        .addOnSuccessListener {
                            // El archivo PDF se descargó exitosamente, puedes realizar las operaciones necesarias aquí
                            // localFile contiene la ubicación del archivo descargado en el dispositivo
                            val destinationFile = File(ruta!!.toURI())
                            localFile.copyTo(destinationFile, overwrite = true)
                            Toast.makeText(applicationContext, "Archivo Descargado",Toast.LENGTH_SHORT).show()
                            progressBar.visibility = View.GONE
                        }
                        .addOnFailureListener { exception ->
                            // Ocurrió un error al descargar el archivo PDF
                            // Maneja el error de acuerdo a tus necesidades
                            Toast.makeText(applicationContext, "Ocurrió un error al descargar el archivo PDF",Toast.LENGTH_SHORT).show()
                            progressBar.visibility = View.GONE
                        }

                }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog: DialogInterface, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()

    }

    private fun showOptionsDialog(archivo: String, urlImagen: String) {
        val options = arrayOf("Ver imagen", "Descargar imagen")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Opciones")
        builder.setItems(options) { dialog: DialogInterface, which: Int ->
            when (which) {
                0 -> {
                    // Acción para "Ver imagen"
                    imagenCompleta.visibility = View.VISIBLE
                    val url = Uri.parse(urlImagen)
                    //  println("url imagen " +adapter.lista[position].archivo)
                    Glide.with(applicationContext)
                        .load(url)
                        .into(imagenCompleta)

                }
                1 -> {
                    // Acción para "Descargar imagen"
                    progressBar.visibility = View.VISIBLE
                    //descargar la imagen
                    //val storage = FirebaseStorage.getInstance()
                   // val storageRef = storage.reference
                    // Reemplaza "nombre_del_archivo.jpg" con el nombre del archivo de imagen que deseas descargar
                   // val imageRef = storageRef.child("UsuariosR").child(telefonoKerkly).child("chats").child("$telefonoKerkly"+"_"+"$telefonoCliente").child(archivo)
                   val imageRef = instancias.StorageReference(uidKerkly,uidCliente,archivo)
                    // crea un archivo temporal con un nombre único en el sistema de archivos local, utilizando el valor de la variable archivo como prefijo y la extensión ".jpg".
                    val localFile = File.createTempFile("$archivo", "jpg")
                    val ruta = getRuta(archivo)
                    val uploadTask = instancias.storageRef.getFile(ruta!!)
                    // Registra un Listener para obtener la URL del archivo una vez cargado
                    uploadTask.addOnProgressListener {taskSnapshot ->
                        // Calcula el progreso en porcentaje
                        val progress = 100.0 * taskSnapshot!!.bytesTransferred / taskSnapshot!!.totalByteCount
                        // Actualiza la barra de progreso
                        progressBar.progress = progress.toInt()
                    }
                    imageRef.getFile(localFile)
                        .addOnSuccessListener {
                            // La imagen se descargó exitosamente
                            // Puedes guardar la imagen en una ubicación específica utilizando el siguiente código

                            val destinationFile = File(ruta!!.toURI())
                            localFile.copyTo(destinationFile, overwrite = true)
                            Toast.makeText(applicationContext, "Imagen Descargada",Toast.LENGTH_SHORT).show()
                            // La imagen se ha guardado en la ubicación especificada
                            progressBar.visibility = View.GONE

                        }
                        .addOnFailureListener { exception ->
                            // Ocurrió un error al descargar la imagen
                            // Maneja el error de acuerdo a tus necesidades
                            Toast.makeText(applicationContext, "Ocurrió un error al descargar la imagen",Toast.LENGTH_SHORT).show()
                            progressBar.visibility = View.GONE
                        }
                }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog: DialogInterface, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private suspend fun showPdfPage(pageIndex: Int) {
        withContext(Dispatchers.IO) {
            val page: PdfRenderer.Page = pdfRenderer.openPage(pageIndex)

            val bitmap: Bitmap = Bitmap.createBitmap(
                page.width,
                page.height,
                Bitmap.Config.ARGB_8888
            )

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            withContext(Dispatchers.Main) {
                imagenCompleta.setImageBitmap(bitmap)
            }

            page.close()
        }
    }

    fun getRuta(NOMBRE_DIRECTORIO: String): File? {
        // El fichero sera almacenado en un directorio dentro del directorio
        // Descargas
        var ruta: File? = null
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
           //  println("nombre archivo 325 " +NOMBRE_DIRECTORIO)
            ruta = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), NOMBRE_DIRECTORIO)
            if (ruta.exists()) {
                // El directorio de descargas existe
                println("El directorio de descargas existe")
            } else {
                // El directorio de descargas no existe
                println("El directorio de descargas no existe")
                ruta.mkdirs()
            }
            /*   if (ruta != null) {
                    if (!ruta.mkdirs()) {
                        if (!ruta.exists()) {
                            return null
                        }
                    }
                }*/
        }
        return ruta
    }

    private fun mensajeVistoKerkly(key: String) {
       // firebaseDatabase = FirebaseDatabase.getInstance()
       // databaseReferenceMensajekerkly = firebaseDatabase.getReference("UsuariosR").child(telefonoKerkly.toString()).child("chats") .child("$telefonoKerkly"+"_"+"$telefonoCliente").child(key)
        val databaseReferenceMensajekerkly = instancias.referenciaChatsKerkly(uidKerkly,uidCliente).child(key)
        val map = mapOf("mensajeLeido" to "Visto")
        databaseReferenceMensajekerkly.updateChildren(map)
    }

    private fun mensajeVistoCliente(key: String) {
       // firebaseDatabase = FirebaseDatabase.getInstance()
       // databaseReferenceMensajeCliente = firebaseDatabase.getReference("UsuariosR").child(telefonoCliente.toString()).child("chats") .child("$telefonoCliente"+"_"+"$telefonoKerkly").child(key)
        val databaseReferenceMensajeCliente = instancias.referenciaChatscliente(uidCliente,uidKerkly).child(key)
        val map = mapOf("mensajeLeido" to "Visto")
        databaseReferenceMensajeCliente.updateChildren(map)
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
    fun SeleccionarArchivo(){
        val options = arrayOf("Enviar Imagen", "Enviar PDF")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Opciones")
        builder.setItems(options) { dialog: DialogInterface, which: Int ->
            when (which) {
                0 -> {
                    progressBar.visibility = View.VISIBLE
                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "image/*"
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    startActivityForResult(intent, REQUEST_CODE_FILE)
                }
                1 -> {
                      progressBar.visibility = View.VISIBLE
                     val intent = Intent(Intent.ACTION_GET_CONTENT)
                     intent.type = "application/pdf"
                    // intent.type = "*/*" // Puedes especificar el tipo de archivo deseado, por ejemplo, "application/pdf" para archivos PDF
                     intent.addCategory(Intent.CATEGORY_OPENABLE)
                      startActivityForResult(intent, REQUEST_CODE_FILE)

                }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog: DialogInterface, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FILE && resultCode == RESULT_OK && data != null) {
            progressBar.visibility = View.VISIBLE
            uriArchivo = data.data!!
            uriArchivo?.let { uri ->
                val fileType: String? = contentResolver.getType(uri)
                fileType?.let {
                    // Aquí tienes el tipo de archivo seleccionado
                    // Puedes realizar acciones adicionales en función del tipo de archivo
                    System.out.println("uri $fileType")
                    if (fileType  == "application/pdf"){
                        println("seleciono un pdf")
                        // Realiza las operaciones necesarias con el archivo PDF seleccionado
                        EnviarArchivo(uriArchivo, "pdf")
                    }else{
                        EnviarArchivo(uriArchivo, "imagen")
                        println("otro archivo ${uriArchivo.toString()}")
                    }
                }

            }

        }else{
            println("no entro archivo")
        }

    }

    private fun EnviarArchivo(uriArchivo: Uri, tipoArchivo: String){
        val nombreArchivo: String = obtenerNombreArchivo(uriArchivo)
        // Obtén una referencia al storage de Firebase
        val storageRef = FirebaseStorage.getInstance().reference
        // Crea un nombre de archivo único para evitar conflictos
        val filename = nombreArchivo
      /*  val fileRef = storageRef.child("UsuariosR").child(telefonoCliente.toString()).child("chats")
            .child("$telefonoCliente"+"_"+"$telefonoKerkly").child(filename)*/
        val fileRef = instancias.EnviarArchivoStorageReference(uidCliente,uidKerkly,filename)

        if (tipoArchivo == "pdf"){
            println("nombre del archivo " + nombreArchivo)
            // Carga el archivo PDF en Firebase Storage
            val uploadTask = fileRef.putFile(uriArchivo)
            // Registra un Listener para obtener la URL del archivo una vez cargado
            uploadTask.addOnProgressListener {taskSnapshot ->
                // Calcula el progreso en porcentaje
                val progress = 100.0 * taskSnapshot!!.bytesTransferred / taskSnapshot!!.totalByteCount
                // Actualiza la barra de progreso
               progressBar.progress = progress.toInt()
            }
            uploadTask.addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
                // Obtiene la URL de descarga del archivo
                fileRef.downloadUrl
                    .addOnSuccessListener { uri: Uri ->
                        // Guarda la URL del archivo en Firebase Realtime Database
                        //val databaseRef = FirebaseDatabase.getInstance().reference
                        val fileUrl = uri.toString()
                        databaseReferenceKerkly.push().setValue(Mensaje(nombreArchivo, getTime(),"",fileUrl,tipoArchivo))
                        databaseReferenceCliente.push().setValue(Mensaje(nombreArchivo, getTime(),"",fileUrl,tipoArchivo))
                        storageRef.child("$tipoArchivo").child(fileUrl)
                        llamartopico.chats(this,tokenKerkly, nombreArchivo, nombreCompletoCliente,
                        nombreKerkly,telefonoKerkly,telefonoCliente,fotoCliente,tokenCliente, uidCliente,uidKerkly)
                        Toast.makeText(applicationContext, "archivo enviado", Toast.LENGTH_SHORT).show()
                        progressBar.visibility =View.GONE
                    }
            }
        }else{
            // Carga el archivo PDF en Firebase Storage
            val uploadTask = fileRef.putFile(uriArchivo)
            // Registra un Listener para obtener la URL del archivo una vez cargado
            uploadTask.addOnProgressListener {taskSnapshot ->
                // Calcula el progreso en porcentaje
                val progress = 100.0 * taskSnapshot!!.bytesTransferred / taskSnapshot!!.totalByteCount
                // Actualiza la barra de progreso
               progressBar.progress = progress.toInt()
            }
            uploadTask.addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
                // Obtiene la URL de descarga del archivo
                fileRef.downloadUrl
                    .addOnSuccessListener { uri: Uri ->
                        // Guarda la URL del archivo en Firebase Realtime Database
                        //val databaseRef = FirebaseDatabase.getInstance().reference
                        val fileUrl = uri.toString()
                        databaseReferenceKerkly.push().setValue(Mensaje(nombreArchivo, getTime(),"",fileUrl,tipoArchivo))
                        databaseReferenceCliente.push().setValue(Mensaje(nombreArchivo, getTime(),"",fileUrl,tipoArchivo))
                        storageRef.child("$tipoArchivo").child(fileUrl)
                        llamartopico.chats(this,tokenKerkly, nombreArchivo, nombreCompletoCliente,
                            nombreKerkly,telefonoKerkly,telefonoCliente,fotoCliente,tokenCliente, uidCliente,uidKerkly)
                        Toast.makeText(applicationContext, "archivo enviado", Toast.LENGTH_SHORT).show()
                        progressBar.visibility =View.GONE
                    }
            }
        }
    }

    @SuppressLint("Range")
    private fun obtenerNombreArchivo(uri: Uri): String {
        var nombre: String? = null
        if (uri.scheme.equals("content")) {
            contentResolver.query(uri, null, null, null, null).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    nombre = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (nombre == null) {
            nombre = uri.path
            val index = nombre!!.lastIndexOf("/")
            if (index != -1) {
                nombre = nombre!!.substring(index + 1)
            }
        }
        return nombre!!
    }

    override fun onBackPressed() {
        if (Noti == "Noti"){
            val intent = Intent(this, SolicitarServicio::class.java)
            intent.putExtra("Telefono", telefonoCliente)
            startActivity(intent)
            finish()
        }
        if ( imagenCompleta.visibility == View.VISIBLE){
            imagenCompleta.visibility = View.GONE
            // Liberar la referencia a la imagen en imageView
            imagenCompleta?.setImageDrawable(null)
            //imagenCompleta = null

            // Llamar al onBackPressed de la clase base
         //   super.onBackPressed()

    }else{
            if (childEventListener == null || childEventListener2 == null){
                //  Toast.makeText(applicationContext, "null el childEventListener", Toast.LENGTH_SHORT).show()
            }else {
                if (PantallaCompletaPdf.visibility == View.VISIBLE){
                    PantallaCompletaPdf.visibility  =View.GONE

                }else
                    finish()
                //  Toast.makeText(applicationContext, "childEventListener detnido ", Toast.LENGTH_SHORT).show()
                databaseReferenceKerkly.removeEventListener(childEventListener!!);
                databaseReferenceCliente.removeEventListener(childEventListener2!!);

            }
        }
        }

}