package com.vart.library.vdownloader.download
import android.util.Log
import com.vart.library.vdownloader.download.DownloaderConfig
import com.vart.library.vdownloader.download.DownloaderEntity

import java.io.InputStream
import java.io.RandomAccessFile
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CountDownLatch

class DownloaderThread (

    private val id: Int,//用来记录当前是第几个线程
    private val path: String, //需要下载到哪个文件
    var isStop: Boolean, //暂停下载的本质是结束下载的所有线程，如果继续，会重启新的线程继续下载，这个变量控制是否暂停
    private val latch: CountDownLatch?,
    private var progressImpl: DownloaderTask.IProgress?, //每个线程会首先回调给创建他的task，然后由这个task统一回调给IDownloader
    val downloaderEntity: DownloaderEntity?, //描述当前下载状态
    private val downloaderConfig: DownloaderConfig?): Runnable {

    private val TAG = "VART_download"

    override fun run() {
        val rangeStart = downloaderEntity?.threadInfo?.start ?: 0
        val rangeOffset = downloaderEntity?.threadInfo?.offset ?: 0
        if (rangeStart + downloaderEntity?.threadInfo?.offset!! >= downloaderEntity.threadInfo.end) {//该线程已经下载完成了
            downloaderEntity.status = DownloaderEntity.Status.complete
            Log.d(TAG, "thread $id has already completed")
            latch?.countDown()
            return
        }
        val url = URL(downloaderEntity.fileInfo?.url)
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.setRequestProperty("Range", "bytes=" + (rangeStart + rangeOffset) + "-" + downloaderEntity.threadInfo.end)
        connection.requestMethod = "GET"
        connection.setRequestProperty("Charset", "UTF-8")
        connection.connectTimeout = downloaderConfig!!.connectTimeout!!
        connection.setRequestProperty("User-Agent", "joowing")
//        connection.setRequestProperty("Accept", "application/vnd.android.package-archive")
        connection.readTimeout = downloaderConfig.readTimeout!!

        val inputStream: InputStream = try{ connection.inputStream } catch (e: Exception) {
            Log.d(TAG, "get input stream error ${e.toString()}")
            latch?.countDown()
            return
        }
        val file = RandomAccessFile(path, "rwd")
        file.seek(rangeStart + rangeOffset)

        val buffer = ByteArray(4096)
        downloaderEntity.status = DownloaderEntity.Status.downloading
        Log.d(TAG, "thread $id id ${Thread.currentThread().id} is running start: $rangeStart, offset: $rangeOffset, end: ${downloaderEntity.threadInfo.end}")
        var bufferedLen = 0L
        while (true) {
            if (isStop) {//如果暂停
                Log.d(TAG, "$id stopped")
                downloaderEntity.status = DownloaderEntity.Status.pending
                break
            }
            val len = try {
                inputStream.read(buffer)
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                -1
            }
            if (len == -1) {
                break
            }
            file.write(buffer, 0, len)

            downloaderEntity.threadInfo.offset = (downloaderEntity.threadInfo.offset ?: 0) + len.toLong()
            bufferedLen += len
            if (bufferedLen >= 409600) {
                Log.d(TAG, "thread $id download ${downloaderEntity.threadInfo.offset}")
                progressImpl?.onProgressUpdate(bufferedLen.toInt())
                bufferedLen = 0
            }
        }
        if (rangeStart + downloaderEntity.threadInfo.offset >= downloaderEntity.threadInfo.end) {
            downloaderEntity.status = DownloaderEntity.Status.complete
        }
        progressImpl?.onProgressUpdate(bufferedLen.toInt())
        Log.d(TAG, "thread $id end: ${downloaderEntity.status}")
        latch?.countDown()
        try {
            file.close()
            inputStream.close()
        } catch (e: Exception) {}
    }

    data class Builder(
        var id: Int = 0,
        var path: String = "",
        var isStop: Boolean = false,
        var latch: CountDownLatch? = null,
        var downloaderImpl: DownloaderTask.IProgress? = null,
        var downloaderEntity: DownloaderEntity? = null,
        var downloaderConfig: DownloaderConfig? = null) {
        fun id(id: Int) = apply { this.id = id }
        fun path(path: String) = apply { this.path = path }
        fun isStop(isStop: Boolean) = apply { this.isStop = isStop }
        fun latch(latch: CountDownLatch?) = apply { this.latch = latch }
        fun downloader(downloader: DownloaderTask.IProgress?) = apply { this.downloaderImpl = downloader }
        fun downloaderEntity(downloaderEntity: DownloaderEntity?) = apply { this.downloaderEntity = downloaderEntity }
        fun downloaderConfig(downloaderConfig: DownloaderConfig?) = apply { this.downloaderConfig = downloaderConfig }
        fun build(): DownloaderThread {
            return DownloaderThread(id, path, isStop, latch, downloaderImpl, downloaderEntity, downloaderConfig)
        }
    }
}