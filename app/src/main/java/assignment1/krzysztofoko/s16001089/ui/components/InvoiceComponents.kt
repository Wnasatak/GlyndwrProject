package assignment1.krzysztofoko.s16001089.ui.components // the package where our UI components are stored

import androidx.compose.animation.* // for smooth transitions and animations
import androidx.compose.animation.core.* // core tools for building custom animations
import androidx.compose.foundation.BorderStroke // to create outlines around boxes
import androidx.compose.foundation.layout.* // for layouts like Column, Row, and padding
import androidx.compose.foundation.shape.CircleShape // for making things perfectly round
import androidx.compose.foundation.shape.RoundedCornerShape // for boxes with rounded corners
import androidx.compose.material.icons.Icons // the standard icon collection
import androidx.compose.material.icons.automirrored.filled.ArrowBack // a back arrow icon
import androidx.compose.material.icons.filled.* // imports all other standard icons
import androidx.compose.material3.* // the modern Google Material Design 3 library
import androidx.compose.runtime.* // for managing app "state" and memory
import androidx.compose.ui.Alignment // for centering or aligning items
import androidx.compose.ui.Modifier // the main tool to change look and size
import androidx.compose.ui.draw.alpha // for making things transparent
import androidx.compose.ui.draw.clip // for cutting shapes out of images
import androidx.compose.ui.graphics.Color // for picking and using colors
import androidx.compose.ui.graphics.graphicsLayer // for advanced transforms like rotation
import androidx.compose.ui.layout.ContentScale // for fitting images into boxes
import androidx.compose.ui.text.font.FontWeight // for bold or thin text
import androidx.compose.ui.text.style.TextAlign // for aligning text within a block
import androidx.compose.ui.unit.dp // unit for measuring layout sizes
import androidx.compose.ui.unit.sp // unit for measuring text sizes
import assignment1.krzysztofoko.s16001089.data.Book // the Book data class we created
import coil.compose.AsyncImage // a tool to load images from assets or web
import kotlinx.coroutines.delay // used to pause code for a moment

