package com.example.yamidpico.aliadosapp.email

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Environment
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import java.io.FileOutputStream


class EmailManager {

    private val username = "ypicoleal@gmail.com"
    private val password = "vbkreasinupdfpyi"

    private val props = Properties().apply {
        put("mail.smtp.auth", "true")
        put("mail.smtp.starttls.enable", "true")
        put("mail.smtp.host", "smtp.gmail.com")
        put("mail.smtp.port", "587")
    }


    fun sendMessage(to: String, subject: String, body: String, qrCode: String) {
        val session = Session.getInstance(props,
            object : javax.mail.Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })

        Thread(Runnable {
            try {
                val message = MimeMessage(session)
                message.setFrom(InternetAddress("benditabeach@gmail.com"))
                message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to))
                message.subject = subject
                message.setText(body)

                val attachmentBodyPart = MimeBodyPart()

                val multipart = MimeMultipart()

                attachmentBodyPart.fileName = "Reservation QR"
                attachmentBodyPart.attachFile(buildQR(qrCode))

                val contentBodyPart = MimeBodyPart().apply {
                    setText(body)
                }

                multipart.addBodyPart(contentBodyPart)
                multipart.addBodyPart(attachmentBodyPart)

                message.setContent(multipart)

                Transport.send(message)

                Log.e("tales5", "message sent")

            } catch (e: MessagingException) {
                e.printStackTrace()
            }
        }).start()
    }

    private fun buildQR(content: String) : File? {
        val bitmap = createQRCode(content) ?: return null
        val path = Environment.getExternalStorageDirectory().toString()
        val file = File(path, "ReservationCode$content.jpg".replace("/", "_"))
        val fOut = FileOutputStream(file)

        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut) // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
        fOut.flush()
        fOut.close()

        return file
    }

    private fun createQRCode(content: String): Bitmap? {
        val writer = QRCodeWriter()
        return try {
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }
}