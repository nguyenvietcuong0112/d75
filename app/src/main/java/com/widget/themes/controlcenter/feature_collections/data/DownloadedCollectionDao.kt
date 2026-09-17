package com.widget.themes.controlcenter.feature_collections.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedCollectionDao {

    @Query("SELECT * FROM downloaded_collections WHERE LOWER(category) = LOWER(:category) ORDER BY appliedTimestamp DESC")
    fun getDownloadedByCategoryFlow(category: String): Flow<List<DownloadedCollectionItem>>

    @Query("SELECT * FROM downloaded_collections WHERE LOWER(category) = LOWER(:category) ORDER BY appliedTimestamp DESC")
    suspend fun getDownloadedByCategory(category: String): List<DownloadedCollectionItem>

    @Query("SELECT * FROM downloaded_collections ORDER BY appliedTimestamp DESC")
    suspend fun getAllDownloaded(): List<DownloadedCollectionItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloaded(item: DownloadedCollectionItem)

    @Query("DELETE FROM downloaded_collections WHERE id = :id")
    suspend fun deleteDownloaded(id: String)

    @Query("SELECT COUNT(*) FROM downloaded_collections WHERE (:id != '' AND (id = :id OR id = :prefixedId)) OR (:path != '' AND targetPath = :path) OR (:name != '' AND LOWER(name) = LOWER(:name))")
    suspend fun countDownloaded(id: String, prefixedId: String, path: String, name: String): Int
}
