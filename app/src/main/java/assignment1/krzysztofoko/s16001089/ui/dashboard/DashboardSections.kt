package assignment1.krzysztofoko.s16001089.ui.dashboard
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
 * DashboardSections.kt
 *
 * This file serves as a modular construction kit for the main Dashboard screen.
 * It contains a series of extension functions for [LazyGridScope], allowing the 
 * primary dashboard grid to be built from distinct, manageable segments. 
 * 
 * This approach keeps the main screen file clean and makes it much easier to 
 * rearrange or toggle sections based on the user's role or account state.
 * All sections are designed to be adaptive, respecting both mobile and tablet layouts.
 */

/**
 * Renders the top-most header of the dashboard.
 * Displays the user's profile card, role badge, and current wallet balance.
 */
fun LazyGridScope.dashboardHeaderSection(
    user: UserLocal?,
    isTablet: Boolean,
    onProfileClick: () -> Unit,
    onTopUp: () -> Unit,
    onViewHistory: () -> Unit
) {
    // The span is set to maxLineSpan so the header always takes up the full width.
    item(key = "header_section", span = { GridItemSpan(this.maxLineSpan) }) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
            // Constrain width on tablets to prevent the header from looking stretched.
            Box(modifier = if (isTablet) Modifier.widthIn(max = AdaptiveWidths.Wide) else Modifier.fillMaxWidth()) {
                DashboardHeader(
                    name = user?.name ?: AppConstants.TEXT_STUDENT,
                    photoUrl = user?.photoUrl,
                    role = user?.role ?: "student",
                    balance = user?.balance ?: 0.0,
                    onProfileClick = onProfileClick,
                    onTopUp = onTopUp,
                    onViewHistory = onViewHistory
                )
            }
        }
    }
}

/**
 * Displays a specialised card linking to the "My Applications" screen.
 * Only visible if the student has active or pending course applications.
 */
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
                        .then(if (isTablet) Modifier.widthIn(max = AdaptiveWidths.Wide) else Modifier.fillMaxWidth())
                        .padding(horizontal = AdaptiveSpacing.contentPadding(), vertical = AdaptiveSpacing.small())
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null, // Custom interaction handling
                            onClick = onClick
                        ),
                    // Use the secondary container colour to distinguish academic status from general store items.
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(AdaptiveSpacing.cornerRadius())
                ) {
                    Row(modifier = Modifier.padding(AdaptiveSpacing.medium()), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.Assignment, null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(AdaptiveSpacing.small()))
                        Column {
                            @Suppress("DEPRECATION")
                            Text(AppConstants.TITLE_MY_APPLICATIONS, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, style = AdaptiveTypography.sectionHeader())
                            Text("Track your enrolment status", style = AdaptiveTypography.caption(), color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}

/**
 * Organises and renders headers for active courses the user is currently in.
 * Prioritises paid courses over free ones and integrates live session badges.
 */
fun LazyGridScope.enrolledCoursesSection(
    enrolledPaidCourse: Book?,
    enrolledFreeCourses: List<Book>,
    activeLiveSessions: List<LiveSession>,
    isTablet: Boolean,
    onEnterClassroom: (String) -> Unit
) {
    // Only render this entire segment if the user is in at least one course.
    if (enrolledPaidCourse != null || enrolledFreeCourses.isNotEmpty()) {
        item(key = "courses_header", span = { GridItemSpan(this.maxLineSpan) }) { 
            SectionHeader(AppConstants.LABEL_MY_COURSES) 
        }

        // Handle the primary paid course if it exists.
        if (enrolledPaidCourse != null) {
            val isLive = activeLiveSessions.any { it.courseId == enrolledPaidCourse.id }
            item(key = "paid_course_${enrolledPaidCourse.id}", span = { GridItemSpan(this.maxLineSpan) }) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = if (isTablet) Modifier.widthIn(max = AdaptiveWidths.Wide) else Modifier.fillMaxWidth()) {
                        EnrolledCourseHeader(course = enrolledPaidCourse, isLive = isLive, onEnterClassroom = onEnterClassroom)
                    }
                }
            }
        }

        // Iterate through any enrolled free courses.
        items(enrolledFreeCourses, key = { "free_course_${it.id}" }, span = { GridItemSpan(this.maxLineSpan) }) { freeCourse ->
            val isLive = activeLiveSessions.any { it.courseId == freeCourse.id }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = if (isTablet) Modifier.widthIn(max = AdaptiveWidths.Wide) else Modifier.fillMaxWidth()) {
                    FreeCourseHeader(course = freeCourse, isLive = isLive, onEnterClassroom = onEnterClassroom)
                }
            }
        }
    }
}

