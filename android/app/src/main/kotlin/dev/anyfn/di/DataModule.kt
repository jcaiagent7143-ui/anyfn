/**
 * DataModule — provides the database, DAOs, and a shared lenient Json codec.
 *
 * Kept tiny on purpose: each consumer that needs Room or Json depends on its
 * own constructor-injected DAO/Json rather than reaching through a god object.
 */
package dev.anyfn.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.anyfn.data.db.AnyfnDatabase
import dev.anyfn.data.db.AppFunctionDao
import dev.anyfn.data.db.ScanRunDao
import javax.inject.Singleton
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AnyfnDatabase =
        Room.databaseBuilder(context, AnyfnDatabase::class.java, AnyfnDatabase.NAME)
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides
    fun provideAppFunctionDao(db: AnyfnDatabase): AppFunctionDao = db.appFunctionDao()

    @Provides
    fun provideScanRunDao(db: AnyfnDatabase): ScanRunDao = db.scanRunDao()

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
        classDiscriminator = "type"
    }
}
