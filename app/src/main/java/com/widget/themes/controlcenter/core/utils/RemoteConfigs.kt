package com.iconchanger.wallpaper.rolling.icons.utils

import com.cscmobi.libraryads.commons.remote.CSCKonfigModel
import com.cscmobi.libraryads.commons.remote.konfig

object RemoteConfigs : CSCKonfigModel {

    val native_permission by konfig("native_permission", true)
    val native_all by konfig("native_all", true)
    val native_home by konfig("native_home", true)
    val banner_home by konfig("banner_home", true)
    val banner_collap_home by konfig("banner_collap_home", true)
    val inter_all by konfig("inter_all", true)
    val reward_ad by konfig("reward_ad", true)
    val native_collap by konfig("native_collap", true)


}