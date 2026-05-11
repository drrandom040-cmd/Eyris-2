package com.elsewhere.eyris.di

import android.content.Context
import androidx.room.Room
import com.elsewhere.eyris.data.local.AppDatabase
import com.elsewhere.eyris.data.local.dao.ContactedDao
import com.elsewhere.eyris.data.local.dao.LeadDao
import com.elsewhere.eyris.data.remote.scraper.*
import com.elsewhere.eyris.data.repository.LeadRepositoryImpl
import com.elsewhere.eyris.domain.repository.LeadRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "eyris_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    @Provides
    @Singleton
    fun provideLeadDao(db: AppDatabase): LeadDao = db.leadDao

    @Provides
    @Singleton
    fun provideContactedDao(db: AppDatabase): ContactedDao = db.contactedDao

    @Provides
    @Singleton
    fun provideLeadRepository(
        db: AppDatabase,
        googleMapsScraper: GoogleMapsScraper,
        foursquareApi: FoursquareApi,
        osmOverpassApi: OsmOverpassApi,
        geocodingApi: NominatimGeocodingApi
    ): LeadRepository {
        return LeadRepositoryImpl(db, googleMapsScraper, foursquareApi, osmOverpassApi, geocodingApi)
    }
}
