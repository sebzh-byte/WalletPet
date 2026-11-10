package com.example.walletpet.integration.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.walletpet.domain.model.Meta
import com.example.walletpet.domain.model.ReporteMensual
import com.example.walletpet.domain.model.pesosConCentavos
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * INTEGRACIÓN: PDF. Crea el reporte mensual con la API nativa PdfDocument (sin librerías).
 * Se llama desde el ViewModel en un hilo aparte (Dispatchers.IO).
 */
class PdfReporter(private val context: Context) {

    private val ancho = 595   // A4 en puntos
    private val alto = 842
    private val margen = 48f

    fun generar(reporte: ReporteMensual, metas: List<Meta>): Uri {
        val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val titulo = Paint().apply { textSize = 22f; isFakeBoldText = true; color = Color.rgb(207, 155, 90) }
        val subtitulo = Paint().apply { textSize = 14f; isFakeBoldText = true; color = Color.rgb(111, 123, 46) }
        val texto = Paint().apply { textSize = 12f; color = Color.DKGRAY }

        val doc = PdfDocument()
        try {
            val escritor = Escritor(doc)
            escritor.linea("WalletPet · Reporte de ${reporte.titulo}", titulo, 34f)
            escritor.linea("Generado: ${formato.format(Date())}", texto)
            escritor.espacio()

            escritor.linea("Resumen del mes", subtitulo, 24f)
            escritor.linea("Total ahorrado: ${reporte.totalAhorrado.pesosConCentavos()} MXN en ${reporte.aportaciones} aportaciones", texto)
            escritor.linea("Gastos registrados: ${reporte.gastos.pesosConCentavos()} MXN", texto)
            escritor.linea("Ingresos registrados: ${reporte.ingresos.pesosConCentavos()} MXN", texto)
            escritor.espacio()

            escritor.linea("Ahorro por meta", subtitulo, 24f)
            if (reporte.porMeta.isEmpty()) escritor.linea("Este mes no hubo ahorros.", texto)
            reporte.porMeta.forEach { (meta, monto) -> escritor.linea("• $meta: ${monto.pesosConCentavos()} MXN", texto) }
            escritor.espacio()

            escritor.linea("Avance actual de tus metas", subtitulo, 24f)
            metas.forEach { m ->
                escritor.linea(
                    "• ${m.nombre}: ${m.ahorrado.pesosConCentavos()} de ${m.objetivo.pesosConCentavos()} (${(m.progreso * 100).toInt()}%)",
                    texto
                )
            }
            escritor.espacio()

            if (reporte.listaGastos.isNotEmpty()) {
                escritor.linea("Gastos del mes", subtitulo, 24f)
                reporte.listaGastos.forEach { g ->
                    escritor.linea(
                        "• ${g.nombre.ifEmpty { g.categoria }} (${g.categoria}): ${g.monto.pesosConCentavos()} MXN",
                        texto
                    )
                }
                escritor.espacio()
            }

            escritor.linea("Detalle de aportaciones", subtitulo, 24f)
            reporte.ahorros.forEach { a ->
                escritor.linea("${formato.format(Date(a.fechaMillis))}   ${a.metaNombre}   ${a.monto.pesosConCentavos()} MXN", texto)
            }
            escritor.terminar()

            val carpeta = (context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: File(context.filesDir, "Documents")).apply { mkdirs() }
            val archivo = File(carpeta, "reporte_${reporte.clave}.pdf")
            FileOutputStream(archivo).use { doc.writeTo(it) }

            return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
        } finally {
            doc.close()
        }
    }

    /** Ayudante que escribe línea por línea y cambia de hoja cuando se acaba el espacio. */
    private inner class Escritor(private val doc: PdfDocument) {
        private var numero = 0
        private var pagina: PdfDocument.Page? = null
        private var y = 0f

        private fun canvas(): Canvas {
            val actual = pagina
            if (actual != null && y <= alto - margen) return actual.canvas
            actual?.let { doc.finishPage(it) }
            numero++
            val nueva = doc.startPage(PdfDocument.PageInfo.Builder(ancho, alto, numero).create())
            pagina = nueva
            y = margen + 10f
            return nueva.canvas
        }

        fun linea(texto: String, paint: Paint, salto: Float = 20f) {
            // Corta el texto si no cabe a lo ancho
            val maximo = ancho - margen * 2
            var resto = texto
            while (resto.isNotEmpty()) {
                val cuantos = paint.breakText(resto, true, maximo, null).coerceAtLeast(1)
                val c = canvas()
                c.drawText(resto.substring(0, cuantos), margen, y, paint)
                y += salto
                resto = resto.substring(cuantos).trimStart()
            }
        }

        fun espacio() { y += 10f }

        fun terminar() {
            if (pagina == null) canvas()
            pagina?.let { doc.finishPage(it) }
            pagina = null
        }
    }
}
