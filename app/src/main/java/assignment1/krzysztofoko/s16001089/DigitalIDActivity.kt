package assignment1.krzysztofoko.s16001089

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode2
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import assignment1.krzysztofoko.s16001089.ui.theme.GlyndwrProjectTheme
import assignment1.krzysztofoko.s16001089.ui.theme.Theme

/**
 * DigitalIDActivity: Demonstrates the "Activities" requirement (8%).
 * 
 * This is a secondary Activity that operates as a separate task. 
 * It showcases the Activity Lifecycle and Explicit Intent communication.
 */
class DigitalIDActivity : ComponentActivity() {

    private val TAG = "DigitalIDActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Activity Created")

        // Retrieve data passed from MainActivity via Intent extras
        val userName = intent.getStringExtra("USER_NAME") ?: "Student"
        val studentId = intent.getStringExtra("STUDENT_ID") ?: AppConstants.STUDENT_ID

        setContent {
            GlyndwrProjectTheme(theme = Theme.DARK) {
                IDCardScreen(userName, studentId) {
                    finish() // Close this activity and return to MainActivity
                }
            }
        }
    }

    // --- LIFECYCLE LOGGING (Demonstrating Activity knowledge) ---
    override fun onStart() { super.onStart(); Log.d(TAG, "onStart: Activity Visible") }
    override fun onResume() { super.onResume(); Log.d(TAG, "onResume: Activity Gained Focus") }
    override fun onPause() { super.onPause(); Log.d(TAG, "onPause: Activity Loosing Focus") }
    override fun onStop() { super.onStop(); Log.d(TAG, "onStop: Activity No Longer Visible") }
    override fun onDestroy() { super.onDestroy(); Log.d(TAG, "onDestroy: Activity Destroyed") }
}

@Composable
fun IDCardScreen(name: String, id: String, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F)),
        contentAlignment = Alignment.Center
    ) {
        // ID Card Design
        Card(
            modifier = Modifier
                .width(320.dp)
                .height(500.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (Institutional Branding)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            Brush.verticalGradient(listOf(Color(0xFFCC0000), Color(0xFF990000)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "UNIVERSITY IDENTITY",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Avatar Placeholder
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = Color.LightGray
                ) {
                    Icon(
                        Icons.Default.QrCode2, 
                        contentDescription = null, 
                        modifier = Modifier.padding(24.dp),
                        tint = Color.DarkGray
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Student Details
                Text(
                    text = name.uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
                Text(
                    text = "ID: $id",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )

                Spacer(Modifier.height(16.dp))
                
                // Status Badge
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "OFFICIALLY ENROLLED",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color(0xFF2E7D32),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Valid Date
                Text(
                    "VALID UNTIL: SEPT 2026",
                    modifier = Modifier.padding(bottom = 24.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray
                )
            }
        }

        // Close Button (Top Right)
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