/**
 * Shortcuts for staff-level functionality.
 * Only renders for users with 'Admin' or 'Tutor' privileges.
 */
fun LazyGridScope.quickActionsSection(
    isAdmin: Boolean,
    isTutor: Boolean,
    onAdminClick: () -> Unit,
    onTutorClick: () -> Unit
) {
    if (isAdmin) {
        item(key = "admin_actions", span = { GridItemSpan(this.maxLineSpan) }) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) { 
                AdminQuickActions(onClick = onAdminClick) 
            }
        }
    }
    if (isTutor) {
        item(key = "tutor_actions", span = { GridItemSpan(this.maxLineSpan) }) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) { 
                TutorQuickActions(onClick = onTutorClick) 
            }
        }
    }
}

/**
 * Renders interactive horizontal rows for browsing history, recent reviews, and the wishlist.
 */
fun LazyGridScope.activityRowsSection(
    lastViewed: List<Book>,
    commented: List<Book>,
    wishlist: List<Book>,
    onBookClick: (Book) -> Unit
) {
    // 1. Browsing History
    item(key = "reading_header", span = { GridItemSpan(this.maxLineSpan) }) { SectionHeader(AppConstants.TITLE_CONTINUE_READING) }
    item(key = "reading_row", span = { GridItemSpan(this.maxLineSpan) }) {
        if (lastViewed.isNotEmpty()) GrowingLazyRow(lastViewed, icon = Icons.Default.History, onBookClick = onBookClick)
        else EmptySectionPlaceholder(AppConstants.MSG_NO_RECENTLY_VIEWED)
    }

    // 2. Social Activity (Items the user has reviewed)
    item(key = "recent_header", span = { GridItemSpan(this.maxLineSpan) }) { SectionHeader(AppConstants.TITLE_RECENT_ACTIVITY) }
    item(key = "recent_row", span = { GridItemSpan(this.maxLineSpan) }) {
        if (commented.isNotEmpty()) GrowingLazyRow(commented, icon = Icons.AutoMirrored.Filled.Comment, onBookClick = onBookClick)
        else EmptySectionPlaceholder(AppConstants.MSG_NO_RECENT_REVIEWS)
    }

    // 3. User Wishlist
    item(key = "liked_header", span = { GridItemSpan(this.maxLineSpan) }) { SectionHeader(AppConstants.TITLE_RECENTLY_LIKED) }
    item(key = "liked_row", span = { GridItemSpan(this.maxLineSpan) }) {
        if (wishlist.isNotEmpty()) GrowingLazyRow(wishlist, icon = Icons.Default.Favorite, onBookClick = onBookClick)
        else EmptySectionPlaceholder(AppConstants.MSG_FAVORITES_EMPTY)
    }
}

/**
 * The filtering toolbar for the user's personal collection.
 * Features an interactive "Cover Flow" selection effect on mobile devices.
 */
