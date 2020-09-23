package com.vart.library.vdownloader.download

import com.vart.library.vdownloader.download.DownloaderEntity

//整个下载状态的描述，会存到本地
class DownloaderWrapper {

    val threadInfoList = mutableListOf<DownloaderEntity.ThreadInfo>()

    var fileSize: Long? = null

    var fileInfo: DownloaderEntity.FileInfo? = null

    var jsonProperties: String = ""

    var isCompleted: Boolean = false

    override fun toString(): String {
        return "DownloaderWrapper(threadInfoList=$threadInfoList, fileSize=$fileSize, fileInfo=$fileInfo, jsonProperties='$jsonProperties', isCompleted=$isCompleted)"
    }


}