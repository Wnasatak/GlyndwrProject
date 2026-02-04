package assignment1.krzysztofoko.s16001089.ui.details.course

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.*
import assignment1.krzysztofoko.s16001089.ui.components.HorizontalWavyBackground
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationsScreen(
    onBack: () -> Unit,
    onNavigateToCourse: (String) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: MyApplicationsViewModel = viewModel(factory = MyApplicationsViewModelFactory(
        userDao = AppDatabase.getDatabase(LocalContext.current).userDao(),
        courseDao = AppDatabase.getDatabase(LocalContext.current).courseDao(),
        bookRepository = BookRepository(AppDatabase.getDatabase(LocalContext.current)),
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    ))
) {
    val applications by viewModel.applications.collectAsState()
    val enrolledPaidCourseTitle by viewModel.enrolledPaidCourseTitle.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalWavyBackground(isDarkTheme = isDarkTheme)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0), // Fix for big gap at top
                    title = { Text(AppConstants.TITLE_MY_APPLICATIONS, fontWeight = FontWeight.Black) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                    actions = { IconButton(onClick = onToggleTheme) { Icon(if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode, null) } },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                )
            }
        ) { padding ->
            if (applications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AssignmentLate, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))
                        Text("No active applications.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(applications) {
                            app ->
                        ApplicationItemCard(app, onNavigateToCourse, enrolledPaidCourseTitle)
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicationItemCard(
    app: ApplicationWithCourse,
    onNavigateToCourse: (String) -> Unit,
    enrolledCourseTitle: String?
) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val isApproved = app.details.status == "APPROVED"
    val isPaidCourse = (app.course?.price ?: 0.0) > 0.0
    val isAlreadyEnrolledInOther = enrolledCourseTitle != null && isPaidCourse && isApproved

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.School, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = app.course?.title ?: "Unknown Course", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text(text = "Submitted: ${sdf.format(Date(app.details.submittedAt))}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }

                StatusBadge(status = app.details.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Academic Review: ${app.details.lastQualification} from ${app.details.institution}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (isAlreadyEnrolledInOther) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(text = "Already Enrolled", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Text(text = "You are currently enrolled in: '$enrolledCourseTitle'. You can only be enrolled in one paid course at a time.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = { onNavigateToCourse(app.details.courseId) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isAlreadyEnrolledInOther
            ) {
                Text(if (isApproved) "Proceed to Enrollment" else "View Course Details")
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, label) = when (status) {
        "PENDING_REVIEW" -> Color(0xFFFBC02D) to "PENDING"
        "APPROVED" -> Color(0xFF4CAF50) to "APPROVED"
        "REJECTED" -> Color(0xFFF44336) to "DECLINED"
        else -> Color.Gray to status
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

data class ApplicationWithCourse(
    val details: CourseEnrollmentDetails,
    val course: Course?
)

class MyApplicationsViewModel(
    private val userDao: UserDao,
    private val courseDao: CourseDao,
    private val bookRepository: BookRepository,
    private val userId: String
) : ViewModel() {
    val applications: StateFlow<List<ApplicationWithCourse>> = flow {
        userDao.getAllEnrollmentsFlow().collect { list ->
            val filtered = list.filter { it.userId == userId }
            val mapped = filtered.map { app ->
                ApplicationWithCourse(app, courseDao.getCourseById(app.courseId))
            }
            emit(mapped)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val enrolledPaidCourseTitle: StateFlow<String?> = combine(
        bookRepository.getAllCombinedData(userId).filterNotNull(),
        userDao.getPurchaseIds(userId)
    ) { allBooks, purchasedIds ->
        purchasedIds.mapNotNull { id ->
            allBooks.find { it.id == id && it.mainCategory == AppConstants.CAT_COURSES && it.price > 0.0 }
        }.firstOrNull()?.title
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

class MyApplicationsViewModelFactory(
    private val userDao: UserDao,
    private val courseDao: CourseDao,
    private val bookRepository: BookRepository,
    private val userId: String
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MyApplicationsViewModel(userDao, courseDao, bookRepository, userId) as T
    }
}
