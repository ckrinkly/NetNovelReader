package com.netnovelreader.bean

import android.databinding.ObservableField
import java.io.Serializable

data class SearchBookResult(
        val bookname: ObservableField<String>,
        val url: ObservableField<String>,
        val latestChapter: ObservableField<String?>,
        val catalogMap: LinkedHashMap<String, String>
) : Serializable