package assignment1.krzysztofoko.s16001089.ui.details.course

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.components.*
import assignment1.krzysztofoko.s16001089.ui.theme.Theme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseEnrollmentScreen(
    courseId: String,
    onBack: () -> Unit,
    onEnrollmentSuccess: () -> Unit,
    currentTheme: Theme,
    onThemeChange: (Theme) -> Unit,
    viewModel: CourseDetailViewModel = viewModel(factory = CourseDetailViewModelFactory(
        courseDao = AppDatabase.getDatabase(LocalContext.current).courseDao(),
        userDao = AppDatabase.getDatabase(LocalContext.current).userDao(),
        courseId = courseId,
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    val course by viewModel.course.collectAsState()
    val isDarkTheme = currentTheme == Theme.DARK
    
    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 4

    // Form State
    var lastQualification by remember { mutableStateOf("") }
    var institution by remember { mutableStateOf("") }
    var graduationYear by remember { mutableStateOf("") }
    var englishLevel by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var nationality by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var emergencyName by remember { mutableStateOf("") }
    var emergencyPhone by remember { mutableStateOf("") }
    var motivation by remember { mutableStateOf("") }
    var portfolioUrl by remember { mutableStateOf("") }
    var supportReqs by remember { mutableStateOf("") }
    var cvAttached by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = { Text("Course Application", fontWeight = FontWeight.Black) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                    actions = {
                        ThemeToggleButton(
                            currentTheme = currentTheme,
                            onThemeChange = onThemeChange
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Progress Indicator
                LinearProgressIndicator(
                    progress = { currentStep.toFloat() / totalSteps.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
                Text(
                    text = "Step $currentStep of $totalSteps",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        AnimatedContent(targetState = currentStep, label = "stepTransition") { step ->
                            when (step) {
                                1 -> PersonalDetailsStep(dob, { dob = it }, nationality, { nationality = it }, gender, { gender = it })
                                2 -> AcademicBackgroundStep(lastQualification, { lastQualification = it }, institution, { institution = it }, graduationYear, { graduationYear = it }, englishLevel, { englishLevel = it })
                                3 -> SupportContactStep(emergencyName, { emergencyName = it }, emergencyPhone, { emergencyPhone = it }, supportReqs, { supportReqs = it })
                                4 -> MotivationStep(motivation, { motivation = it }, portfolioUrl, { portfolioUrl = it }, cvAttached, { cvAttached = it })
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            if (currentStep > 1) {
                                OutlinedButton(onClick = { currentStep-- }, modifier = Modifier.height(50.dp).weight(1f)) {
                                    @Suppress("DEPRECATION")
                                    Text("Previous")
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                            
                            Button(
                                onClick = {
                                    if (currentStep < totalSteps) {
                                        currentStep++
                                    } else {
                                        // Final Step: Submit Application for Review
                                        viewModel.submitEnrollmentApplication(
                                            CourseEnrollmentDetails(
                                                id = "${viewModel.userId}_$courseId",
                                                userId = viewModel.userId,
                                                courseId = courseId,
                                                lastQualification = lastQualification,
                                                institution = institution,
                                                graduationYear = graduationYear,
                                                englishProficiencyLevel = englishLevel,
                                                dateOfBirth = dob,
                                                nationality = nationality,
                                                gender = gender,
                                                emergencyContactName = emergencyName,
                                                emergencyContactPhone = emergencyPhone,
                                                motivationalText = motivation,
                                                portfolioUrl = portfolioUrl,
                                                specialSupportRequirements = supportReqs,
                                                cvFileName = if (cvAttached) "student_cv.pdf" else null
                                            )
                                        ) {
                                            onEnrollmentSuccess()
                                        }
                                    }
                                },
                                modifier = Modifier.height(50.dp).weight(1f)
                            ) {
                                @Suppress("DEPRECATION")
                                Text(if (currentStep == totalSteps) "Submit Application" else "Next")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalDetailsStep(dob: String, onDob: (String) -> Unit, nat: String, onNat: (String) -> Unit, gen: String, onGen: (String) -> Unit) {
    Column {
        @Suppress("DEPRECATION")
        Text("Personal Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        @Suppress("DEPRECATION")
        Text("Required for official university records.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = dob, onValueChange = onDob, label = { Text("Date of Birth (DD/MM/YYYY)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = nat, onValueChange = onNat, label = { Text("Nationality") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = gen, onValueChange = onGen, label = { Text("Gender") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
    }
}

@Composable
fun AcademicBackgroundStep(qual: String, onQual: (String) -> Unit, inst: String, onInst: (String) -> Unit, year: String, onYear: (String) -> Unit, eng: String, onEng: (String) -> Unit) {
    Column {
        @Suppress("DEPRECATION")
        Text("Academic Background", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        @Suppress("DEPRECATION")
        Text("Help us assess your entry requirements.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = qual, onValueChange = onQual, label = { Text("Highest Qualification (e.g. A-Levels, BSc)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = inst, onValueChange = onInst, label = { Text("Institution Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = year, onValueChange = onYear, label = { Text("Grad Year") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = eng, onValueChange = onEng, label = { Text("English Level") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }
    }
}

@Composable
fun SupportContactStep(name: String, onName: (String) -> Unit, phone: String, onPhone: (String) -> Unit, support: String, onSupport: (String) -> Unit) {
    Column {
        @Suppress("DEPRECATION")
        Text("Support & Contact", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = name, onValueChange = onName, label = { Text("Emergency Contact Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = phone, onValueChange = onPhone, label = { Text("Emergency Contact Phone") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = support, onValueChange = onSupport, label = { Text("Special Support Needs (Optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 3, shape = RoundedCornerShape(12.dp))
    }
}

@Composable
fun MotivationStep(mot: String, onMot: (String) -> Unit, port: String, onPort: (String) -> Unit, cv: Boolean, onCv: (Boolean) -> Unit) {
    @Suppress("DEPRECATION")
    Column {
        @Suppress("DEPRECATION")
        Text("Motivation & Documents", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = mot, onValueChange = onMot, label = { Text("Motivational Statement") }, modifier = Modifier.fillMaxWidth(), minLines = 4, shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = port, onValueChange = onPort, label = { Text("Portfolio URL (Optional)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.height(24.dp))
        
        Surface(
            onClick = { onCv(!cv) },
            shape = RoundedCornerShape(12.dp),
            color = if (cv) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, if (cv) MaterialTheme.colorScheme.primary else Color.LightGray)
        ) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (cv) Icons.Default.CheckCircle else Icons.Default.UploadFile, null, tint = if (cv) MaterialTheme.colorScheme.primary else Color.Gray)
                Spacer(Modifier.width(12.dp))
                @Suppress("DEPRECATION")
                Text(if (cv) "CV Attached: student_cv.pdf" else "Click to Upload CV (Simulated)", fontWeight = FontWeight.Bold)
            }
        }
    }
}
