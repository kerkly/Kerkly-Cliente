package com.example.kerklyv5.modelo

class usuariosKerkly(telefono: String, email: String, name: String, foto: String, currentDateTimeString: String, token: String, uid: String, curp:String) {
    var telefono: String = telefono

    var correo: String = email

    var nombre: String = name

    var foto: String = foto

    var fechaHora: String = currentDateTimeString

    var token: String = token
    var uid: String = uid
    var curp: String = curp
    constructor() : this("","","","","", "","","")

}