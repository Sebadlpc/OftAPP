package com.example.oftapp.util

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.oftapp.data.local.entity.ExamEntity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Utilidades para la previsualización nativa y exportación/descarga de informes clínicos en formato PDF.
 */
object DocumentUtils {

    /**
     * Lanza un Intent(Intent.ACTION_VIEW) para previsualizar el informe o documento adjunto con el visor del sistema.
     * Si el examen no tiene un archivo físico asociado, genera un informe PDF válido en el directorio de caché.
     */
    fun previewDocument(context: Context, exam: ExamEntity) {
        try {
            val fileUri: Uri = exam.fileUri ?: generateCachePdfUri(context, exam)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Previsualizar documento clínico")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                "No se encontró un visor de PDF instalado en el dispositivo",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Error al abrir el documento: ${e.localizedMessage ?: "desconocido"}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Guarda una copia del archivo adjunto o informe generado en la carpeta pública de Descargas (Downloads) del dispositivo.
     */
    fun downloadDocument(context: Context, exam: ExamEntity, onResult: (String) -> Unit) {
        try {
            val sanitizedExamType = exam.examType.replace("\\s+".toRegex(), "_")
            val fileName = "informe_${sanitizedExamType}_${exam.id}.pdf"

            val pdfBytes: ByteArray = if (exam.fileUri != null) {
                try {
                    context.contentResolver.openInputStream(exam.fileUri)?.use { it.readBytes() }
                } catch (_: Exception) {
                    null
                } ?: generateClinicalPdf(exam)
            } else {
                generateClinicalPdf(exam)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_DOWNLOADS}/OftAPP"
                    )
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(pdfBytes)
                        outputStream.flush()
                    }
                    onResult("Descarga exitosa: Descargas/OftAPP/$fileName")
                } else {
                    onResult("Error al crear el archivo en Descargas")
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val appDir = File(downloadsDir, "OftAPP").apply { if (!exists()) mkdirs() }
                val targetFile = File(appDir, fileName)
                FileOutputStream(targetFile).use { fos ->
                    fos.write(pdfBytes)
                    fos.flush()
                }
                onResult("Descarga exitosa: Descargas/OftAPP/$fileName")
            }
        } catch (e: Exception) {
            onResult("Error en la descarga: ${e.localizedMessage ?: "Error de E/S"}")
        }
    }

    /**
     * Genera un archivo PDF temporal en caché y retorna su Uri mediante FileProvider.
     */
    fun generateCachePdfUri(context: Context, exam: ExamEntity): Uri {
        val cacheFile = File(context.cacheDir, "informe_${exam.id}.pdf")
        val pdfBytes = generateClinicalPdf(exam)
        FileOutputStream(cacheFile).use { fos ->
            fos.write(pdfBytes)
            fos.flush()
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheFile
        )
    }

    /**
     * Construye un documento PDF clínico estandarizado usando la API PdfDocument nativa de Android.
     */
    fun generateClinicalPdf(exam: ExamEntity): ByteArray {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Formato A4
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // Encabezado superior con color corporativo
        paint.color = Color.rgb(11, 79, 138)
        canvas.drawRect(0f, 0f, 595f, 75f, paint)

        // Título del encabezado
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("OftApp - Ficha e Informe Clínico", 35f, 48f, paint)

        // Subtítulo
        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("Sistema de Trazabilidad Oftalmológica Especializada", 35f, 65f, paint)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CL"))
        val fechaFormateada = try {
            dateFormat.format(exam.date)
        } catch (_: Exception) {
            "N/D"
        }

        var y = 115f
        paint.color = Color.rgb(11, 79, 138)
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("1. IDENTIFICACIÓN DE LA ATENCIÓN", 35f, y, paint)

        paint.color = Color.rgb(50, 50, 50)
        paint.textSize = 12f
        paint.isFakeBoldText = false
        y += 24f
        canvas.drawText("Código / ID Examen: ${exam.id}", 35f, y, paint)
        y += 20f
        canvas.drawText("Paciente: ${exam.patientName}", 35f, y, paint)
        y += 20f
        canvas.drawText("Fecha y Hora: $fechaFormateada", 35f, y, paint)
        y += 20f
        canvas.drawText("Sucursal / Sede: ${exam.branch}", 35f, y, paint)
        y += 20f
        canvas.drawText("Profesional a Cargo: ${exam.doctorName}", 35f, y, paint)

        y += 35f
        paint.color = Color.rgb(11, 79, 138)
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("2. PROCEDIMIENTO EVALUADO", 35f, y, paint)

        paint.color = Color.rgb(50, 50, 50)
        paint.textSize = 12f
        paint.isFakeBoldText = false
        y += 24f
        canvas.drawText("Tipo de Examen: ${exam.examType}", 35f, y, paint)
        y += 20f
        canvas.drawText("Ojo Evaluado: ${exam.eye}", 35f, y, paint)
        y += 20f
        canvas.drawText("Estado de Validación: ${exam.status}", 35f, y, paint)

        y += 35f
        paint.color = Color.rgb(11, 79, 138)
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("3. OBSERVACIONES Y NOTAS CLÍNICAS", 35f, y, paint)

        paint.color = Color.rgb(50, 50, 50)
        paint.textSize = 11f
        paint.isFakeBoldText = false
        y += 24f
        val notesText = exam.notes.ifBlank { "Sin observaciones adicionales registradas para este procedimiento." }
        
        // Dividir líneas largas de observaciones
        val words = notesText.split(" ")
        var line = ""
        for (w in words) {
            if ((line + w).length > 70) {
                canvas.drawText(line.trim(), 35f, y, paint)
                y += 18f
                line = "$w "
            } else {
                line += "$w "
            }
        }
        if (line.isNotBlank()) {
            canvas.drawText(line.trim(), 35f, y, paint)
            y += 18f
        }

        // Pie de página
        paint.color = Color.rgb(150, 150, 150)
        paint.textSize = 9f
        canvas.drawText(
            "Documento emitido electrónicamente por OftApp. Validez médica sujeta a firma y protocolo clínico institucional.",
            35f,
            810f,
            paint
        )

        document.finishPage(page)
        val stream = ByteArrayOutputStream()
        document.writeTo(stream)
        document.close()
        return stream.toByteArray()
    }
}
