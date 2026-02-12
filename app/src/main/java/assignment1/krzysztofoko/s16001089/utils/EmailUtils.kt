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
 * EmailUtils is a robust utility service built on the JavaMail (Jakarta Mail) API.
 * It centralizes all outgoing email communications for the application, including:
 * - Security: Multi-Factor Authentication (2FA) delivery.
 * - Onboarding: Welcome emails for new university members.
 * - E-commerce: Instant digital receipts for store purchases and course enrollments.
 * - Account Management: Password reset and security update notifications.
 */
object EmailUtils {
    private const val TAG = "EmailUtils"
    
    // SMTP Server Configuration (Gmail specific)
    private const val SMTP_HOST = "smtp.gmail.com"
    private const val SMTP_PORT = "587"
    private const val SMTP_USER = "prokocomp@gmail.com"
    private const val SMTP_PASS = "zxwe kbit dapj efcc" // App-specific password (Security Note: Should ideally be externalized)

    /**
     * Core internal function that handles the low-level SMTP protocol communication.
     * It manages session authentication, MIME message construction, and multi-part
     * content delivery (HTML + Inline Images).
     *
     * @param context Required to access assets for branding (logos).
     * @param recipientEmail Target address.
     * @param subject Email subject line.
     * @param htmlBody The rendered HTML template content.
     */
    private suspend fun sendBaseEmail(
        context: Context?, 
        recipientEmail: String, 
        subject: String, 
        htmlBody: String
    ): Boolean = withContext(Dispatchers.IO) {
        // Configure SMTP properties for secure STARTTLS communication
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", SMTP_HOST)
            put("mail.smtp.port", SMTP_PORT)
            put("mail.smtp.connectiontimeout", "10000") // 10s connection timeout
            put("mail.smtp.timeout", "10000")           // 10s read timeout
            put("mail.smtp.writetimeout", "10000")      // 10s write timeout
        }

        // Initialize SMTP Session with the app-specific password authentication
        val session = Session.getInstance(props, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(SMTP_USER, SMTP_PASS)
            }
        })

        try {
            // Construct the MIME message container
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(SMTP_USER, AppConstants.INSTITUTION))
                addRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
                this.subject = subject
            }

            // MimeMultipart("related") allows embedding images that are referenced by the HTML
            val multipart = MimeMultipart("related")
            
            // 1. HTML BODY PART: The main content of the email
            val htmlPart = MimeBodyPart()
            htmlPart.setContent(htmlBody, "text/html; charset=utf-8")
            multipart.addBodyPart(htmlPart)

            // 2. EMBEDDED BRANDING: Loads the university logo from app assets and attaches it inline
            if (context != null) {
                try {
                    val logoPath = "images/media/GlyndwrUniversity.jpg"
                    context.assets.open(logoPath).use { inputStream ->
                        val bytes = inputStream.readBytes()
                        val dataSource: DataSource = ByteArrayDataSource(bytes, "image/jpeg")
                        val imagePart = MimeBodyPart()
                        imagePart.dataHandler = DataHandler(dataSource)
                        // Content-ID allows the HTML template to use <img src='cid:logo'>
                        imagePart.setHeader("Content-ID", "<logo>") 
                        imagePart.disposition = MimeBodyPart.INLINE
                        imagePart.fileName = "logo.jpg"
                        multipart.addBodyPart(imagePart)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Logo attachment failed: ${e.message}")
                }
            }

            // Finalize and transmit the email
            message.setContent(multipart)
            Transport.send(message)
            true
        } catch (e: Exception) {
            Log.e(TAG, "SMTP Transmission Error: ${e.message}")
            false
        }
    }

    /**
     * CURRENTLY IN USE: Delivers a 6-digit numeric code for Identity Verification (2FA).
     * Integration Point: AuthViewModel.kt during the login/verification flow.
     */
    suspend fun send2FACode(context: Context?, recipientEmail: String, code: String): Boolean {
        return sendBaseEmail(context, recipientEmail, "Login Verification: $code", EmailTemplate.get2FAHtml(code))
    }

    /**
     * FUTURE UPDATE: Planned for the finalized registration flow.
     * This will be triggered once the university database creates a new official student profile.
     * Use Case: Welcome onboarding, providing initial login instructions and institutional resources.
     */
    suspend fun sendWelcomeEmail(context: Context?, recipientEmail: String, userName: String): Boolean {
        return sendBaseEmail(context, recipientEmail, "Welcome to ${AppConstants.INSTITUTION}!", EmailTemplate.getRegistrationHtml(userName))
    }

    /**
     * FUTURE UPDATE: Reserved for Account Recovery services.
     * This will be implemented in the next security sprint to handle forgot-password requests.
     * Use Case: Secure token delivery for resetting institutional portal passwords.
     */
    suspend fun sendPasswordResetEmail(context: Context?, recipientEmail: String): Boolean {
        return sendBaseEmail(context, recipientEmail, "Password Reset Security Update", EmailTemplate.getPasswordResetHtml())
    }

    /**
     * CURRENTLY IN USE: Generates and sends digital receipts/enrollment confirmations.
     * Integration Point: Various ViewModels (e.g., AudioBookViewModel.kt) after a successful transaction.
     * Logic: Automatically detects if the item is a course, book, or gear to adjust the subject and template.
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
