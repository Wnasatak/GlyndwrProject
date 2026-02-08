package assignment1.krzysztofoko.s16001089.ui.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import assignment1.krzysztofoko.s16001089.AppConstants
import assignment1.krzysztofoko.s16001089.data.Book
import assignment1.krzysztofoko.s16001089.data.LiveSession
import assignment1.krzysztofoko.s16001089.data.UserLocal
import assignment1.krzysztofoko.s16001089.ui.components.*
import kotlin.math.abs

/**
 * Extracted sections for the Dashboard to keep DashboardScreen.kt clean.
 */

fun LazyGridScope.dashboardHeaderSection(
    user: UserLocal?,
    isTablet: Boolean,
    onTopUp: () -> Unit,
    onViewHistory: () -> Unit
) {
    item(key = "header_section", span = { GridItemSpan(this.maxLineSpan) }) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = if (isTablet) Modifier.widthIn(max = 600.dp) else Modifier.fillMaxWidth()) {
                DashboardHeader(
                    name = user?.name ?: AppConstants.TEXT_STUDENT,
                    photoUrl = user?.photoUrl,
                    role = user?.role ?: "student",
                    balance = user?.balance ?: 0.0,
                    onTopUp = onTopUp,
                    onViewHistory = onViewHistory
                )
            }
        }
    }
}

fun LazyGridScope.applicationsSection(
    show: Boolean,
    isTablet: Boolean,
    onClick: () -> Unit
) {
    if (show) {
        item(key = "applications_section", span = { GridItemSpan(this.maxLineSpan) }) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier
                        .then(if (isTablet) Modifier.widthIn(max = 580.dp) else Modifier.fillMaxWidth())
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClick
                        ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Assignment, null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(AppConstants.TITLE_MY_APPLICATIONS, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Text("Track your enrollment status", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

fun LazyGridScope.enrolledCoursesSection(
    enrolledPaidCourse: Book?,
    enrolledFreeCourses: List<Book>,
    activeLiveSessions: List<LiveSession>,
    isTablet: Boolean,
    onEnterClassroom: (String) -> Unit
) {
    if (enrolledPaidCourse != null || enrolledFreeCourses.isNotEmpty()) {
        item(key = "courses_header", span = { GridItemSpan(this.maxLineSpan) }) { SectionHeader(AppConstants.LABEL_MY_COURSES) }
        
        if (enrolledPaidCourse != null) {
            val isLive = activeLiveSessions.any { it.courseId == enrolledPaidCourse.id }
            item(key = "paid_course_${enrolledPaidCourse.id}", span = { GridItemSpan(this.maxLineSpan) }) { 
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = if (isTablet) Modifier.widthIn(max = 600.dp) else Modifier.fillMaxWidth()) {
                        EnrolledCourseHeader(course = enrolledPaidCourse, isLive = isLive, onEnterClassroom = onEnterClassroom)
                    }
                }
            }
        }
        
        items(enrolledFreeCourses, key = { "free_course_${it.id}" }, span = { GridItemSpan(this.maxLineSpan) }) { freeCourse ->
            val isLive = activeLiveSessions.any { it.courseId == freeCourse.id }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = if (isTablet) Modifier.widthIn(max = 600.dp) else Modifier.fillMaxWidth()) {
                    FreeCourseHeader(course = freeCourse, isLive = isLive, onEnterClassroom = onEnterClassroom)
                }
            }
        }
    }
}

fun LazyGridScope.quickActionsSection(
    isAdmin: Boolean,
    isTutor: Boolean,
    onAdminClick: () -> Unit,
    onTutorClick: () -> Unit
) {
    if (isAdmin) { 
        item(key = "admin_actions", span = { GridItemSpan(this.maxLineSpan) }) { 
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) { AdminQuickActions(onClick = onAdminClick) } 
        } 
    }
    if (isTutor) { 
        item(key = "tutor_actions", span = { GridItemSpan(this.maxLineSpan) }) { 
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) { TutorQuickActions(onClick = onTutorClick) } 
        } 
    }
}

