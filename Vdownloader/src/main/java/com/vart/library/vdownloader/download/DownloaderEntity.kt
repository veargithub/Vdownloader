package com.vart.library.vdownloader.download

/**
 * 对于单个下载线程的描述
 */
class DownloaderEntity(val threadInfo: ThreadInfo?, var fileInfo: FileInfo?, var status: Status?) {

    data class Builder(
        var threadInfo: ThreadInfo? = null,
        var fileInfo: FileInfo? = null,
        var status: Status? = null) { //下载的状态

        fun threadInfo(info: ThreadInfo) = apply { this.threadInfo = info }
        fun fileInfo(fileInfo: FileInfo) = apply { this.fileInfo = fileInfo }
//        fun url(url: String?) = apply { this.url = url }
//        fun fileName(fileName: String?) = apply { this.fileName = fileName }
        fun status(status: Status?) = apply { this.status = status }

        fun build(): DownloaderEntity {
            return DownloaderEntity(threadInfo, fileInfo, status)
        }
    }

    class ThreadInfo(//下载状态
        var start: Long = 0, //从文件的什么地方开始下载
        var end: Long = 0, //下载到文件的什么位置
        var offset: Long = 0 //已经下载了多少
    )

    class FileInfo(//文件信息
        var url: String = "", //下载的url
        var fileName: String = "", //下载完成后的文件名
        var dictionary: String = "" //文件下载到sd卡的什么位置，默认是/data/data/Android/包名/{dictionary}/{fileName}
    )

    enum class Status {

        pending, //未下载、暂停
        downloading, //下载中
        complete //下载已完成

    }
}