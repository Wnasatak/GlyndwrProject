package assignment1.krzysztofoko.s16001089.utils

import android.content.Context
import android.util.Log
import assignment1.krzysztofoko.s16001089.AppConstants
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

/**
 * Utility object for sending emails using JavaMail API.
 * Handles 2FA codes, welcome emails, and order confirmations.
 */
object EmailUtils {
    private const val TAG = "EmailUtils"
    
    // SMTP Server Configuration
    private const val SMTP_HOST = "smtp.gmail.com"
    private const val SMTP_PORT = "587"
    private const val SMTP_USER = "prokocomp@gmail.com"
    private const val SMTP_PASS = "zxwe kbit dapj efcc" // App-specific password

    /**
     * Internal function to handle the core email sending logic via SMTP.
     */
    private suspend fun sendBaseEmail(
        context: Context?, 
        recipientEmail: String, 
        subject: String, 
        htmlBody: String
    ): Boolean = withContext(Dispatchers.IO) {
        // Configure SMTP properties for Gmail
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", SMTP_HOST)
            put("mail.smtp.port", SMTP_PORT)
            put("mail.smtp.connectiontimeout", "10000") 
            put("mail.smtp.timeout", "10000")
            put("mail.smtp.writetimeout", "10000")
        }

        // Authenticate the SMTP session
        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(SMTP_USER, SMTP_PASS)
            }
        })

        try {
            // Create a new MIME message
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(SMTP_USER, AppConstants.INSTITUTION))
                addRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
                this.subject = subject
            }

            // Use Multipart to allow both HTML content and embedded images (logo)
            val multipart = MimeMultipart("related")
            
            // 1. Create the HTML Text Part
            val htmlPart = MimeBodyPart()
            htmlPart.setContent(htmlBody, "text/html; charset=utf-8")
            multipart.addBodyPart(htmlPart)

            // 2. Attach the Institution Logo as an Inline Resource
            if (context != null) {
                try {
                    val logoPath = "images/media/GlyndwrUniversity.jpg"
                    context.assets.open(logoPath).use { inputStream ->
                        val bytes = inputStream.readBytes()
                        val dataSource: DataSource = ByteArrayDataSource(bytes, "image/jpeg")
                        val imagePart = MimeBodyPart()
                        imagePart.dataHandler = DataHandler(dataSource)
                        imagePart.setHeader("Content-ID", "<logo>") // References <img src='cid:logo'> in template
                        imagePart.disposition = MimeBodyPart.INLINE
                        imagePart.fileName = "logo.jpg"
                        multipart.addBodyPart(imagePart)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Logo error: ${e.message}")
                }
            }

            // Set content and send
            message.setContent(multipart)
            Transport.send(message)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Email failed: ${e.message}")
            false
        }
    }

    /**
     * Sends a 6-digit verification code for Login.
     */
    suspend fun send2FACode(context: Context?, recipientEmail: String, code: String): Boolean {
        return sendBaseEmail(context, recipientEmail, "Login Verification: $code", EmailTemplate.get2FAHtml(code))
    }

    /**
     * Sends a welcome email after successful registration.
     */
    suspend fun sendWelcomeEmail(context: Context?, recipientEmail: String, userName: String): Boolean {
        return sendBaseEmail(context, recipientEmail, "Welcome to ${AppConstants.INSTITUTION}!", EmailTemplate.getRegistrationHtml(userName))
    }

    /**
     * Sends a generic security update/reset notification.
     */
    suspend fun sendPasswordResetEmail(context: Context?, recipientEmail: String): Boolean {
        return sendBaseEmail(context, recipientEmail, "Password Reset Security Update", EmailTemplate.getPasswordResetHtml())
    }

    /**
     * Sends a detailed receipt after a purchase or enrollment.
     */
    suspend fun sendPurchaseConfirmation(
        context: Context?, 
        recipientEmail: String, 
        userName: String, 
        itemTitle: String, 
        orderRef: String, 
        price: String,
        category: String,
        details: Map<String, String> = emptyMap()
    ): Boolean {
        val isFree = price.contains("FREE", ignoreCase = true)
        val isCourse = category == AppConstants.CAT_COURSES
        val subjectPrefix = if (isCourse) "Enrollment Confirmed" else if (isFree) "Collection Confirmed" else "Order Confirmation"
        
        return sendBaseEmail(
            context, 
            recipientEmail, 
            "$subjectPrefix: $orderRef", 
            EmailTemplate.getPurchaseHtml(userName, itemTitle, orderRef, price, category, details)
        )
    }
}
