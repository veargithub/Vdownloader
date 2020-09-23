package com.vart.vartdownloader.download

class DownloaderConfig private constructor(val threadNum: Int?, val connectTimeout: Int?, val readTimeout: Int? , val retryTimes: Int){


    data class Builder(
        var threadNum: Int? = 1, //启动多少个线程下载
        var connectTimeout: Int? = 5000, //连接超时
        var readTimeout: Int? = 5000,
        var retryTimes: Int = 0) {//读超时

        fun threadNum(threadNum: Int) = apply { this.threadNum = threadNum }
        fun connectTimeout(connectTimeout: Int) = apply { this.connectTimeout = connectTimeout }
        fun readTimeout(readTimeout: Int) = apply { this.readTimeout = readTimeout }
        fun retryTimes(retryTimes: Int) = apply { this.retryTimes = retryTimes }

        fun build() = DownloaderConfig(threadNum, connectTimeout, readTimeout, retryTimes)
    }
}