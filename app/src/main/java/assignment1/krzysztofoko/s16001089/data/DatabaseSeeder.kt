package assignment1.krzysztofoko.s16001089.data

import android.util.Log
import kotlinx.coroutines.flow.first

/**
 * Seeder function for initial database data.
 * Links UserLocal accounts with professional TutorProfiles.
 */
suspend fun seedDatabase(db: AppDatabase) {
    Log.d("DatabaseSeeder", "Seeding database...")
    
    val userDao = db.userDao()
    val courseDao = db.courseDao()
    val assignedDao = db.assignedCourseDao()
    val classroomDao = db.classroomDao()
    
    val allUsers = userDao.getAllUsersFlow().first()
    
    // Define the two main tutors we want to initialize
    val tutorConfigs = listOf(
        Triple("teacher@example.com", "Senior Tutor", "Prof."),
        Triple("teacher2@example.com", "Faculty Lead", "Dr.")
    )

    tutorConfigs.forEach { (email, defaultName, title) ->
        val user = allUsers.find { it.email.lowercase() == email }
        if (user != null) {
            // Update UserLocal with title if it doesn't have one
            if (user.title == null) {
                userDao.upsertUser(user.copy(title = title))
            }

            // 1. Initial Assignment: Only assign all courses if the tutor has NONE yet.
            // This prevents resetting your manual changes every time the app starts.
            val existingAssignments = assignedDao.getAssignedCoursesForTutor(user.id).first()
            
            if (existingAssignments.isEmpty()) {
                val allCourses = courseDao.getAllCourses().first()
                allCourses.forEach { course ->
                    assignedDao.assignCourse(
                        AssignedCourse(tutorId = user.id, courseId = course.id)
                    )
                }
                Log.d("DatabaseSeeder", "Initial courses assigned to ${user.email}")
            }

            // 2. Initialize or Update their professional TutorProfile
            val existingProfile = classroomDao.getTutorProfile(user.id)
            if (existingProfile == null) {
                classroomDao.upsertTutorProfile(
                    TutorProfile(
                        id = user.id,
                        name = user.name,
                        title = title,
                        email = user.email,
                        photoUrl = user.photoUrl,
                        department = "Department of Computing",
                        officeHours = "Mon-Fri, 9AM-5PM",
                        bio = "Professional educator at Wrexham University specializing in modern software development."
                    )
                )
                Log.d("DatabaseSeeder", "Created new professional profile for ${user.email}")
            } else if (existingProfile.title == null) {
                classroomDao.upsertTutorProfile(existingProfile.copy(title = title))
            }
        }
    }
    
    Log.d("DatabaseSeeder", "Database seeding checked.")
}
