package com.example.kerklyv5.modelo

class usuariosCercanosPerfil(telefono: String, email: String, name: String, foto: String, token:String, fechaHora: String,hora: String, minuto:String, segundo:String,uid:String ,curp:String) {
    var telefono: String = telefono
    var correo: String = email
    var nombre: String = name
    var foto: String = foto
    var token: String = token
    var fechaHora: String = fechaHora
    var hora: String = hora
    var minuto:String = minuto
    var segundo:String = segundo
    var uid:String = uid
    var curp: String = curp
    constructor() : this("","","","","","","","","","","")

}