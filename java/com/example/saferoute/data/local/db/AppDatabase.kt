package com.example.saferoute.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.saferoute.data.local.db.dao.CachedRouteDao
import com.example.saferoute.data.local.db.dao.EmergencyEventDao
import com.example.saferoute.data.local.db.dao.EmergencySessionDao
import com.example.saferoute.data.local.db.dao.EvidenceDao
import com.example.saferoute.data.local.db.dao.HazardReportDao
import com.example.saferoute.data.local.db.dao.JourneyDao
import com.example.saferoute.data.local.db.dao.LocationDao
import com.example.saferoute.data.local.db.dao.SafeHavenDao
import com.example.saferoute.data.local.db.dao.SafetyReportDao
import com.example.saferoute.data.local.db.dao.TrustedContactDao
import com.example.saferoute.data.local.db.dao.UserDao
import com.example.saferoute.data.local.db.entity.CachedRouteEntity
import com.example.saferoute.data.local.db.entity.EmergencyEventEntity
import com.example.saferoute.data.local.db.entity.EmergencySessionEntity
import com.example.saferoute.data.local.db.entity.EvidenceEntity
import com.example.saferoute.data.local.db.entity.HazardReportEntity
import com.example.saferoute.data.local.db.entity.JourneyEntity
import com.example.saferoute.data.local.db.entity.LocationEntity
import com.example.saferoute.data.local.db.entity.SafeHavenEntity
import com.example.saferoute.data.local.db.entity.SafetyReportEntity
import com.example.saferoute.data.local.db.entity.TrustedContactEntity
import com.example.saferoute.data.local.db.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        TrustedContactEntity::class,
        JourneyEntity::class,
        SafetyReportEntity::class,
        HazardReportEntity::class,
        EmergencySessionEntity::class,
        EmergencyEventEntity::class,
        CachedRouteEntity::class,
        EvidenceEntity::class,
        LocationEntity::class,
        SafeHavenEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun trustedContactDao(): TrustedContactDao
    abstract fun journeyDao(): JourneyDao
    abstract fun safetyReportDao(): SafetyReportDao
    abstract fun hazardReportDao(): HazardReportDao
    abstract fun emergencySessionDao(): EmergencySessionDao
    abstract fun emergencyEventDao(): EmergencyEventDao
    abstract fun cachedRouteDao(): CachedRouteDao
    abstract fun evidenceDao(): EvidenceDao
    abstract fun locationDao(): LocationDao
    abstract fun safeHavenDao(): SafeHavenDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "saferoute_db"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // Seed Default User Profile
            db.userDao().insertOrUpdate(
                UserEntity(
                    id = 1,
                    userId = "usr_sr_01",
                    name = "Priya Sharma",
                    phoneNumber = "+15550192831",
                    email = "priya.sharma@saferoute.app",
                    emergencyMessage = "SAFE ROUTE EMERGENCY ALERT: I may be in danger. Please track my location immediately.",
                    isGuest = false
                )
            )

            // Seed Default Trusted Contacts
            db.trustedContactDao().insertContacts(
                listOf(
                    TrustedContactEntity(
                        name = "Mom (Sunita)",
                        phoneNumber = "+15550192831",
                        relationship = "Mother",
                        isPrimary = true,
                        isEnabled = true
                    ),
                    TrustedContactEntity(
                        name = "Rajesh (Father)",
                        phoneNumber = "+15550192832",
                        relationship = "Father",
                        isPrimary = false,
                        isEnabled = true
                    ),
                    TrustedContactEntity(
                        name = "Ananya (Friend)",
                        phoneNumber = "+15550192833",
                        relationship = "Friend",
                        isPrimary = false,
                        isEnabled = true
                    ),
                    TrustedContactEntity(
                        name = "Campus Security Helpline",
                        phoneNumber = "+15550199911",
                        relationship = "Guardian / Security",
                        isPrimary = false,
                        isEnabled = true
                    )
                )
            )

            // Seed Initial Safe Havens (Police, Hospitals, 24/7 Stores, Pharmacies)
            db.safeHavenDao().insertSafeHavens(
                listOf(
                    SafeHavenEntity(
                        name = "Central Metro Police Station",
                        type = "Police Station",
                        address = "450 Safety Blvd, Downtown",
                        latitude = 37.7749,
                        longitude = -122.4194,
                        isVerified = true,
                        isOpen24_7 = true,
                        phoneNumber = "911",
                        distanceMeters = 320.0
                    ),
                    SafeHavenEntity(
                        name = "St. Jude Community Hospital",
                        type = "Hospital",
                        address = "780 Care Ave, Downtown",
                        latitude = 37.7780,
                        longitude = -122.4150,
                        isVerified = true,
                        isOpen24_7 = true,
                        phoneNumber = "+15550100010",
                        distanceMeters = 540.0
                    ),
                    SafeHavenEntity(
                        name = "24/7 City Express Mart",
                        type = "24/7 Store",
                        address = "102 Main Street",
                        latitude = 37.7720,
                        longitude = -122.4210,
                        isVerified = true,
                        isOpen24_7 = true,
                        phoneNumber = "+15550123456",
                        distanceMeters = 180.0
                    ),
                    SafeHavenEntity(
                        name = "Downtown All-Night Pharmacy",
                        type = "Pharmacy",
                        address = "215 Health Crescent",
                        latitude = 37.7738,
                        longitude = -122.4175,
                        isVerified = true,
                        isOpen24_7 = true,
                        phoneNumber = "+15550129988",
                        distanceMeters = 290.0
                    ),
                    SafeHavenEntity(
                        name = "University Safe Refuge Spot",
                        type = "Safe Zone",
                        address = "Building B, University Plaza",
                        latitude = 37.7765,
                        longitude = -122.4180,
                        isVerified = true,
                        isOpen24_7 = true,
                        phoneNumber = "+15550199911",
                        distanceMeters = 420.0
                    )
                )
            )

            // Seed Initial Safety Reports (Community Hazards & Safe Zones)
            db.safetyReportDao().insertReports(
                listOf(
                    SafetyReportEntity(
                        reportId = "rep_01",
                        type = "Broken Street Light",
                        description = "Streetlights non-functional for 200m stretch near 4th St corner.",
                        latitude = 37.7735,
                        longitude = -122.4205,
                        address = "4th Street & Elm",
                        severity = 2,
                        isSynced = true
                    ),
                    SafetyReportEntity(
                        reportId = "rep_02",
                        type = "Unsafe Area",
                        description = "Isolated construction detour with low foot traffic after 8 PM.",
                        latitude = 37.7755,
                        longitude = -122.4170,
                        address = "Oak Street Tunnel Walkway",
                        severity = 3,
                        isSynced = true
                    ),
                    SafetyReportEntity(
                        reportId = "rep_03",
                        type = "Police Checkpoint",
                        description = "Active police booth and CCTV surveillance active 24/7.",
                        latitude = 37.7742,
                        longitude = -122.4190,
                        address = "Market Square Junction",
                        severity = 1,
                        isSynced = true
                    ),
                    SafetyReportEntity(
                        reportId = "rep_04",
                        type = "Safe Zone",
                        description = "Well-lit transit hub with 24/7 security staff present.",
                        latitude = 37.7760,
                        longitude = -122.4182,
                        address = "Downtown Transit Pavilion",
                        severity = 1,
                        isSynced = true
                    )
                )
            )

            // Also keep HazardReportEntity seeded for backward compatibility with existing views
            db.hazardReportDao().insertHazards(
                listOf(
                    HazardReportEntity(
                        title = "Unlit Walkway",
                        category = "Broken Lighting",
                        description = "Streetlight flickering and dark alley near 4th St corner.",
                        latitude = 37.7735,
                        longitude = -122.4205,
                        severity = 2,
                        isSynced = true
                    ),
                    HazardReportEntity(
                        title = "Sidewalk Construction",
                        category = "Road Block",
                        description = "Blocked pedestrian lane forcing walking onto street.",
                        latitude = 37.7755,
                        longitude = -122.4170,
                        severity = 1,
                        isSynced = true
                    )
                )
            )

            // Seed Default Sample Routes
            db.cachedRouteDao().insertRoutes(
                listOf(
                    CachedRouteEntity(
                        originName = "Current Location (Downtown)",
                        destinationName = "Grand Central Station",
                        originLat = 37.7749,
                        originLng = -122.4194,
                        destLat = 37.7833,
                        destLng = -122.4167,
                        distanceKm = 1.4,
                        estimatedMinutes = 18,
                        safetyScore = 9.2,
                        lightingLevel = "HIGH",
                        openFacilitiesCount = 5,
                        riskRating = "LOW",
                        isRecommended = true,
                        routeWaypointsJson = ""
                    ),
                    CachedRouteEntity(
                        originName = "Current Location (Downtown)",
                        destinationName = "Grand Central Station",
                        originLat = 37.7749,
                        originLng = -122.4194,
                        destLat = 37.7833,
                        destLng = -122.4167,
                        distanceKm = 1.1,
                        estimatedMinutes = 14,
                        safetyScore = 6.4,
                        lightingLevel = "MEDIUM",
                        openFacilitiesCount = 1,
                        riskRating = "MODERATE",
                        isRecommended = false,
                        routeWaypointsJson = ""
                    ),
                    CachedRouteEntity(
                        originName = "Current Location (Downtown)",
                        destinationName = "Grand Central Station",
                        originLat = 37.7749,
                        originLng = -122.4194,
                        destLat = 37.7833,
                        destLng = -122.4167,
                        distanceKm = 0.9,
                        estimatedMinutes = 11,
                        safetyScore = 4.1,
                        lightingLevel = "LOW",
                        openFacilitiesCount = 0,
                        riskRating = "HIGH",
                        isRecommended = false,
                        routeWaypointsJson = ""
                    )
                )
            )

            // Seed a past sample journey in history
            db.journeyDao().insertJourney(
                JourneyEntity(
                    journeyId = "jrn_sample_01",
                    startLatitude = 37.7749,
                    startLongitude = -122.4194,
                    destinationName = "University Library",
                    destinationLatitude = 37.7765,
                    destinationLongitude = -122.4180,
                    chosenRouteType = "SAFEST",
                    safetyScore = 9.1,
                    distanceMeters = 850.0,
                    durationMinutes = 12,
                    startTime = System.currentTimeMillis() - 86400000L,
                    endTime = System.currentTimeMillis() - 85680000L,
                    status = "COMPLETED",
                    deviationsCount = 0,
                    unexpectedStopsCount = 0,
                    isSynced = true
                )
            )
        }
    }
}
