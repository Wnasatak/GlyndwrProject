package assignment1.krzysztofoko.s16001089

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.ui.theme.GlyndwrProjectTheme
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import assignment1.krzysztofoko.s16001089.ui.components.UserAvatar
import java.util.Locale

/**
 * DigitalIDActivity: Demonstrates the "Activities" requirement (8%).
 */
class DigitalIDActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userName = intent.getStringExtra("USER_NAME") ?: "Full Name"
        val studentId = intent.getStringExtra("STUDENT_ID") ?: "--------"
        val userRole = intent.getStringExtra("USER_ROLE") ?: "student"
        val photoUrl = intent.getStringExtra("USER_PHOTO")

        setContent {
            GlyndwrProjectTheme(theme = Theme.DARK) {
                IDCardScreen(userName, studentId, userRole, photoUrl) {
                    finish()
                }
            }
        }
    }
}

@Composable
fun IDCardScreen(name: String, id: String, role: String, photoUrl: String?, onClose: () -> Unit) {
    val cleanRole = role.lowercase(Locale.ROOT)
    
    // Professional header colors based on role
    val headerColor = when (cleanRole) {
        "admin" -> Color(0xFFB8860B) // Dark Goldenrod
        "teacher", "tutor" -> Color(0xFF1E3A8A) // University Navy
        else -> Color(0xFFB71C1C) // Deep Red
    }

    val (headerLabel, badgeLabel, badgeBg, badgeText) = when(cleanRole) {
        "admin" -> Quadruple(
            "UNIVERSITY ADMINISTRATION",
            "ADMINISTRATION",
            Color(0xFFFFD700).copy(alpha = 0.2f),
            Color(0xFFB8860B)
        )
        "teacher", "tutor" -> Quadruple(
            "UNIVERSITY FACULTY",
            "TEACHER",
            Color(0xFF3B82F6).copy(alpha = 0.2f),
            Color(0xFF1E3A8A)
        )
        else -> Quadruple(
            "UNIVERSITY IDENTITY",
            "OFFICIALLY ENROLLED",
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F)),
        contentAlignment = Alignment.Center
    ) {
        // Main Vertical ID Card
        Card(
            modifier = Modifier
                .width(320.dp)
                .height(520.dp),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Institutional Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(headerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = headerLabel,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                // 2. Main Identity Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Profile Photo with Professional Border
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(150.dp),
                            shape = CircleShape,
                            color = Color(0xFFF5F5F5)
                        ) {}
                        
                        UserAvatar(
                            photoUrl = photoUrl,
                            modifier = Modifier
                                .size(140.dp)
                                .border(4.dp, headerColor, CircleShape)
                        )
                    }

                    // Personal Details
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = name.uppercase(Locale.ROOT),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "ID: ${id.uppercase(Locale.ROOT)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray,
                            letterSpacing = 2.sp
                        )
                    }

                    // Dynamic Role Badge
                    Surface(
                        color = badgeBg,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, badgeText.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = badgeLabel,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = badgeText
                        )
                    }

                    // QR Code for security scanning
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.Black.copy(alpha = 0.7f)
                    )

                    // Validity Footer
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "VALID UNTIL: SEPT 2026",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.LightGray,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, null, tint = headerColor.copy(alpha = 0.3f), modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("OFFICIAL UNIVERSITY CREDENTIAL", fontSize = 8.sp, color = Color.LightGray)
                        }
                    }
                }
            }
        }

        // Close Button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(Icons.Default.Close, "Close", tint = Color.White)
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
