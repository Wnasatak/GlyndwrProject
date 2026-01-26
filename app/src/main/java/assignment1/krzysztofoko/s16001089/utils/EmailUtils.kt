package assignment1.krzysztofoko.s16001089.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.activation.DataHandler
import javax.activation.DataSource
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource

object EmailUtils {
    private const val TAG = "EmailUtils"
    
    private const val SMTP_HOST = "smtp.gmail.com"
    private const val SMTP_PORT = "587"
    private const val SMTP_USER = "prokocomp@gmail.com"
    private const val SMTP_PASS = "zxwe kbit dapj efcc"

    private suspend fun sendBaseEmail(
        context: Context?, 
        recipientEmail: String, 
        subject: String, 
        htmlBody: String
    ): Boolean = withContext(Dispatchers.IO) {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", SMTP_HOST)
            put("mail.smtp.port", SMTP_PORT)
            put("mail.smtp.connectiontimeout", "10000") 
            put("mail.smtp.timeout", "10000")
            put("mail.smtp.writetimeout", "10000")
        }

        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(SMTP_USER, SMTP_PASS)
            }
        })

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(SMTP_USER, "Glyndwr University"))
                addRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
                this.subject = subject
            }

            val multipart = MimeMultipart("related")
            val htmlPart = MimeBodyPart()
            htmlPart.setContent(htmlBody, "text/html; charset=utf-8")
            multipart.addBodyPart(htmlPart)

            // Logo part - only add if context is available
            if (context != null) {
                val imagePart = MimeBodyPart()
                try {
                    context.assets.open("images/media/GlyndwrUniversity.jpg").use { inputStream ->
                        val dataSource: DataSource = ByteArrayDataSource(inputStream.readBytes(), "image/jpeg")
                        imagePart.dataHandler = DataHandler(dataSource)
                        imagePart.setHeader("Content-ID", "<logo>")
                        imagePart.disposition = MimeBodyPart.INLINE
                        multipart.addBodyPart(imagePart)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Could not load logo: ${e.message}")
                }
            }

            message.setContent(multipart)
            Transport.send(message)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send email: ${e.message}")
            false
        }
    }

    suspend fun send2FACode(context: Context?, recipientEmail: String, code: String): Boolean {
        return sendBaseEmail(context, recipientEmail, "Login Verification: $code", EmailTemplate.get2FAHtml(code))
    }

    suspend fun sendWelcomeEmail(context: Context?, recipientEmail: String, userName: String): Boolean {
        return sendBaseEmail(context, recipientEmail, "Welcome to Glyndwr University!", EmailTemplate.getRegistrationHtml(userName))
    }

    suspend fun sendPasswordResetEmail(context: Context?, recipientEmail: String): Boolean {
        return sendBaseEmail(context, recipientEmail, "Password Reset Security Update", EmailTemplate.getPasswordResetHtml())
    }

    suspend fun sendPurchaseConfirmation(
        context: Context?, 
        recipientEmail: String, 
        userName: String, 
        itemTitle: String, 
        orderRef: String, 
        price: String
    ): Boolean {
        return sendBaseEmail(
            context, 
            recipientEmail, 
            "Order Confirmation: $orderRef", 
            EmailTemplate.getPurchaseHtml(userName, itemTitle, orderRef, price)
        )
    }
}
