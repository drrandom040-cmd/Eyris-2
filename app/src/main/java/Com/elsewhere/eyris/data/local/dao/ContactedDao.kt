package Com.elsewhere.eyris.data.local.dao

import androidx.room.*
import Com.elsewhere.eyris.data.local.entities.ContactedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactedDao {
    @Query("SELECT * FROM contacted ORDER BY contactedAt DESC")
    fun getAllContacted(): Flow<List<ContactedEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacted(contacted: ContactedEntity)

    @Delete
    suspend fun deleteContacted(contacted: ContactedEntity)
}
