package com.widget.themes.controlcenter.core.utils

import kotlin.math.abs
import kotlin.random.Random

object DownloadCountUtils {
    private const val MIN_DOWNLOADS = 1200
    private const val MAX_DOWNLOADS = 98500

    /**
     * If downloads <= 0 or is the placeholder 7654321, returns a stable pseudo-random number
     * between MIN_DOWNLOADS and MAX_DOWNLOADS based on seed (e.g. item id or name).
     * If seed is empty, generates a random number between MIN_DOWNLOADS and MAX_DOWNLOADS.
     */
    fun resolveDownloads(downloads: Long, seed: String = ""): Long {
        if (downloads > 0 && downloads != 7654321L) return downloads
        return if (seed.isNotEmpty()) {
            val hashSeed = abs(seed.hashCode().toLong())
            val rand = Random(hashSeed)
            rand.nextInt(MIN_DOWNLOADS, MAX_DOWNLOADS + 1).toLong()
        } else {
            Random.nextInt(MIN_DOWNLOADS, MAX_DOWNLOADS + 1).toLong()
        }
    }
}
