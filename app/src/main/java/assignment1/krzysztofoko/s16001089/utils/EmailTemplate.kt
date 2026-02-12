package assignment1.krzysztofoko.s16001089.utils

import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.ui.theme.HEX_BRAND_BLUE
import assignment1.krzysztofoko.s16001089.ui.theme.HEX_BRAND_TEAL

/**
 * EmailTemplate is a centralized engine for generating professionally branded HTML emails.
 * It follows a "Shell & Body" architectural pattern:
 * 1. Base Template (Shell): Provides the consistent institutional header, footer, and CSS styling.
 * 2. Specialized Generators (Body): Injects specific content (2FA, Receipts, etc.) into the shell.
 */
object EmailTemplate {
    // Branded color palette derived from institutional design guidelines
    private const val PRIMARY_COLOR = HEX_BRAND_BLUE
    private const val ACCENT_COLOR = HEX_BRAND_TEAL
    private const val SUCCESS_COLOR = "#00b894"

    /**
     * Inline CSS styles optimized for modern email clients.
     * Includes responsive layout wrappers, typography scales, and visual container styles.
     */
    private const val CSS_STYLES = """
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7f9; margin: 0; padding: 20px; color: #333; }
        .wrapper { width: 100%; table-layout: fixed; background-color: #f4f7f9; padding-bottom: 40px; }
        .container { width: 100%; max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.08); border-collapse: collapse; }
        .header { background-color: $PRIMARY_COLOR; padding: 45px 40px; text-align: center; color: #ffffff; }
        .logo { width: 80px; height: 80px; margin-bottom: 15px; border: 2px solid rgba(255,255,255,0.3); border-radius: 50%; padding: 5px; }
        .institution-name { font-size: 22px; font-weight: 700; letter-spacing: 0.5px; margin: 0; color: #ffffff; }
        .content { padding: 40px; }
        h1 { font-size: 28px; color: #2d3436; margin-top: 0; margin-bottom: 20px; font-weight: 800; text-align: left; }
        p { margin-bottom: 15px; font-size: 16px; color: #636e72; line-height: 1.6; }
        .highlight { color: $SUCCESS_COLOR; font-weight: 600; }
        .details-section { 
            background-color: #f8fbfb; 
            border: 1px solid #edf2f7;
            padding: 25px; 
            margin: 30px 0; 
            border-radius: 12px;
        }
        .info-table { width: 100%; border-collapse: collapse; }
        .info-row td { padding: 12px 0; font-size: 15px; border-bottom: 1px solid #f1f2f6; }
        .info-row:last-child td { border-bottom: none; }
        .info-label { color: #7f8c8d; width: 45%; font-weight: 500; text-align: left; }
        .info-value { color: #2d3436; font-weight: 700; text-align: right; }
        .section-header { 
            font-size: 14px; 
            font-weight: 800; 
            color: $PRIMARY_COLOR; 
            text-transform: uppercase; 
            letter-spacing: 1.2px; 
            margin-bottom: 15px;
            display: block;
        }
        .footer { padding: 30px; text-align: center; font-size: 13px; color: #b2bec3; background-color: #fafbfc; border-top: 1px solid #f1f2f6; width: 100%; }
        .button-container { text-align: center; margin-top: 10px; margin-bottom: 20px; }
        .button { 
            display: inline-block; 
            padding: 16px 45px; 
            background-color: $PRIMARY_COLOR; 
            color: #ffffff !important; 
            text-decoration: none; 
            border-radius: 12px; 
            font-weight: 700; 
            font-size: 16px;
            box-shadow: 0 4px 15px rgba(0,45,86,0.2);
        }
        .divider { height: 1px; background-color: #edf2f7; margin: 25px 0; border: none; }
    """

    /**
     * The unified "Shell" for all emails.
     * Wraps the provided [bodyContent] in institutional branding and metadata.
     * Uses 'cid:logo' for the inline university emblem attached by EmailUtils.
     */
    private fun wrapInBaseTemplate(title: String, bodyContent: String): String {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>$CSS_STYLES</style>
            </head>
            <body>
                <div class="wrapper">
                    <table class="container" align="center" cellpadding="0" cellspacing="0">
                        <tr>
                            <td class="header">
                                <img src="cid:logo" alt="University Logo" class="logo">
                                <p class="institution-name">${AppConstants.INSTITUTION}</p>
                            </td>
                        </tr>
                        <tr>
                            <td class="content">
                                <h1>$title</h1>
                                $bodyContent
                            </td>
                        </tr>
                        <tr>
                            <td class="footer">
                                <div style="font-weight: bold; color: $PRIMARY_COLOR; margin-bottom: 5px;">${AppConstants.APP_NAME}</div>
                                &copy; ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} | Learning at the heart of the community.
                            </td>
                        </tr>
                    </table>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * Helper to generate a structured data table for order summaries or student info.
     */
    private fun generateInfoTable(data: Map<String, String>): String {
        if (data.isEmpty()) return ""
        return """
            <table class="info-table">
                ${data.entries.joinToString("") { (label, value) -> 
                    "<tr class='info-row'><td class='info-label'>$label</td><td class='info-value'>$value</td></tr>"
                }}
            </table>
        """.trimIndent()
    }

    // --- Content Generators ---

