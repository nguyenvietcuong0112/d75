package com.widget.themes.controlcenter.feature_charging.data.dao

import androidx.room.*
import com.widget.themes.controlcenter.feature_charging.data.entity.ChargingAnimation

@Dao
interface ChargingAnimationDao {
    @Query("SELECT * FROM charging_animations")
    fun getAllAnimations(): List<ChargingAnimation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAnimations(animations: List<ChargingAnimation>)

    @Update
    fun updateAnimation(animation: ChargingAnimation)
}
