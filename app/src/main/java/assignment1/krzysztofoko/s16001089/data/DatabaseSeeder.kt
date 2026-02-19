/**
 * DatabaseSeeder.kt
 *
 * This file contains the seedDatabase function, responsible for populating the Room database
 * with initial development and testing data. It establishes relationships between users,
 * courses, modules, assignments, and attendance records.
 */

package assignment1.krzysztofoko.s16001089.data

import android.util.Log
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.UUID

/**
 * Seeder function for initial database data.
 * Establishing a clean, minimal hierarchy: Course -> Modules -> Assignments -> Grades -> Attendance
 *
 * @param db The AppDatabase instance to perform operations on.
 */
suspend fun seedDatabase(db: AppDatabase) {
    Log.d("DatabaseSeeder", "Checking database state...") // Log start of seeding process
    
    // Initialize DAOs for various entities
    val userDao = db.userDao()
    val courseDao = db.courseDao()
    val assignedDao = db.assignedCourseDao()
    val classroomDao = db.classroomDao()
    
    // Fetch existing users and courses to determine if seeding is necessary
    val allUsers = userDao.getAllUsersFlow().first()
    val allCourses = courseDao.getAllCourses().first()
    
    // Abort if the core tables are empty, as seeding relies on existing users/courses from assets
    if (allUsers.isEmpty() || allCourses.isEmpty()) return

    // --- 1. Initialize Tutors and Course Assignments ---
    
    // Define initial tutor configurations (Email, Description, Academic Title)
    val tutorConfigs = listOf(
        Triple("teacher@example.com", "Senior Tutor", "Prof."),
        Triple("teacher2@example.com", "Faculty Lead", "Dr.")
    )

    tutorConfigs.forEach { (email, _, title) ->
        val user = allUsers.find { it.email.lowercase() == email }
        if (user != null) {
            // Update user title if missing
            if (user.title == null) userDao.upsertUser(user.copy(title = title))
            
            // Check if the tutor already has assigned courses
            val existingAssignments = assignedDao.getAssignedCoursesForTutor(user.id).first()
            if (existingAssignments.isEmpty()) {
                // Assign every course in the database to these tutors for testing purposes
                allCourses.forEach { course ->
                    assignedDao.assignCourse(AssignedCourse(tutorId = user.id, courseId = course.id))
                }
            }
        }
    }

    // --- 2. Hierarchy Seeding (Modules, Assignments, Grades, Attendance) ---
    
    val firstCourse = allCourses.first()
    // Check if classroom modules already exist for the first course
    val existingModules = classroomDao.getModulesForCourse(firstCourse.id).first()
    
    if (existingModules.isEmpty()) {
        Log.d("DatabaseSeeder", "Performing fresh minimal seed...")
        
        // Clear existing classroom-related data to ensure a clean state
        classroomDao.deleteAllGrades()
        classroomDao.deleteAllAssignments()
        classroomDao.deleteAllModules()
        classroomDao.deleteAllAttendance()

        // Filter for users with the 'student' role
        val students = allUsers.filter { it.role == "student" }
        
        // Process the first three courses for the seed data
        allCourses.take(3).forEach { course ->
            // Create a foundational module for the course
            val moduleId = "mod_1_${course.id}"
            val module = ModuleContent(
                id = moduleId, 
                courseId = course.id, 
                title = "Module 1: Core Principles", 
                description = "Essential concepts of ${course.title}.", 
                contentType = "VIDEO", 
                contentUrl = "https://example.com/vid1", 
                order = 1
            )
            classroomDao.insertModules(listOf(module))

            // Create a final assignment linked to the module
            val assignmentId = "asgn_${module.id}"
            val assignment = Assignment(
                id = assignmentId,
                courseId = course.id,
                moduleId = module.id,
                title = "Final Coursework",
                description = "Primary assessment for ${course.title}.",
                dueDate = System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 14) // Due in 14 days
            )
            classroomDao.insertAssignments(listOf(assignment))

            // Attendance Simulation: Calculate timestamps for Today and Yesterday (at midnight)
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val today = cal.timeInMillis
            
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = cal.timeInMillis

            students.forEachIndexed { index, student ->
                // Ensure student is 'enrolled' in the course with an approved status
                userDao.addEnrollmentDetails(
                    CourseEnrollmentDetails(
                        id = "enroll_${student.id}_${course.id}",
                        userId = student.id,
                        courseId = course.id,
                        lastQualification = "Diploma",
                        institution = "Wrexham Local",
                        graduationYear = "2023",
                        englishProficiencyLevel = "Fluent",
                        dateOfBirth = "2001-01-01",
                        nationality = "British",
                        gender = "Not specified",
                        emergencyContactName = "Guardian",
                        emergencyContactPhone = "077",
                        motivationalText = "Simulation data.",
                        status = "APPROVED"
                    )
                )

                // Generate a random grade for the assignment
                classroomDao.upsertGrade(
                    Grade(
                        id = "grade_${student.id}_${assignment.id}",
                        userId = student.id,
                        courseId = course.id,
                        assignmentId = assignment.id,
                        score = (70..98).random().toDouble(),
                        feedback = "Well done.",
                        gradedAt = System.currentTimeMillis()
                    )
                )

                // Seed simulated attendance for Yesterday and Today based on student index
                classroomDao.upsertAttendance(Attendance(student.id, course.id, today, index % 2 == 0))
                classroomDao.upsertAttendance(Attendance(student.id, course.id, yesterday, index % 3 != 0))
            }
        }
        Log.d("DatabaseSeeder", "Seeding with attendance finished.")
    }
}