    /**
     * CURRENTLY IN USE: Generates the 2FA security verification email.
     * Features a high-contrast numeric display and a CTA to return to the app.
     */
    fun get2FAHtml(code: String): String {
        val body = """
            <p>Hi there,</p>
            <p>To keep your account secure, please use the following one-time verification code to complete your login process:</p>
            <div style="text-align: center; margin: 40px 0;">
                <div style="display: inline-block; background: #f0f4f8; padding: 25px 45px; border-radius: 16px; border: 2px dashed $ACCENT_COLOR;">
                    <span style="font-size: 48px; font-weight: 900; letter-spacing: 12px; color: $PRIMARY_COLOR; font-family: monospace;">$code</span>
                </div>
            </div>
            <p style="text-align: center; font-size: 14px; color: #95a5a6; margin-bottom: 30px;">
                This code is valid for 10 minutes. If you did not request this code, please ignore this email.
            </p>
            <div class="button-container">
                <a href="#" class="button">Go to Dashboard</a>
            </div>
        """.trimIndent()
        
        return wrapInBaseTemplate("Security Verification", body)
    }

    /**
     * CURRENTLY IN USE: Generates detailed purchase or enrollment receipts.
     * Dynamically adjusts labels (Order vs Enrollment) based on the item category.
     */
    fun getPurchaseHtml(
        userName: String, 
        itemTitle: String, 
        orderRef: String, 
        price: String, 
        category: String,
        details: Map<String, String> = emptyMap()
    ): String {
        val isFree = price.contains("FREE", ignoreCase = true)
        val isCourse = category == AppConstants.CAT_COURSES
        val isGear = category == AppConstants.CAT_GEAR
        
        val itemType = when (category) {
            AppConstants.CAT_COURSES -> if (isFree) "free course" else "course"
            AppConstants.CAT_GEAR -> "product"
            AppConstants.CAT_AUDIOBOOKS -> "audiobook"
            else -> "book"
        }

        val title = if (isCourse) "Enrollment Confirmed" else "Order Confirmed"
        val actionText = if (isCourse) {
            "successfully enrolled on your <span class='highlight'>$itemType</span>"
        } else if (isFree) {
            "successfully claimed your <span class='highlight'>free $itemType</span>"
        } else {
            "successfully purchased your <span class='highlight'>$itemType</span>"
        }

        val sectionLabel = when(category) {
            AppConstants.CAT_COURSES -> "Course Details"
            AppConstants.CAT_GEAR -> "Product Details"
            else -> "Book Details"
        }

        val summaryData = mutableMapOf(
            (if (isCourse) "Enrollment Ref" else "Order Number") to orderRef,
            "Amount Paid" to price
        )

        val body = """
            <p>Hi <strong>$userName</strong>,</p>
            <p>You have $actionText: <strong>$itemTitle</strong>.</p>
            
            <div class="details-section">
                ${generateInfoTable(summaryData)}
                ${if (details.isNotEmpty()) {
                    "<hr class='divider'><span class='section-header'>$sectionLabel:</span>" + generateInfoTable(details)
                } else ""}
            </div>

            <p style="font-size: 15px; margin-bottom: 30px;">
                ${if (isGear) "Your items are being prepared. You can collect them at the <strong>Student Hub (Wrexham Campus)</strong>. Please remember to bring your Student ID." else "Your content is now available! You can access it anytime through your personalized dashboard."}
            </p>
            
            <div class="button-container">
                <a href="#" class="button">Open Dashboard</a>
            </div>
            
            <p style="margin-top: 20px; font-size: 13px; color: #b2bec3; text-align: center;">
                You can view your full transaction history and invoices in the app settings.
            </p>
        """.trimIndent()

        return wrapInBaseTemplate(title, body)
    }

    /**
     * FUTURE UPDATE: Used for official student onboarding once registration is fully integrated.
     * Purpose: Welcomes the student to the institution and provides their first steps in the portal.
     */
    fun getRegistrationHtml(userName: String): String {
        val body = """
            <p>Hi <strong>$userName</strong>,</p>
            <p>We are delighted to have you with us. Your student account for <span class="highlight">${AppConstants.APP_NAME}</span> is now active.</p>
            <p>Our platform offers a seamless way to access academic resources, purchase university gear, and enroll in specialized courses designed to help you excel.</p>
            
            <div class="details-section" style="text-align: center;">
                <p style="font-weight: bold; color: $PRIMARY_COLOR; margin-bottom: 8px;">Ready to get started?</p>
                <p style="font-size: 14px; margin-bottom: 0;">Log in to explore the store and access your personalized student dashboard.</p>
            </div>

            <div class="button-container">
                <a href="#" class="button">Start Exploring</a>
            </div>
            
            <p style="margin-top: 30px; font-style: italic; text-align: center; color: #636e72;">
                Happy learning!<br><strong>The Glyndŵr Support Team</strong>
            </p>
        """.trimIndent()

        return wrapInBaseTemplate("Welcome to the Community!", body)
    }

    /**
     * FUTURE UPDATE: Reserved for the institutional account recovery flow.
     * Purpose: Delivers a secure link or notification when a password reset is requested, 
     * including safety warnings for account protection.
     */
    fun getPasswordResetHtml(): String {
        val body = """
            <p>Hello,</p>
            <p>A password reset request was recently made for your Glyndŵr account. If you initiated this request, please return to the app to complete the process securely.</p>
            
            <div class="details-section" style="background-color: #fff5f5; border: 1px solid #ff7675;">
                <p style="margin: 0; color: #d63031; font-weight: bold; font-size: 14px;">Important Security Notice:</p>
                <p style="margin: 8px 0 0 0; font-size: 13px; color: #636e72;">If you did not request a password reset, your account may be at risk. We recommend checking your active sessions in the app settings immediately.</p>
            </div>

            <p style="margin-bottom: 30px;">For your protection, your current password will remain unchanged until you confirm the reset through the application.</p>
            
            <div class="button-container">
                <a href="#" class="button">Open App</a>
            </div>
        """.trimIndent()

        return wrapInBaseTemplate("Security Update", body)
    }
}
