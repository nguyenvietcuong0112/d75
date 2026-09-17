package com.bumptech.glide

import android.content.Context
import com.widget.themes.controlcenter.feature_wallpaper.MyAppGlideModule
import kotlin.Boolean
import kotlin.Suppress

internal class GeneratedAppGlideModuleImpl(
  @Suppress("UNUSED_PARAMETER")
  context: Context,
) : GeneratedAppGlideModule() {
  private val appGlideModule: MyAppGlideModule
  init {
    appGlideModule = MyAppGlideModule()
  }

  public override fun registerComponents(
    context: Context,
    glide: Glide,
    registry: Registry,
  ) {
    appGlideModule.registerComponents(context, glide, registry)
  }

  public override fun applyOptions(context: Context, builder: GlideBuilder) {
    appGlideModule.applyOptions(context, builder)
  }

  public override fun isManifestParsingEnabled(): Boolean = false
}
