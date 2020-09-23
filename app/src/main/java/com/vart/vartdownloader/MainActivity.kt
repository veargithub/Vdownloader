package com.vart.vartdownloader

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.vart.vartdownloader.customer.VartProgressDialog
import com.vart.vartdownloader.download.DownloaderEntity
import com.vart.vartdownloader.download.DownloaderManager
import com.vart.vartdownloader.download.DownloaderWrapper
import com.vart.vartdownloader.download.IDownloader
import com.vart.vartdownloader.util.MD5
import com.vart.vartdownloader.util.StorageUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity(), IDownloader {

    val TAG = "VART_download"
    val fileInfo = DownloaderEntity.FileInfo(
        "http://stb-video.joowing.com/video/56f9ca22-2fa0-4625-9e08-c644476e04ee.mp4",
        "",
        "video0"
    )
    var progressDialog: VartProgressDialog ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initNotify()
        progressDialog = VartProgressDialog.Builder().context(this)
            .title("提示")
            .tips("正在下载")
            .onInteractionListener(object : VartProgressDialog.OnInteractionListener {
                override fun onConfirm(id: Int, subId: Int) {
                    Log.d(TAG, "on confirm")
                }

                override fun onCancel(id: Int, subId: Int) {
                    if (progressDialog?.tvCancel?.text == "暂停") {
                        progressDialog?.tvCancel?.text = "继续"
                        Log.d(TAG, "暂停")
                        DownloaderManager.pause(this@MainActivity, fileInfo)

                    } else {
                        progressDialog?.tvCancel?.text = "暂停"
                        Log.d(TAG, "继续")
                        DownloaderManager.addTask(this@MainActivity, fileInfo, this@MainActivity, 0)
                    }
                }

            })
            .canCancel(false)
            .btnConfirmEnabled(false)
            .btnConfirmText("安装")
            .btnCancelText("暂停")
            .build()

        btnDownload.setOnClickListener{

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                when {
                    ContextCompat.checkSelfPermission(
                        MainActivity@ this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        doSomeThing()
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {

                        ActivityCompat.requestPermissions(
                            MainActivity@ this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            1
                        )
                    }
                    else -> {
                        //AlertDialog.Builder(MainActivity@this).setTitle("提示")
//                            .setMessage("需要存储权限才能继续，是否去设置?")
//                            .setPositiveButton("设置") { _, _ ->
//
//                            }
//                            .setNegativeButton("不了") {p0, _ -> p0.dismiss()}
//                            .show()
                    }
                }
            } else {
                doSomeThing()
            }

        }

        btnDeleteFile.setOnClickListener {
            DownloaderManager.deleteDownloaderInfoTest(this, fileInfo)
        }

        btnPause.setOnClickListener {
            Log.d(TAG, "btnPause")
            DownloaderManager.pause(this, fileInfo)
        }

        btnFileExists.setOnClickListener {
            Log.d(
                TAG,
                "${
                    StorageUtils.fileExists(
                        this,
                        "video0", "56f9ca22-2fa0-4625-9e08-c644476e04ee.mp4",
                        true
                    )
                }"
            )
        }

        //15e05fd3b0b20dec2f0b8ca00b9b5e93
        btnMD5.setOnClickListener {
            val dir = StorageUtils.createCache(this, "video0")
            val file = File(dir.toString() + File.separator + "56f9ca22-2fa0-4625-9e08-c644476e04ee.mp4")
            Log.d(">>>>", MD5().calculateMD5(file) ?: "empty")
        }

        btnProgressbar.setOnClickListener {
            progressDialog?.show()
            progressDialog?.setProgress(50)
        }

        btnDownloaderNotify.setOnClickListener {
            Log.d(TAG, "btnDownloaderNotify")
            downloaderNotify()
        }
    }



    ///data/user/0/com.vart.vartdownloader/cache/video0
    ///storage/emulated/0/Android/data/com.vart.vartdownloader/cache/video0
    fun doSomeThing() {
        progressDialog?.show()
        DownloaderManager.addTask(this, fileInfo, this, 0)
    }

    override fun onStart(wrapper: DownloaderWrapper) {
        Log.d(TAG, "on onStart")

    }

    override fun onComplete(wrapper: DownloaderWrapper) {
        Log.d(TAG, "on complete")
        progressDialog?.tvConfirm?.isEnabled = true
        NotificationManagerCompat.from(this).apply {
            notificationBuilder?.setProgress(0, 0, false)?.setContentText("下载完成")
            notify(10001, notificationBuilder?.build()!!)
        }
    }

    override fun onFail(wrapper: DownloaderWrapper) {
        Log.d(TAG, "on onFail")

    }

    override fun onProgress(progress: Float) {
        progressDialog?.setProgress((progress * 100).toInt())
        NotificationManagerCompat.from(this).apply {
            notificationBuilder?.setProgress(100, (progress * 100).toInt(), false)
            notify(10001, notificationBuilder?.build()!!)
        }
    }

//    var mNotificationManager: NotificationManager? = null
    val channelId = "vart_downloader"
    val channelName = "apk下载"
    var notificationBuilder: NotificationCompat.Builder? = null

    fun initNotify() {
        notificationBuilder = NotificationCompat.Builder(this, channelId).apply {
            setContentTitle("下载")
            setContentText("下载中")
            setSmallIcon(R.mipmap.ic_launcher)
            priority = NotificationCompat.PRIORITY_LOW
        }

        NotificationManagerCompat.from(this).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationBuilder?.setChannelId(channelId)
                createNotificationChannel(channel)
            }
        }
    }
    fun downloaderNotify() {
        NotificationManagerCompat.from(this).apply {
            notificationBuilder?.setProgress(100, 30, false)
            notify(10001, notificationBuilder?.build()!!)
//            builder.setContentText("下载完成").setProgress(0, 0, false)
//            notify(10001, builder.build())
        }


        /*
        val mBuilder = NotificationCompat.Builder(this.getApplicationContext(), "notify_001")
        mBuilder.setSmallIcon(R.mipmap.ic_launcher)
        mBuilder.setContentTitle("Your Title")
        mBuilder.setContentText("Your text")

        mNotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "Your_channel_id"
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH
            )
            mNotificationManager?.createNotificationChannel(channel)
            mBuilder.setChannelId(channelId)
        }

        mNotificationManager?.notify(0, mBuilder.build())
        */

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doSomeThing()
                } else {
                    Toast.makeText(this, "抱歉，你无法使用此权限", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}