@OptIn(ExperimentalMaterial3Api::class) // tells Android we're using a newer API
@Composable // tells Android this function draws part of the UI
fun InvoiceCreatingScreen( // the main function for this screen
    book: Book, // the book being purchased
    onCreationComplete: () -> Unit, // what happens when the invoice is done
    onBack: () -> Unit, // what happens when the back button is clicked
    isDarkTheme: Boolean, // if the app is currently in dark mode
    onToggleTheme: () -> Unit // function to switch between light and dark
) {
    var progress by remember { mutableFloatStateOf(0f) } // tracks 0% to 100% progress
    var isComplete by remember { mutableStateOf(false) } // tracks if generation is finished

    // Animation for the university logo to spin once when screen opens
    val rotation = remember { Animatable(0f) } // starting rotation at 0
    LaunchedEffect(Unit) { // runs this code once when screen loads
        rotation.animateTo(
            targetValue = 360f, // target a full circle spin
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing) // spin for 1 second
        )
    }

    LaunchedEffect(Unit) { // side-effect to simulate the "work" being done
        // This loop simulates generating a document
        while (progress < 1f) {
            delay(50) // wait 50 milliseconds
            progress += 0.02f // increase progress slightly
        }
        isComplete = true // mark as finished
        delay(1000) // wait 1 second so the user can see the "Success" icon
        onCreationComplete() // move to the next screen (the actual invoice)
    }

    Box(modifier = Modifier.fillMaxSize()) { // the base layer of the screen
        HorizontalWavyBackground(isDarkTheme = isDarkTheme) // the animated background waves
        
        Scaffold( // a standard screen structure (header, body, etc)
            containerColor = Color.Transparent, // let the background waves show through
            topBar = { // the header at the top
                CenterAlignedTopAppBar( // a header with a centered title
                    windowInsets = WindowInsets(0, 0, 0, 0), // remove default padding
                    title = { Text("Processing", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }, // title text
                    navigationIcon = { // the button on the left
                        IconButton(onClick = onBack) { // clickable back button
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") // the back arrow icon
                        }
                    },
                    actions = { // buttons on the right
                        IconButton(onClick = onToggleTheme) { // theme switcher button
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, // icon changes based on theme
                                contentDescription = "Toggle Theme"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f) // slightly transparent header
                    )
                )
            }
        ) { padding -> // 'padding' is the area below the header
            Column( // stacks items vertically in the center
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp), // add space around the content
                horizontalAlignment = Alignment.CenterHorizontally, // center everything horizontally
                verticalArrangement = Arrangement.Center // center everything vertically
            ) {
                Card( // a white/dark card that holds the main info
                    modifier = Modifier.fillMaxWidth(), // make it full width
                    shape = RoundedCornerShape(28.dp), // nice round corners
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)), // color matches theme
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)) // thin border
                ) {
                    Column( // contents inside the card
                        modifier = Modifier.padding(32.dp), // space inside the card
                        horizontalAlignment = Alignment.CenterHorizontally // center everything
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) { // area for the progress circle or success icon
                            if (!isComplete) { // while still loading
                                CircularProgressIndicator( // a spinning circle
                                    progress = { progress }, // link to our progress variable
                                    modifier = Modifier.fillMaxSize(), // fill the 120dp box
                                    strokeWidth = 8.dp, // thickness of the line
                                    color = MaterialTheme.colorScheme.primary, // main app color
                                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) // faint background of the circle
                                )
                                AsyncImage( // the logo in the middle of the circle
                                    model = "file:///android_asset/images/media/GlyndwrUniversity.jpg", // path to logo asset
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(64.dp) // size of the logo
                                        .graphicsLayer { rotationZ = rotation.value } // apply the spinning animation
                                        .clip(CircleShape), // make the logo round
                                    contentScale = ContentScale.Crop // fill the circle properly
                                )
                            } else { // when loading is finished
                                this@Column.AnimatedVisibility( // smooth fade-in for the checkmark
                                    visible = isComplete,
                                    enter = scaleIn() + fadeIn() // zoom in and fade in effect
                                ) {
                                    Icon( // the big green checkmark
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Complete",
                                        modifier = Modifier.size(100.dp), // big size
                                        tint = Color(0xFF4CAF50) // success green color
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp)) // space below the icon

                        Text( // the headline text
                            text = if (isComplete) "Invoice Generated!" else "Generating Invoice...", // text changes when done
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold, // very thick text
                            textAlign = TextAlign.Center, // center the text
                            modifier = Modifier.fillMaxWidth(),
                            color = if (isComplete) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary // color changes when done
                        )

                        Spacer(modifier = Modifier.height(12.dp)) // small space

                        Text( // the descriptive subtext
                            text = if (isComplete) 
                                "Your official document for '${book.title}' is ready for viewing." // message when finished
                                else "Please wait while we prepare your academic purchase records and apply student discounts.", // message while loading
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Gray, // neutral gray color
                            lineHeight = 22.sp // space between lines of text
                        )

                        if (!isComplete) { // only show the progress bar if not finished
                            Spacer(modifier = Modifier.height(24.dp)) // space above bar
                            LinearProgressIndicator( // horizontal progress bar
                                progress = { progress }, // link to progress variable
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape), // make the bar ends round
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                            Text( // percentage text below the bar
                                text = "${(progress * 100).toInt()}%", // convert 0.5 to 50%
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(top = 8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp)) // space below the card
                
                // Professional hint at the bottom
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().alpha(0.6f), // make it slightly faded
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Description, null, modifier = Modifier.size(16.dp), tint = Color.Gray) // small paper icon
                    Spacer(modifier = Modifier.width(8.dp)) // tiny space
                    Text( // the official university seal text
                        "Certified by Glyndŵr University Academic Records",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
