package com.widget.themes.controlcenter.feature_icon.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetTheme
import java.util.UUID

/**
 * Entity representing an Icon Pack bundled with each Theme
 */
@Entity(
    tableName = "theme_icon_packs",
    foreignKeys = [
        ForeignKey(
            entity = WidgetTheme::class,
            parentColumns = ["id"],
            childColumns = ["themeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["themeId"])]
)
data class ThemeIconPack(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val themeId: UUID, // Foreign key linked to WidgetTheme
    val folder: String,
    val name: String
) : java.io.Serializable
