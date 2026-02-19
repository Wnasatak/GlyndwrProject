/**
 * Mappers.kt
 *
 * This file contains extension functions used to map specialized domain entities
 * (Gear, Course, AudioBook) into a unified 'Book' model. This allows the UI layer
 * to handle diverse product types using a single, consistent data structure.
 */

package assignment1.krzysztofoko.s16001089.data

/**
 * Maps a Gear entity to the unified Book model.
 * 
 * @param orderConf Optional purchase confirmation code.
 * @return A Book object populated with gear metadata.
 */
fun Gear.toBook(orderConf: String? = null): Book {
    return Book(
        id = this.id, // Transfer unique identifier
        title = this.title, // Transfer product title
        author = this.brand, // Brand is used as 'author' in the unified model
        price = this.price, // Selling price
        isAudioBook = false, // Gear is a physical item, not an audiobook
        audioUrl = "", // No audio content
        pdfUrl = "", // No PDF content
        description = this.description, // Detailed gear description
        imageUrl = this.imageUrl, // URL for the gear image
        category = this.category, // Sub-category (e.g., Clothing)
        mainCategory = this.mainCategory, // Top-level category (e.g., University Gear)
        isInstallmentAvailable = false, // Installments typically not available for gear
        modulePrice = 0.0 // No module pricing
    ).apply {
        orderConfirmation = orderConf // Attach order confirmation status
    }
}

/**
 * Maps a Course entity to the unified Book model.
 * 
 * @param orderConf Optional purchase confirmation code.
 * @return A Book object populated with course metadata.
 */
fun Course.toBook(orderConf: String? = null): Book {
    return Book(
        id = this.id, // Transfer course identifier
        title = this.title, // Transfer course title
        author = this.department, // Department is used as 'author' in the unified model
        price = this.price, // Total tuition/course fee
        isAudioBook = false, // Educational courses are separate from audiobooks
        audioUrl = "", // No primary audio stream URL here
        pdfUrl = "", // No primary PDF URL here
        description = this.description, // Detailed syllabus/overview
        imageUrl = this.imageUrl, // Course promotional image
        category = this.category, // Subject area (e.g., Computing)
        mainCategory = this.mainCategory, // 'University Courses'
        isInstallmentAvailable = this.isInstallmentAvailable, // Respect course installment setting
        modulePrice = this.modulePrice // Set price per module
    ).apply {
        orderConfirmation = orderConf // Attach enrollment confirmation status
    }
}

/**
 * Maps an AudioBook entity to the unified Book model.
 * 
 * @param orderConf Optional purchase confirmation code.
 * @return A Book object populated with audiobook metadata.
 */
fun AudioBook.toBook(orderConf: String? = null): Book {
    return Book(
        id = this.id, // Transfer audiobook identifier
        title = this.title, // Transfer title
        author = this.author, // Narrator or author name
        price = this.price, // Digital purchase price
        isAudioBook = true, // Flag as an audiobook for UI icons/logic
        audioUrl = this.audioUrl, // URL for the audio stream
        pdfUrl = "", // No PDF associated
        description = this.description, // Book summary
        imageUrl = this.imageUrl, // Cover art URL
        category = this.category, // Sub-genre
        mainCategory = this.mainCategory, // 'Audio Books'
        isInstallmentAvailable = false, // Digital books usually paid in full
        modulePrice = 0.0 // No module pricing
    ).apply {
        orderConfirmation = orderConf // Attach purchase confirmation status
    }
}

/**
 * Helper extension to attach an order confirmation string to an existing Book instance.
 * 
 * @param orderConf The confirmation ID or status.
 * @return The same Book instance with the updated orderConfirmation field.
 */
fun Book.withOrderConf(orderConf: String?): Book {
    return this.apply { orderConfirmation = orderConf }
}
