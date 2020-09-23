package com.vart.library.vdownloader.download

interface IDownloader {

//    fun needDownload(wrapper: DownloaderWrapper): Boolean

    fun onStart(wrapper: DownloaderWrapper)

    fun onComplete(wrapper: DownloaderWrapper)

//    fun onResume(wrapper: DownloaderWrapper)
//
//    fun onPause(wrapper: DownloaderWrapper)

    fun onFail(wrapper: DownloaderWrapper)

    fun onProgress(progress: Float)

}