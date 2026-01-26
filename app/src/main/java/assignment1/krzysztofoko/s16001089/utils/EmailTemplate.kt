package assignment1.krzysztofoko.s16001089.utils

object EmailTemplate {
    private const val CSS_STYLES = """
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7f9; margin: 0; padding: 20px; }
        .container { max-width: 550px; margin: auto; background: #ffffff; border-radius: 20px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.05); }
        .header { background: linear-gradient(135deg, #002d56 0%, #004b8d 100%); padding: 40px; text-align: center; }
        .logo { width: 90px; height: 90px; border-radius: 50%; border: 4px solid #ffffff; box-shadow: 0 4px 8px rgba(0,0,0,0.2); }
        .content { padding: 45px 35px; text-align: center; color: #2d3436; }
        h1 { font-size: 26px; margin-bottom: 15px; color: #002d56; font-weight: 700; }
        p { font-size: 16px; line-height: 1.6; color: #636e72; margin-bottom: 20px; }
        .impact-box { background-color: #f8f9fa; border: 2px solid #00d2c1; border-radius: 15px; padding: 25px; margin: 25px 0; display: inline-block; min-width: 280px; }
        .impact-text { font-size: 32px; font-weight: 800; color: #002d56; }
        .footer { padding: 25px; text-align: center; font-size: 13px; color: #b2bec3; background-color: #fafbfc; border-top: 1px solid #f1f2f6; }
        .accent { color: #00d2c1; font-weight: 700; }
        .icon-box { font-size: 50px; margin-bottom: 15px; }
        .status-badge { background: #e0fcf9; color: #00d2c1; padding: 8px 16px; border-radius: 20px; font-weight: bold; font-size: 14px; display: inline-block; margin-bottom: 10px; }
        .receipt-table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        .receipt-row { border-bottom: 1px solid #f1f2f6; }
        .receipt-label { text-align: left; padding: 10px 0; color: #636e72; font-size: 14px; }
        .receipt-value { text-align: right; padding: 10px 0; color: #2d3436; font-weight: bold; }
    """

    private fun getBaseHtml(title: String, bodyContent: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>$CSS_STYLES</style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <img src="cid:logo" alt="Glyndwr Logo" class="logo">
                    </div>
                    <div class="content">
                        $bodyContent
                    </div>
                    <div class="footer">
                        <div style="font-weight: bold; color: #002d56; margin-bottom: 5px;">Wrexham Glyndwr University</div>
                        &copy; ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} Student Portal. <br>
                        Learning at the heart of the community.
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    fun get2FAHtml(code: String): String {
        return getBaseHtml(
            "Identity Verification",
            """
                <div class="status-badge">SECURITY CHECK</div>
                <h1>Confirm Your Identity</h1>
                <p>To complete your login to the <span class="accent">Student Portal</span>, please use the following secure code:</p>
                <div class="impact-box"><div class="impact-text" style="letter-spacing: 10px;">$code</div></div>
                <p style="font-size: 14px; color: #999;">Expired in 10 minutes. If this wasn't you, please secure your account.</p>
            """.trimIndent()
        )
    }

    fun getPurchaseHtml(userName: String, itemTitle: String, orderRef: String, price: String): String {
        return getBaseHtml(
            "Order Confirmed",
            """
                <div class="status-badge">ORDER RECEIVED</div>
                <h1>Success, $userName!</h1>
                <p>Your purchase for <span class="accent">$itemTitle</span> has been processed successfully.</p>
                
                <table class="receipt-table">
                    <tr class="receipt-row">
                        <td class="receipt-label">Order Reference</td>
                        <td class="receipt-value">$orderRef</td>
                    </tr>
                    <tr class="receipt-row">
                        <td class="receipt-label">Amount Paid</td>
                        <td class="receipt-value">$price</td>
                    </tr>
                </table>

                <div class="impact-box">
                    <div class="impact-text" style="font-size: 18px;">Pick-up Instructions</div>
                    <p style="margin: 10px 0 0 0; font-size: 14px; color: #636e72;">Please visit the <strong>Wrexham Student Hub</strong> with your student ID and the reference code above to collect your items.</p>
                </div>
                
                <p style="font-size: 13px; color: #b2bec3;">You can view your full invoice and order history in the app Dashboard at any time.</p>
            """.trimIndent()
        )
    }

    fun getRegistrationHtml(userName: String): String {
        return getBaseHtml(
            "Welcome!",
            """
                <div class="status-badge">ACCOUNT ACTIVE</div>
                <h1>Welcome, $userName!</h1>
                <div class="icon-box">🎓</div>
                <p>We're absolutely thrilled to have you join the <span class="accent">Glyndwr Community</span>. Your student account is now fully set up!</p>
                <div class="impact-box" style="background: #e0fcf9; border-color: #00d2c1;">
                    <div class="impact-text" style="font-size: 20px;">Ready to Explore?</div>
                    <p style="margin: 10px 0 0 0; font-size: 14px; color: #002d56;">Your journey toward excellence starts today.</p>
                </div>
                <p>Log in now to access your personalized library, course materials, and the university gear store.</p>
                <p style="margin-top: 30px; font-style: italic;">Happy learning!<br><strong>The Glyndwr Team</strong></p>
            """.trimIndent()
        )
    }

    fun getPasswordResetHtml(): String {
        return getBaseHtml(
            "Security Update",
            """
                <div class="status-badge" style="background: #fff5f5; color: #d63031;">PASSWORD RESET</div>
                <h1>Need a New Password?</h1>
                <div class="icon-box">🔑</div>
                <p>We received a request to reset your <span class="accent">Glyndwr Account</span> password. Don't worry, it happens to everyone!</p>
                <div class="impact-box" style="border-color: #d63031; background: #fff5f5;">
                    <div class="impact-text" style="font-size: 18px; color: #d63031;">Check Your App</div>
                    <p style="margin: 10px 0 0 0; font-size: 14px; color: #636e72;">Follow the instructions shown on your phone screen.</p>
                </div>
                <p style="font-size: 13px; color: #b2bec3;">If you didn't request this, you can safely ignore this email. Your current password remains secure.</p>
            """.trimIndent()
        )
    }
}
