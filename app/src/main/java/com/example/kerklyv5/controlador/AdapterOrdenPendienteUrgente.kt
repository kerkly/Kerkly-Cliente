package com.example.kerklyv5.controlador

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kerklyv5.R
import com.example.kerklyv5.modelo.serial.OrdenPendiente
import com.example.kerklyv5.modelo.serial.OrdenPendienteUrgente
import com.example.kerklyv5.vista.fragmentos.LoadMoreListener
import java.text.SimpleDateFormat
import java.util.Locale


class AdapterOrdenPendienteUrgente(val dataset: ArrayList<OrdenPendienteUrgente>) :
    RecyclerView.Adapter<AdapterOrdenPendienteUrgente.ViewHolder>(),
    View.OnClickListener, Filterable {

    private lateinit var listener: View.OnClickListener
    lateinit var loadMoreListener: LoadMoreListener
    private var isLoading = false

    private var datasetFilteredOriginal: MutableList<OrdenPendienteUrgente> = ArrayList(dataset)
    var datasetFiltered = dataset


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txt_oficio: TextView = view.findViewById(R.id.oficio_txt_orden)

        init {}

        val txt_folio: TextView = view.findViewById(R.id.folio_txt_orden)

        init {}
        val txtFecha: TextView = view.findViewById(R.id.fecha_txt_orden)

        //val txtcorreok: TextView = view.findViewById(R.id.oficio_txt_correokerkly)
        init {}
        val problema : TextView = view.findViewById(R.id.txt_problema)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val fechaFormateada = formatearFecha(dataset[position].fechaP)
        viewHolder.txtFecha.text = fechaFormateada
        viewHolder.txt_folio.text = "Folio No. ${dataset[position].idPresupuesto}"
        viewHolder.txt_oficio.text = "Tipo de Servicio: ${dataset[position].nombreO}"
       // viewHolder.txtnombrek.text = "kerkly: ${dataset[position].n}"
        viewHolder.problema.text = " Problema: ${dataset[position].problema}"
      //  println("id ${datset[position].idPresupuesto} nombre ${datset[position].Nombre}")
       // viewHolder.txtcorreok.text = "${datset[position].correo_electronico}"
     //   viewHolder.txtnombrek.text = "${datset[position].NombreK}"

        // Verificar si estamos cerca del final y se debe cargar más
        if (loadMoreListener != null && position == dataset.size - 1 && !isLoading) {
            isLoading = true
            loadMoreListener.onLoadMore()
            println("Scroll detectado - position: $position, datset.size: ${dataset.size}")
        }
    }

    fun formatearFecha(fechaOriginal: String): String {
        try {
            // Formato de la fecha y hora devuelto por el servidor
            val formatoOriginal = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            // Parsear la fecha y hora original
            val fechaParseada = formatoOriginal.parse(fechaOriginal)

            // Nuevo formato deseado
            val nuevoFormato = SimpleDateFormat("h:mm a 'del' EEEE d 'de' MMMM yyyy", Locale.getDefault())

            // Formatear la fecha y hora parseada en el nuevo formato
            return nuevoFormato.format(fechaParseada!!)
        } catch (e: Exception) {
            e.printStackTrace()
            return fechaOriginal // En caso de error, devolver la fecha original sin formato
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycler_orden_pendiente, viewGroup, false)
        view.setOnClickListener(this)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return datasetFiltered.size
    }

    fun setOnClickListener(l: View.OnClickListener) {
        this.listener = l

    }

    override fun onClick(v: View?) {
        listener.onClick(v)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraints: CharSequence?): FilterResults {
                val filteredList = ArrayList<OrdenPendienteUrgente>()

                if (!constraints.isNullOrBlank()) {
                    val filterPattern = constraints.toString().toLowerCase(Locale.getDefault()).trim()

                    for (item in datasetFilteredOriginal) {
                        val idContratoString = item.idPresupuesto.toString().toLowerCase(Locale.getDefault())

                        if (idContratoString.contains(filterPattern)) {
                            // Agrega elementos que contienen el número buscado
                            println("item agregado ${item.idPresupuesto}")
                            filteredList.add(item)
                        }
                    }
                } else {
                    filteredList.addAll(dataset)
                }

                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(constraints: CharSequence?, results: FilterResults?) {
                if (results?.values != null) {
                    val filteredList = results.values as List<OrdenPendienteUrgente>

                    if (filteredList.isEmpty()) {
                        println("No se encontraron coincidencias")
                        isLoading = true
                        loadMoreListener.onLoadMore()
                    } else {
                        datasetFiltered.clear()
                        // Filtrar las coincidencias que contienen el número buscado y agregarlas al inicio
                        val matchingItems = filteredList.filter { item ->
                            val idContratoString =
                                item.idPresupuesto.toString().toLowerCase(Locale.getDefault())
                            idContratoString.contains(
                                constraints.toString().toLowerCase(Locale.getDefault()).trim()
                            )
                        }

                        // Agregar las coincidencias al inicio de la lista filtrada
                        datasetFiltered.addAll(matchingItems)
                        // Agregar el resto de los elementos al final de la lista filtrada
                        val nonMatchingItems = filteredList.filterNot { matchingItems.contains(it) }
                        datasetFiltered.addAll(nonMatchingItems)
                    }
                }else{
                    println("No hay resultados de filtrado")
                }
                notifyDataSetChanged()
            }
        }
    }

    fun showOriginalList() {
        println("adapter borrado tamaño de ${datasetFilteredOriginal.size}")
        // Limpiar la lista filtrada y agregar todos los elementos originales
        datasetFiltered.clear()
        datasetFiltered.addAll(datasetFilteredOriginal)
        notifyDataSetChanged()
    }

    fun masDatos(masDatos: ArrayList<OrdenPendienteUrgente>){
        datasetFilteredOriginal.addAll(masDatos)
    }

}