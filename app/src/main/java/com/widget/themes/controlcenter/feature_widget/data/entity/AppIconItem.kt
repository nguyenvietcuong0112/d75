package com.widget.themes.controlcenter.feature_widget.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.widget.themes.controlcenter.feature_icon.data.entity.ThemeIconPack
import java.util.UUID

/**
 * Entity representing an individual App Icon
 */
@Entity(
    tableName = "app_icon_items",
    foreignKeys = [
        ForeignKey(
            entity = ThemeIconPack::class,
            parentColumns = ["id"],
            childColumns = ["iconPackId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["iconPackId"])]
)
data class AppIconItem(
    @PrimaryKey val id: String, // e.g.: "facebook", "tiktok", "chrome"
    val iconPackId: UUID, // Foreign key linked to ThemeIconPack
    val appName: String, // Default display name
    val packageId: String, // App package name (e.g.: "com.facebook.katana")
    val customDisplayName: String? = null, // User-customized display name
    val customPackageId: String? = null // User-customized package name
) : java.io.Serializable
