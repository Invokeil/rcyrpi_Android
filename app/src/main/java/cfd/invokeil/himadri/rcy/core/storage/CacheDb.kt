package cfd.invokeil.himadri.rcy.core.storage

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Offline cache — one generic JSON snapshot table. Each endpoint response is
 * stored raw and re-parsed on read; robust against model evolution and keeps
 * the app fully browsable when the server is down (Supabase idle etc).
 */
@Entity(tableName = "cache")
data class CacheEntry(
    @PrimaryKey val key: String,
    val json: String,
    val updatedAt: Long,
)

@Dao
interface CacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entry: CacheEntry)

    @Query("SELECT * FROM cache WHERE `key` = :key")
    suspend fun get(key: String): CacheEntry?

    @Query("SELECT updatedAt FROM cache WHERE `key` = :key")
    suspend fun updatedAt(key: String): Long?

    @Query("DELETE FROM cache")
    suspend fun clear()
}

@Database(entities = [CacheEntry::class], version = 1, exportSchema = false)
abstract class CacheDb : RoomDatabase() {
    abstract fun cacheDao(): CacheDao

    companion object {
        @Volatile private var instance: CacheDb? = null

        fun get(context: Context): CacheDb = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, CacheDb::class.java, "rcy_cache.db"
            ).fallbackToDestructiveMigration().build().also { instance = it }
        }
    }
}

/** Typed offline-cache helpers used by repositories. */
class CacheStore(context: Context) {
    private val dao = CacheDb.get(context).cacheDao()

    suspend fun <T> get(key: String, parse: (String) -> T): T? = withContext(Dispatchers.IO) {
        runCatching { dao.get(key)?.let { parse(it.json) } }.getOrNull()
    }

    suspend fun put(key: String, json: String) = withContext(Dispatchers.IO) {
        runCatching { dao.put(CacheEntry(key, json, System.currentTimeMillis())) }
    }

    suspend fun age(key: String): Long? = dao.updatedAt(key)?.let { System.currentTimeMillis() - it }

    suspend fun clear() = dao.clear()
}
