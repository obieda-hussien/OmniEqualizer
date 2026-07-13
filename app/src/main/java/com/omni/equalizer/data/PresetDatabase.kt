package com.omni.equalizer.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "presets")
data class EqualizerPreset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    // 10 bands gains in dB (typically from -15.0 to +15.0)
    val gain31: Float = 0f,
    val gain62: Float = 0f,
    val gain125: Float = 0f,
    val gain250: Float = 0f,
    val gain500: Float = 0f,
    val gain1k: Float = 0f,
    val gain2k: Float = 0f,
    val gain4k: Float = 0f,
    val gain8k: Float = 0f,
    val gain16k: Float = 0f,
    // FX controls
    val bassBoost: Float = 40f,
    val bassBoostEnabled: Boolean = false,
    val loudness: Float = 3f,
    val loudnessEnabled: Boolean = false,
    val virtualizer: Float = 48f,
    val virtualizerEnabled: Boolean = true,
    val isCustom: Boolean = true,
    val tags: String = "EQ"
)

@Dao
interface PresetDao {
    @Query("SELECT * FROM presets ORDER BY isCustom ASC, name ASC")
    fun getAllPresets(): Flow<List<EqualizerPreset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: EqualizerPreset)

    @Query("DELETE FROM presets WHERE id = :id")
    suspend fun deletePresetById(id: Int)

    @Query("SELECT * FROM presets WHERE id = :id LIMIT 1")
    suspend fun getPresetById(id: Int): EqualizerPreset?
}

@Database(entities = [EqualizerPreset::class], version = 1, exportSchema = false)
abstract class PresetDatabase : RoomDatabase() {
    abstract fun presetDao(): PresetDao

    companion object {
        @Volatile
        private var INSTANCE: PresetDatabase? = null

        fun getDatabase(context: Context): PresetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PresetDatabase::class.java,
                    "preset_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class PresetRepository(private val presetDao: PresetDao) {
    val allPresets: Flow<List<EqualizerPreset>> = presetDao.getAllPresets()

    suspend fun insert(preset: EqualizerPreset) {
        presetDao.insertPreset(preset)
    }

    suspend fun deleteById(id: Int) {
        presetDao.deletePresetById(id)
    }

    suspend fun getById(id: Int): EqualizerPreset? {
        return presetDao.getPresetById(id)
    }
}