fun LazyGridScope.collectionControlsSection(
    isTablet: Boolean,
    selectedFilter: String,
    filterOptions: List<String>,
    filterListState: LazyListState,
    infiniteCount: Int,
    onFilterClick: (String) -> Unit
) {
    item(key = "collection_controls", span = { GridItemSpan(this.maxLineSpan) }) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionHeader(AppConstants.TITLE_YOUR_COLLECTION)
            Spacer(modifier = Modifier.height(AdaptiveSpacing.small()))

            if (isTablet) {
                // Simplified, centred row for tablets where space is abundant.
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = AdaptiveSpacing.medium(), vertical = AdaptiveSpacing.small()),
                    horizontalArrangement = Arrangement.spacedBy(AdaptiveSpacing.medium(), Alignment.CenterHorizontally),
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
                // Sophisticated "Cover Flow" implementation for mobile devices.
                LazyRow(
                    state = filterListState,
                    contentPadding = PaddingValues(horizontal = AdaptiveSpacing.contentPadding(), vertical = AdaptiveSpacing.small()),
                    horizontalArrangement = Arrangement.spacedBy(AdaptiveSpacing.small()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(infiniteCount) { index ->
                        val filterIndex = index % filterOptions.size
                        val filter = filterOptions[filterIndex]
                        
                        // Dynamically calculate scaling based on the item's distance from the screen centre.
                        val scale by remember { derivedStateOf {
                            val layoutInfo = filterListState.layoutInfo
                            val visibleItemsInfo = layoutInfo.visibleItemsInfo
                            val itemInfo = visibleItemsInfo.find { it.index == index }
                            if (itemInfo != null) {
                                val center = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
                                val itemCenter = itemInfo.offset + (itemInfo.size / 2)
                                val dist = abs(center - itemCenter).toFloat()
                                val normDist = (dist / center).coerceIn(0f, 1f)
                                // Central item is scaled up to 1.25x for focus.
                                1.25f - (normDist * 0.4f)
                            } else 0.85f // Items off-screen are smaller.
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

/**
 * The main grid of items owned by the user.
 * Each item features a context-aware menu for common actions like viewing invoices 
 * or entering classrooms.
 */
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
        // Show empty-state guidance if the user has no matching items.
        item(key = "empty_library", span = { GridItemSpan(this.maxLineSpan) }) {
            EmptyLibraryPlaceholder(onBrowse = { /* Navigation handled in parent screen */ })
        }
    } else {
        // Grid items for owned books, courses, and merchandise.
        items(books, key = { "book_${it.id}" }) { book ->
            var showItemMenu by remember { mutableStateOf(false) }
            val appStatus = applicationsMap[book.id]
            
            // --- ITEM STATUS LOGIC --- //
            val isPending = appStatus == "PENDING_REVIEW"
            val isApproved = appStatus == "APPROVED"
            val isRejected = appStatus == "REJECTED"
            val isFullyOwned = purchasedIds.contains(book.id)
            // Constraint check: cannot finalise enrolment if in another paid course.
            val isAlreadyEnrolledInOther = book.mainCategory == AppConstants.CAT_COURSES && book.price > 0.0 && !isFullyOwned && books.any { it.mainCategory == AppConstants.CAT_COURSES && it.price > 0.0 && purchasedIds.contains(it.id) }

            BookItemCard(
                book = book,
                modifier = Modifier.padding(horizontal = AdaptiveSpacing.contentPadding(), vertical = AdaptiveSpacing.small()),
                onClick = { onBookClick(book) },
                imageOverlay = {
                    // Playback overlay for owned audiobooks.
                    if (book.isAudioBook) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            SpinningAudioButton(isPlaying = isAudioPlaying && currentPlayingBookId == book.id, onToggle = { onPlayAudio(book) }, size = 40)
                        }
                    }
                },
                topEndContent = {
                    // Context Menu Trigger.
                    if (!isPending && !isAlreadyEnrolledInOther) {
                        Box {
                            IconButton(onClick = { showItemMenu = true }, modifier = Modifier.size(40.dp).padding(4.dp)) {
                                Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(24.dp), tint = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.outline)
                            }
                            DropdownMenu(expanded = showItemMenu, onDismissRequest = { showItemMenu = false }) {
                                // Dynamic menu items based on product type and ownership state.
                                if (book.mainCategory == AppConstants.CAT_COURSES && !isRejected) {
                                    DropdownMenuItem(
                                        text = { Text(if (isApproved && !isFullyOwned) "Complete Enrolment" else AppConstants.BTN_ENTER_CLASSROOM) },
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
                                if (book.mainCategory == AppConstants.CAT_GEAR || (book.mainCategory == AppConstants.CAT_BOOKS && book.price <= 0.0)) {
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
                    // The status label row provides immediate feedback on the state of the item.
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        val label = when {
                            isPending -> "REVIEWING"
                            isAlreadyEnrolledInOther -> "ALREADY ENROLLED"
                            isApproved && !isFullyOwned -> "APPROVED"
                            isRejected -> "DECLINED"
                            book.mainCategory == AppConstants.CAT_BOOKS && book.price <= 0.0 -> AppConstants.LABEL_PICKED_UP
                            else -> AppConstants.getItemStatusLabel(book)
                        }
                        // Status mapping to semantic colours.
                        val color = when { 
                            isPending -> Color(0xFFFBC02D) 
                            isAlreadyEnrolledInOther -> MaterialTheme.colorScheme.secondary 
                            isApproved && !isFullyOwned -> Color(0xFF4CAF50) 
                            isRejected -> Color(0xFFF44336) 
                            else -> MaterialTheme.colorScheme.primary 
                        }
                        Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, color.copy(alpha = 0.3f))) {
                            Text(text = label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    }
}

/**
 * Utility: Maps a filter option name to a specific Icon.
 */
private fun getFilterIcon(filter: String) = when (filter) {
    AppConstants.FILTER_BOOKS -> Icons.AutoMirrored.Filled.MenuBook
    AppConstants.FILTER_AUDIOBOOKS -> Icons.Default.Headphones
    AppConstants.FILTER_GEAR -> Icons.Default.Checkroom
    AppConstants.FILTER_COURSES -> Icons.Default.School
    else -> Icons.Default.GridView
}
