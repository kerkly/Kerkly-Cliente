package com.example.kerklyv5.modelo

class usuariosSqlite(
    telefono: Long, foto: ByteArray?,
    nombre: String, apellidoPa: String, apellidoMa: String,
    correo:String) {
    var telefono: Long = telefono


    var nombre: String = nombre

    var foto: ByteArray = foto!!

    var apellidoPa: String = apellidoPa
    var apellidoMa: String = apellidoMa

    var correo: String = correo

    constructor() : this(0,null,"","","", "")

}