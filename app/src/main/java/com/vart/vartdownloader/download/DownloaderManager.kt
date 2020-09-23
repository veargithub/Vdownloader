package com.vart.vartdownloader.download

import android.content.Context
import android.content.DialogInterface
import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import com.vart.vartdownloader.util.StorageUtils
import kotlinx.coroutines.*
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

object DownloaderManager {

    private const val PREFERENCE_FILE_KEY = "vart_downloader_manager"
    val TAG = "VART_download"

    var config: DownloaderConfig = DownloaderConfig.Builder()
        .threadNum(5)
        .connectTimeout(5000)
        .readTimeout(5000)
        .retryTimes(1)
        .build()

    private val mapper = ObjectMapper()

    private val tasks = ConcurrentHashMap<String, DownloaderTask>()

    fun addTask(context: Context, fileInfo: DownloaderEntity.FileInfo, downloadImpl: IDownloader, fileSize: Long = 0) {
//        deleteDownloaderInfoTest(context, fileInfo) //todo test

        val previousTask = tasks[fileInfo.url]
        if (previousTask != null) {//如果task列表存在，说明正在下载中
            previousTask.downloaderImpl = downloadImpl
            //为了解决并发问题，要到后面才能return

        }

        val sp = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        val wrapper = sp.getString(fileInfo.url, null)
        var downloadWrapper: DownloaderWrapper? = null
        if (wrapper != null) {
            downloadWrapper = mapper.readValue(wrapper, DownloaderWrapper::class.java)
            if (downloadWrapper.isCompleted) {//如果这个文件已经下载完成了
                downloadImpl.onComplete(downloadWrapper)
                Log.d(TAG, "task already complete")
                return
            }
        }

        if (previousTask != null) {
            Log.d(TAG, "task exists")
            return
        }


        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        scope.launch {
            if (downloadWrapper == null) {//既没有下载已完成，也不是正在下载中

                downloadWrapper = DownloaderWrapper()
                if (fileInfo.fileName.isBlank()) fileInfo.fileName = fileInfo.url.substring(fileInfo.url.lastIndexOf("/") + 1) //如果没有设置文件名，则自动设置一个
                if (fileInfo.dictionary.isBlank()) fileInfo.dictionary = "tmp" //如果没有文件夹名，则自动设置一个
//                val cache = StorageUtils.createCache(context, fileInfo.dictionary)
//                fileInfo.fileName = cache.toString() + File.separator + fileInfo.url.substring(fileInfo.url.lastIndexOf("/") + 1)
                Log.d(TAG, "init wrapper ${fileInfo.fileName}")
                downloadWrapper?.fileInfo = fileInfo
                if (fileSize > 0) {
                    downloadWrapper?.fileSize = fileSize
                } else {
                    downloadWrapper?.fileSize = getFileSize(fileInfo.url)
                    Log.d(TAG, "file size: ${downloadWrapper?.fileSize}")
                    val threadNum = config.threadNum ?: 1
                    val block: Long = downloadWrapper?.fileSize!! / threadNum

                    for (i in 0 until threadNum) {
                        val start = i * block
                        val end =  if (i == (threadNum - 1)) downloadWrapper?.fileSize!! - 1 else start + block - 1
                        val threadInfo = DownloaderEntity.ThreadInfo(start, end, 0)
                        downloadWrapper?.threadInfoList?.add(threadInfo)
                    }
                }
            }
            Log.d(TAG, "wrapper $downloadWrapper")
            val task = DownloaderTask(context, downloadWrapper!!, config, downloadImpl)
            tasks[fileInfo.url] = task
            downloadImpl.onStart(downloadWrapper!!)
            withContext(Dispatchers.IO) {
                task.start()
            }

        }

    }

    private suspend fun getFileSize(url: String) : Long = withContext(Dispatchers.IO) {
        val uRL = URL(url)
        val httpURLConnection = uRL.openConnection() as HttpURLConnection
        httpURLConnection.requestMethod = "GET"
        httpURLConnection.setRequestProperty("Charset", "UTF-8")
        httpURLConnection.connectTimeout = config.connectTimeout ?: 5000
        httpURLConnection.readTimeout = config.readTimeout ?: 5000
//        httpURLConnection.setRequestProperty("Accept")
        httpURLConnection.connect()
        val fileSize = httpURLConnection.contentLength
        val code = httpURLConnection.responseCode
        if (code == 200 && fileSize > 0) {
            fileSize.toLong()
        } else {
            0L
        }
    }

    fun saveWrapper(context: Context, downloaderWrapper: DownloaderWrapper) {
        val sp = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        sp.edit().putString(downloaderWrapper.fileInfo?.url, mapper.writeValueAsString(downloaderWrapper)).apply()
    }

    fun removeTask(downloaderTask: DownloaderTask) {
        tasks.remove(downloaderTask.downloaderWrapper.fileInfo?.url!!, downloaderTask)
    }

    fun pause(context: Context, fileInfo: DownloaderEntity.FileInfo) {
        Log.d(TAG, "pause")
        val task = tasks[fileInfo.url]
        task?.let {
            it.stop()
            tasks.remove(fileInfo.url, it)
        }
    }

    fun deleteDownloaderInfoTest(context: Context, fileInfo: DownloaderEntity.FileInfo) {
        Log.d(TAG, "deleteDownloaderInfoTest")
        pause(context, fileInfo)
        val sp = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
        sp.edit().remove(fileInfo.url).commit()
        if (fileInfo.fileName.isBlank()) fileInfo.fileName = fileInfo.url.substring(fileInfo.url.lastIndexOf("/") + 1) //如果没有设置文件名，则自动设置一个

        StorageUtils.deleteFile(context, fileInfo.dictionary, fileInfo.fileName)

    }

//    fun isDownloadComplete(downloaderWrapper: DownloaderWrapper): Boolean {
//        return downloaderWrapper.threadInfoList.map {it.offset} .sum() >= downloaderWrapper.fileSize ?: 0
//    }
}