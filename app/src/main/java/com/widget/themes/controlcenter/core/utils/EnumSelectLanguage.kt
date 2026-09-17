package com.widget.themes.controlcenter.core.utils

import com.widget.themes.controlcenter.R
import com.cscmobi.libraryads.data.LanguageModel

enum class EnumSelectLanguage(val id: Int, val nameLanguage: Int, val flag: Int, val code: String) {
    FRENCH(1, R.string.language_french, R.drawable.flag_fr, "fr"),
    UNITED_STATES(2, R.string.language_english, R.drawable.flag_us, "en"),
    HINDI(3, R.string.language_hindi, R.drawable.flag_in, "hi"),
    SPANISH(4, R.string.language_spanish, R.drawable.flag_es, "es"),
    PORTUGUESE(5, R.string.language_portuguese, R.drawable.flag_pt, "pt"),
    INDONESIAN(6, R.string.language_indonesian, R.drawable.flag_id, "id"),
    BENGALI(7, R.string.language_bengali, R.drawable.flag_bn, "bn"),
    PHILIPPINES(8, R.string.language_filipino, R.drawable.flag_ph, "fil"),
    GERMAN(9, R.string.language_german, R.drawable.flag_de, "de"),
    KOREAN(10, R.string.language_korean, R.drawable.flag_kr, "ko"),
    ITALIAN(11, R.string.language_italian, R.drawable.flag_it, "it"),
    CHINESE(12, R.string.language_chinese, R.drawable.flag_cn, "zh"),
    VIETNAMESE(13, R.string.language_vietnamese, R.drawable.flag_vn, "vi"),
    RUSSIAN(14, R.string.language_russian, R.drawable.flag_ru, "ru");

    companion object {
        fun toLanguageModelList(): ArrayList<LanguageModel> {
            val list = arrayListOf<LanguageModel>()
            for (lang in entries) {
                list.add(
                    LanguageModel(
                        txtLanguage = lang.nameLanguage,
                        icFlag = lang.flag,
                        code = lang.code,
                        isSelect = false
                    )
                )
            }
            return list
        }
    }
}
