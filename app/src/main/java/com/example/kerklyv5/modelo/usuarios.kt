package com.example.kerklyv5.modelo

class usuarios(telefono: String, email: String, name: String, foto: String, currentDateTimeString: String, token: String,uid: String) {
    var telefono: String = telefono

    var correo: String = email

    var nombre: String = name

    var foto: String = foto

    var fechaHora: String = currentDateTimeString

    var token: String = token
    var uid: String = uid

    constructor() : this("","","","","", "","")

}