fun LazyGridScope.activityRowsSection(
    lastViewed: List<Book>,
    commented: List<Book>,
    wishlist: List<Book>,
    onBookClick: (Book) -> Unit
) {
    item(key = "reading_header", span = { GridItemSpan(this.maxLineSpan) }) { SectionHeader(AppConstants.TITLE_CONTINUE_READING) }
    item(key = "reading_row", span = { GridItemSpan(this.maxLineSpan) }) { 
        if (lastViewed.isNotEmpty()) GrowingLazyRow(lastViewed, icon = Icons.Default.History, onBookClick = onBookClick)
        else EmptySectionPlaceholder(AppConstants.MSG_NO_RECENT_REVIEWS) 
    }

    item(key = "recent_header", span = { GridItemSpan(this.maxLineSpan) }) { SectionHeader(AppConstants.TITLE_RECENT_ACTIVITY) }
    item(key = "recent_row", span = { GridItemSpan(this.maxLineSpan) }) { 
        if (commented.isNotEmpty()) GrowingLazyRow(commented, icon = Icons.AutoMirrored.Filled.Comment, onBookClick = onBookClick)
        else EmptySectionPlaceholder(AppConstants.MSG_NO_RECENT_REVIEWS) 
    }

    item(key = "liked_header", span = { GridItemSpan(this.maxLineSpan) }) { SectionHeader(AppConstants.TITLE_RECENTLY_LIKED) }
    item(key = "liked_row", span = { GridItemSpan(this.maxLineSpan) }) { 
        if (wishlist.isNotEmpty()) GrowingLazyRow(wishlist, icon = Icons.Default.Favorite, onBookClick = onBookClick)
        else EmptySectionPlaceholder(AppConstants.MSG_FAVORITES_EMPTY) 
    }
}

fun LazyGridScope.collectionControlsSection(
    isTablet: Boolean,
    selectedFilter: String,
    filterOptions: List<String>,
    filterListState: LazyListState,
    infiniteCount: Int,
    onFilterClick: (String) -> Unit,
    onNavigateToClassroom: () -> Unit = {},
    onNavigateToStore: () -> Unit = {}
) {
    item(key = "collection_controls", span = { GridItemSpan(this.maxLineSpan) }) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionHeader(AppConstants.TITLE_YOUR_COLLECTION)
            Spacer(modifier = Modifier.height(16.dp))

            if (isTablet) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(filterOptions) { filter ->
                        val isSelected = selectedFilter == filter
                        CategorySquareButton(
                            label = filter,
                            icon = getFilterIcon(filter),
                            isSelected = isSelected,
                            scale = if (isSelected) 1.25f else 1f,
                            onClick = { onFilterClick(filter) }
                        )
                    }
                }
            } else {
                LazyRow(
                    state = filterListState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(infiniteCount) { index ->
                        val filterIndex = index % filterOptions.size
                        val filter = filterOptions[filterIndex]
                        val scale by remember { derivedStateOf {
                            val layoutInfo = filterListState.layoutInfo
                            val visibleItemsInfo = layoutInfo.visibleItemsInfo
                            val itemInfo = visibleItemsInfo.find { it.index == index }
                            if (itemInfo != null) {
                                val center = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
                                val itemCenter = itemInfo.offset + (itemInfo.size / 2)
                                val dist = abs(center - itemCenter).toFloat()
                                val normDist = (dist / center).coerceIn(0f, 1f)
                                1.25f - (normDist * 0.4f)
                            } else 0.85f
                        } }
                        CategorySquareButton(
                            label = filter,
                            icon = getFilterIcon(filter),
                            isSelected = selectedFilter == filter,
                            scale = scale,
                            onClick = { onFilterClick(filter) }
                        )
                    }
                }
            }
        }
    }
}

