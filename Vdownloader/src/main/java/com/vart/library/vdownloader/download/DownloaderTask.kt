package com.vart.library.vdownloader.download

import android.content.Context
import android.util.Log
import com.vart.library.vdownloader.download.DownloaderConfig
import com.vart.library.vdownloader.download.DownloaderEntity
import com.vart.library.vdownloader.util.StorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.RandomAccessFile
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicLong

class DownloaderTask (val context: Context, val downloaderWrapper: DownloaderWrapper, val downloaderConfig: DownloaderConfig, var downloaderImpl: IDownloader?) {

    val TAG = "VART_download"
    val atomCurrent = AtomicLong(0L)//当前下载总量
    var retryTimes = 0 //当前重试次数
    var isStop: Boolean = false //是否已停止
    private val threads = mutableListOf<DownloaderThread>()
    private val progressImpl = object: IProgress {
        override fun onProgressUpdate(plus: Int) {
            if (isStop) {
                Log.d(TAG, "onProgressUpdate: task stop")
                return
            }
            val current = atomCurrent.addAndGet(plus.toLong())
            val progress = current * 1.0f / (downloaderWrapper.fileSize ?: 1L)
            downloaderImpl?.onProgress(progress)
            Log.d(TAG, "plus: $plus current: $current total: ${downloaderWrapper.fileSize} progress $progress")
            if (progress >= 1.0f) downloaderWrapper.isCompleted = true
            DownloaderManager.saveWrapper(context, downloaderWrapper)
            //这里因为没有做同步，所以不好删除掉该task
        }
    }

    fun start() {
        val file = StorageUtils.createFile(context, downloaderWrapper.fileInfo!!.dictionary,
            downloaderWrapper.fileInfo!!.fileName, false)
        val randomFile = RandomAccessFile(file.path, "rwd")
        randomFile.setLength(downloaderWrapper.fileSize!!)
        randomFile.close()

        atomCurrent.set(0L)
        val latch = CountDownLatch(downloaderWrapper.threadInfoList.size)
        downloaderWrapper.threadInfoList.forEach {
            atomCurrent.addAndGet(it.offset)
        }
        progressImpl.onProgressUpdate(0)
        Log.d(TAG, "task ${Thread.currentThread().id} start current: ${atomCurrent.get()} retryTimes: $retryTimes path: ${file.path} ")
        downloaderWrapper.threadInfoList.forEachIndexed{ index, it ->
            val downloaderEntity = DownloaderEntity.Builder()
                .threadInfo(it)
                .fileInfo(downloaderWrapper.fileInfo!!)
                .status(DownloaderEntity.Status.pending)
                .build()
            val downloaderThread = DownloaderThread.Builder()
                .id(index)
                .path(file.path)
                .isStop(isStop)
                .downloader(progressImpl)
                .downloaderConfig(downloaderConfig)
                .downloaderEntity(downloaderEntity)
                .latch(latch)
                .build()
            val thread = Thread(downloaderThread)
            threads.add(downloaderThread)
            thread.start()
//            Log.d(TAG, ">>>> ${isStop === downloaderThread.isStop}")
        }
        latch.await()
        if (isStop) {
            //todo downloaderImpl.onStop
            Log.d(TAG, "task stopped")
            return
        }
        if (downloaderWrapper.isCompleted) {
            downloaderImpl?.onComplete(downloaderWrapper)
            return
        }
        if (retryTimes >= downloaderConfig.retryTimes) {
            Log.d(TAG, "failed: $retryTimes ${downloaderConfig.retryTimes}")
            downloaderImpl?.onFail(downloaderWrapper)
        } else {
            Log.d(TAG, "retry: $retryTimes ${downloaderConfig.retryTimes}")
            retryTimes++
            start()
        }
    }

    fun stop() {
        Log.d(TAG, "task stop")
        this.isStop = true
        threads.forEach { it.isStop = true }
    }

    interface IProgress {
        fun onProgressUpdate(plus: Int)
    }
}