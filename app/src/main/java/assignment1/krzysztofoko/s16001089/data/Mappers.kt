package assignment1.krzysztofoko.s16001089.data

/**
 * Mapper functions to convert specific domain models to the unified Book UI model.
 * Updated to match the current full Book entity constructor.
 */

fun Gear.toBook(orderConf: String? = null): Book {
    return Book(
        id = this.id,
        title = this.title,
        author = this.brand,
        price = this.price,
        isAudioBook = false,
        audioUrl = "",
        pdfUrl = "",
        description = this.description,
        imageUrl = this.imageUrl,
        category = this.category,
        mainCategory = this.mainCategory,
        isInstallmentAvailable = false,
        modulePrice = 0.0
    ).apply {
        orderConfirmation = orderConf
    }
}

fun Course.toBook(orderConf: String? = null): Book {
    return Book(
        id = this.id,
        title = this.title,
        author = this.department,
        price = this.price,
        isAudioBook = false,
        audioUrl = "",
        pdfUrl = "",
        description = this.description,
        imageUrl = this.imageUrl,
        category = this.category,
        mainCategory = this.mainCategory,
        isInstallmentAvailable = this.isInstallmentAvailable,
        modulePrice = this.modulePrice
    ).apply {
        orderConfirmation = orderConf
    }
}

fun AudioBook.toBook(orderConf: String? = null): Book {
    return Book(
        id = this.id,
        title = this.title,
        author = this.author,
        price = this.price,
        isAudioBook = true,
        audioUrl = this.audioUrl,
        pdfUrl = "",
        description = this.description,
        imageUrl = this.imageUrl,
        category = this.category,
        mainCategory = this.mainCategory,
        isInstallmentAvailable = false,
        modulePrice = 0.0
    ).apply {
        orderConfirmation = orderConf
    }
}

fun Book.withOrderConf(orderConf: String?): Book {
    return this.apply { orderConfirmation = orderConf }
}
