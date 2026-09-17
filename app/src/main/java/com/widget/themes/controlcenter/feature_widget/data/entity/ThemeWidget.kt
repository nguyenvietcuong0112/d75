package com.widget.themes.controlcenter.feature_widget.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.widget.themes.controlcenter.feature_widget.data.entity.WidgetTheme
import java.util.UUID

/**
 * Entity representing a Widget included in a Theme
 */
@Entity(
    tableName = "theme_widgets",
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
data class ThemeWidget(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val themeId: UUID, // Foreign key linked to WidgetTheme
    val templatePath: String, // Folder path of template on CDN (e.g.: "templates/template1")
    val size: String, // Size: SMALL / MEDIUM / LARGE
    val type: String // Widget type: CLOCK / CALENDAR / WEATHER / IMAGE
) : java.io.Serializable
