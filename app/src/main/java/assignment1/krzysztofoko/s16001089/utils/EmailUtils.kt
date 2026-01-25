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
    
    // SMTP connection settings for Google's mail server
    private const val SMTP_HOST = "smtp.gmail.com"
    private const val SMTP_PORT = "587" // TLS port
    private const val SMTP_USER = "prokocomp@gmail.com"
    private const val SMTP_PASS = "zxwe kbit dapj efcc" // App-specific password for security

    // Main helper function to handle the technical parts of sending any email
    private suspend fun sendBaseEmail(
        context: Context, 
        recipientEmail: String, 
        subject: String, 
        htmlBody: String
    ): Boolean = withContext(Dispatchers.IO) {
        // Step 1: Set up the mail server properties
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true") // Secure connection
            put("mail.smtp.host", SMTP_HOST)
            put("mail.smtp.port", SMTP_PORT)
        }

        // Step 2: Create a session with our credentials
        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(SMTP_USER, SMTP_PASS)
            }
        })

        try {
            // Step 3: Create the email message structure
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(SMTP_USER, "Glyndwr University"))
                addRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
                this.subject = subject
            }

            // Step 4: Handle complex content (HTML text + embedded images)
            val multipart = MimeMultipart("related")
            
            // Add the HTML body part
            val htmlPart = MimeBodyPart()
            htmlPart.setContent(htmlBody, "text/html; charset=utf-8")
            multipart.addBodyPart(htmlPart)

            // Add the University logo from the app's assets folder
            val imagePart = MimeBodyPart()
            try {
                context.assets.open("images/media/GlyndwrUniversity.jpg").use { inputStream ->
                    val dataSource: DataSource = ByteArrayDataSource(inputStream.readBytes(), "image/jpeg")
                    imagePart.dataHandler = DataHandler(dataSource)
                    imagePart.setHeader("Content-ID", "<logo>") // Matches 'cid:logo' in HTML
                    imagePart.disposition = MimeBodyPart.INLINE
                    multipart.addBodyPart(imagePart)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Could not load logo: ${e.message}")
            }

            // Step 5: Finalize and send the email
            message.setContent(multipart)
            Transport.send(message)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send email: ${e.message}")
            false
        }
    }

    // Function to send a 6-digit 2FA code for logins
    suspend fun send2FACode(context: Context, recipientEmail: String, code: String): Boolean {
        return sendBaseEmail(context, recipientEmail, "Login Verification: $code", EmailTemplate.get2FAHtml(code))
    }

    // Function to send a friendly welcome email after registration
    suspend fun sendWelcomeEmail(context: Context, recipientEmail: String, userName: String): Boolean {
        return sendBaseEmail(context, recipientEmail, "Welcome to Glyndwr University!", EmailTemplate.getRegistrationHtml(userName))
    }

    // Function to notify the student about a password reset request
    suspend fun sendPasswordResetEmail(context: Context, recipientEmail: String): Boolean {
        return sendBaseEmail(context, recipientEmail, "Password Reset Security Update", EmailTemplate.getPasswordResetHtml())
    }
}
