package assignment1.krzysztofoko.s16001089.data

import android.util.Log

suspend fun seedGearOnly(db: AppDatabase) {
    Log.d("DatabaseSeeder", "Updating Gear catalog with actual assets and professional attributes...")
    
    val sampleGear = listOf(
        Gear(
            title = "Official Hoodie", 
            price = 31.50, 
            originalPrice = 35.00,
            category = "Apparel", 
            description = "Premium navy blue hoodie featuring the university crest. Perfect for campus life.", 
            imageUrl = "file:///android_asset/products/p3-Official Hoodie/glyndwr_hoodie.jpg",
            sizes = "S,M,L,XL,XXL",
            colors = "Navy,Grey,Black",
            stockCount = 50,
            material = "80% Cotton, 20% Polyester",
            sku = "WREX-APP-HD01",
            isFeatured = true,
            productTags = "clothing,hoodie,popular"
        ),
        Gear(
            title = "Pro Tech Backpack", 
            price = 55.00, 
            category = "Accessories", 
            description = "Ergonomically designed laptop bag with dedicated compartments.", 
            imageUrl = "file:///android_asset/products/p4-Pro Tech Backpack/backpack.jpg",
            sizes = "One Size",
            colors = "Black,Deep Blue",
            stockCount = 25,
            material = "Water-resistant Nylon",
            sku = "WREX-ACC-BK02",
            isFeatured = true,
            productTags = "bag,tech,travel"
        ),
        Gear(
            title = "Insulated Water Bottle", 
            price = 15.00, 
            category = "Lifestyle", 
            description = "Durable stainless steel bottle that keeps drinks cold for 24 hours.", 
            imageUrl = "file:///android_asset/products/p5-Insulated Water Bottle/water_bootle.jpg",
            sizes = "500ml,750ml",
            colors = "Silver,Black,Pink",
            stockCount = 100,
            material = "Stainless Steel",
            sku = "WREX-LS-BT03",
            productTags = "eco,drinkware"
        ),
        Gear(
            title = "Deluxe Stationery Set", 
            price = 25.00, 
            category = "Office", 
            description = "Complete set including a professional notebook and engraved pens.", 
            imageUrl = "file:///android_asset/products/p6-Deluxe Stationery Set/Deluxe Stactionery Set.jpg",
            sizes = "A5,A4",
            colors = "Blue,Black",
            stockCount = 40,
            material = "Paper, Metal, Plastic",
            sku = "WREX-OFF-ST04",
            productTags = "study,writing"
        ),
        Gear(
            title = "University Sports Jersey", 
            price = 36.00, 
            originalPrice = 40.00,
            category = "Apparel", 
            description = "High-performance athletic wear designed for Wrexham University teams.", 
            imageUrl = "file:///android_asset/products/p7-University Sports Jersey/Glyndwr_sports_Jersey.webp",
            secondaryImageUrl = "file:///android_asset/products/p7-University Sports Jersey/Glyndwr_sports_Jersey(pink).webp",
            sizes = "S,M,L,XL",
            colors = "White/Navy,Pink",
            stockCount = 30,
            material = "100% Breathable Polyester",
            sku = "WREX-APP-JS05",
            isFeatured = false,
            productTags = "sports,apparel"
        ),
        Gear(
            title = "Freshers Wristband", 
            price = 0.0, 
            category = "Event", 
            description = "Free entry wristband for the orientation events during the first week of term.", 
            imageUrl = "file:///android_asset/products/p1-Freshers Wristband/freshers_wristband.png",
            sizes = "One Size",
            colors = "Default", // Excluded color selection
            stockCount = 500,
            material = "Silicone",
            sku = "WREX-EVE-WB06",
            productTags = "freshers,event,free"
        ),
        Gear(
            title = "Campus Map Kit", 
            price = 0.0, 
            category = "General", 
            description = "A high-quality physical map kit and pocket guide for navigating the university buildings.", 
            imageUrl = "file:///android_asset/products/p2-Campus Map Kit/glyndwr_map.jpg",
            sizes = "Default", // Excluded size selection
            colors = "Default", // Excluded color selection
            stockCount = 200,
            material = "Laminated Paper",
            sku = "WREX-GEN-MP07",
            productTags = "map,orientation,free"
        )
    ).map { it.copy(id = it.title.lowercase().replace(" ", "_")) }

    db.gearDao().insertAll(sampleGear)
    Log.d("DatabaseSeeder", "Gear update with actual images finished!")
}

suspend fun seedDatabase(db: AppDatabase) { 
}