fun LazyGridScope.ownedBooksGrid(
    books: List<Book>,
    purchasedIds: Set<String>,
    applicationsMap: Map<String, String>,
    isDarkTheme: Boolean,
    isAudioPlaying: Boolean,
    currentPlayingBookId: String?,
    onBookClick: (Book) -> Unit,
    onPlayAudio: (Book) -> Unit,
    onViewInvoice: (Book) -> Unit,
    onPickupInfo: (Book) -> Unit,
    onRemoveFromLibrary: (Book) -> Unit,
    onEnterClassroom: (String) -> Unit
) {
    if (books.isEmpty()) {
        item(key = "empty_library", span = { GridItemSpan(this.maxLineSpan) }) {
            EmptyLibraryPlaceholder(onBrowse = { /* Handled in screen */ })
        }
    } else {
        items(books, key = { "book_${it.id}" }) { book ->
            var showItemMenu by remember { mutableStateOf(false) }
            val appStatus = applicationsMap[book.id]
            val isPending = appStatus == "PENDING_REVIEW"
            val isApproved = appStatus == "APPROVED"
            val isRejected = appStatus == "REJECTED"
            val isFullyOwned = purchasedIds.contains(book.id)
            val isAlreadyEnrolledInOther = book.mainCategory == AppConstants.CAT_COURSES && book.price > 0.0 && !isFullyOwned && books.any { it.mainCategory == AppConstants.CAT_COURSES && it.price > 0.0 && purchasedIds.contains(it.id) }

            BookItemCard(
                book = book,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                onClick = { onBookClick(book) },
                imageOverlay = {
                    if (book.isAudioBook) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            SpinningAudioButton(isPlaying = isAudioPlaying && currentPlayingBookId == book.id, onToggle = { onPlayAudio(book) }, size = 40)
                        }
                    }
                },
                topEndContent = {
                    if (!isPending && !isAlreadyEnrolledInOther) {
                        Box {
                            IconButton(onClick = { showItemMenu = true }, modifier = Modifier.size(40.dp).padding(4.dp)) {
                                Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(24.dp), tint = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.outline)
                            }
                            DropdownMenu(expanded = showItemMenu, onDismissRequest = { showItemMenu = false }) {
                                if (book.mainCategory == AppConstants.CAT_COURSES && !isRejected) {
                                    DropdownMenuItem(
                                        text = { Text(if (isApproved && !isFullyOwned) "Complete Enrollment" else AppConstants.BTN_ENTER_CLASSROOM) },
                                        onClick = {
                                            showItemMenu = false
                                            if (isApproved && !isFullyOwned) onBookClick(book) else onEnterClassroom(book.id)
                                        },
                                        leadingIcon = { Icon(Icons.Default.School, null) }
                                    )
                                }
                                if (book.price > 0.0 && isFullyOwned) {
                                    DropdownMenuItem(
                                        text = { Text(AppConstants.BTN_VIEW_INVOICE) },
                                        onClick = { showItemMenu = false; onViewInvoice(book) },
                                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, null) }
                                    )
                                }
                                if (book.mainCategory == AppConstants.CAT_GEAR) {
                                    DropdownMenuItem(
                                        text = { Text(AppConstants.BTN_PICKUP_INFO) },
                                        onClick = { showItemMenu = false; onPickupInfo(book) },
                                        leadingIcon = { Icon(Icons.Default.Info, null) }
                                    )
                                }
                                if (book.mainCategory != AppConstants.CAT_GEAR && book.price <= 0.0) {
                                    DropdownMenuItem(
                                        text = { Text(AppConstants.MENU_REMOVE_FROM_LIBRARY, color = MaterialTheme.colorScheme.error) },
                                        onClick = { showItemMenu = false; onRemoveFromLibrary(book) },
                                        leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error) }
                                    )
                                }
                            }
                        }
                    }
                },
                bottomContent = {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        val label = when { isPending -> "REVIEWING"; isAlreadyEnrolledInOther -> "ALREADY ENROLLED"; isApproved && !isFullyOwned -> "APPROVED"; isRejected -> "DECLINED"; else -> AppConstants.getItemStatusLabel(book) }
                        val color = when { isPending -> Color(0xFFFBC02D); isAlreadyEnrolledInOther -> MaterialTheme.colorScheme.secondary; isApproved && !isFullyOwned -> Color(0xFF4CAF50); isRejected -> Color(0xFFF44336); else -> MaterialTheme.colorScheme.primary }
                        Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, color.copy(alpha = 0.3f))) {
                            Text(text = label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            )
        }
    }
}

private fun getFilterIcon(filter: String) = when(filter) {
    AppConstants.FILTER_BOOKS -> Icons.AutoMirrored.Filled.MenuBook
    AppConstants.FILTER_AUDIOBOOKS -> Icons.Default.Headphones
    AppConstants.FILTER_GEAR -> Icons.Default.Checkroom
    AppConstants.FILTER_COURSES -> Icons.Default.School
    else -> Icons.Default.GridView
}
