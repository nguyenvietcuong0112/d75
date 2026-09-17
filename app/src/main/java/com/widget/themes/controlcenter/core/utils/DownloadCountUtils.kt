package com.widget.themes.controlcenter.core.utils

import kotlin.math.abs
import kotlin.random.Random

object DownloadCountUtils {
    private const val MIN_DOWNLOADS = 500
    private const val MAX_DOWNLOADS = 99999

    /**
     * If downloads <= 0, returns a stable pseudo-random number between 500 and 99999 based on seed.
     * If seed is empty, generates a random number between 500 and 99999.
     */
    fun resolveDownloads(downloads: Long, seed: String = ""): Long {
        if (downloads > 0) return downloads
        return if (seed.isNotEmpty()) {
            val hashSeed = abs(seed.hashCode().toLong())
            val rand = Random(hashSeed)
            rand.nextInt(MIN_DOWNLOADS, MAX_DOWNLOADS + 1).toLong()
        } else {
            Random.nextInt(MIN_DOWNLOADS, MAX_DOWNLOADS + 1).toLong()
        }
    }
}
