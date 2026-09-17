package com.widget.themes.controlcenter.feature_widget.data.dao

import androidx.room.*
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetItem

@Dao
interface WidgetItemDao {
    @Query("SELECT * FROM widget_items")
    fun getAllWidgetItems(): List<WidgetItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWidgetItems(items: List<WidgetItem>)
}
