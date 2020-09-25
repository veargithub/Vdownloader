package com.vart.vartdownloader

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.vart.library.vdownloader.customer.AppUpgradeInfo
import com.vart.library.vdownloader.customer.UpgradeDialog
import com.vart.library.vdownloader.download.DownloaderEntity
import com.vart.library.vdownloader.download.DownloaderManager
import com.vart.library.vdownloader.download.DownloaderWrapper
import com.vart.library.vdownloader.download.IDownloader
import com.vart.library.vdownloader.util.MD5
import com.vart.library.vdownloader.util.NetworkUtil
import com.vart.library.vdownloader.util.StorageUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity(), IDownloader {

    val TAG = "VART_download"
    var fileInfo = DownloaderEntity.FileInfo(
        "https://jw-advertise.oss-cn-beijing.aliyuncs.com/apks/app-release.apk",
        "",
        "video0"
    )
//    var progressDialog: VartProgressDialog?= null
    var upgradeDialog: UpgradeDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initNotify()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(
                    MainActivity@ this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    checkUpgrade()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                    ActivityCompat.requestPermissions(
                        MainActivity@ this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        1
                    )
                }
                else -> {
                    Log.d(">>>>", "aaaa")
                }
            }
        } else {
            checkUpgrade()
        }
        checkUpgrade()
        btnDownload.setOnClickListener{

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                when {
                    ContextCompat.checkSelfPermission(
                        MainActivity@ this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        checkUpgrade()
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {

                        ActivityCompat.requestPermissions(
                            MainActivity@ this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            1
                        )
                    }
                    else -> {
                        AlertDialog.Builder(MainActivity@ this).setTitle("提示")
                            .setMessage("需要存储权限才能继续，是否去设置?")
                            .setPositiveButton("设置") { _, _ ->

                            }
                            .setNegativeButton("不了") { p0, _ -> p0.dismiss()}
                            .show()
                    }
                }
            } else {
                checkUpgrade()
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
            upgradeDialog = UpgradeDialog.Builder().buttonText("无需流量，立即安装").version("2.3.0").build();
            upgradeDialog?.show(supportFragmentManager, "upgrade")

        }

        btnDownloaderNotify.setOnClickListener {
            Log.d(TAG, "btnDownloaderNotify")
            downloaderNotify()
        }

        btnJavaActivity.setOnClickListener {
            startActivity(Intent(this, MainJavaActivity::class.java))
        }

        btnIsWifi.setOnClickListener {
            Log.d(TAG, "is wifi: ${NetworkUtil.isWifi(this)}")
        }

        btnAlert.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("更新提示")
                .setMessage("有新版本xxxx，是否更新")
                .setCancelable(false)
                .setPositiveButton(
                    "确定"
                ) { dialog, which ->
                    if (NetworkUtil.isCellular(this)) {
                        AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setMessage("您当前正在使用蜂窝网络，是否继续下载？")
                            .setCancelable(false)
                            .setPositiveButton("是") { dialog, which ->
                                doSomeThing()
                                dialog.dismiss()
                            }
                    } else {
                        doSomeThing()
                    }
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun checkUpgrade() {
        val appUpgradeInfo = AppUpgradeInfo()
        appUpgradeInfo.appVersion = "2.3.0"
        appUpgradeInfo.downloadUrl = "https://jw-advertise.oss-cn-beijing.aliyuncs.com/apks/app-release.apk"
        appUpgradeInfo.forced = 0
        appUpgradeInfo.versionLog = "升级功能点啊快点哈康师傅哈时代考古发掘打算\n 升级功能点啊快点哈康师傅哈时代考古发掘打算\n 升级功能点啊快点哈康师傅哈时代考古发掘打算\n sndfpsaf0h0sg0g"
        if (appUpgradeInfo.forced == -1) return
        val wrapper = DownloaderManager.loadDownloadWrapper(this, appUpgradeInfo.downloadUrl)
        upgradeDialog = UpgradeDialog.Builder()
            .version(appUpgradeInfo.appVersion)
            .buttonText("无需流量，立即安装")
            .canCancel(appUpgradeInfo.forced != 1)
            .upgradeLogs(appUpgradeInfo.versionLog)
            .onButtonClick {
                if (wrapper?.isCompleted == true) {
                    onComplete(wrapper)
                } else {
                    doSomeThing()
                }
            }.build()

        this.fileInfo =  DownloaderEntity.FileInfo(
            appUpgradeInfo.downloadUrl,
            "",
            "video0"
        )
        upgradeDialog?.show(supportFragmentManager, "upgrade")
    }

    ///data/user/0/com.vart.vartdownloader/cache/video0
    ///storage/emulated/0/Android/data/com.vart.vartdownloader/cache/video0
    fun doSomeThing() {
        upgradeDialog?.showProgressVisible(View.VISIBLE)
        upgradeDialog?.enableButton(false)
        DownloaderManager.addTask(this, fileInfo, this, 0)
    }

    fun installApkIntent(wrapper: DownloaderWrapper): Intent {
        val file = StorageUtils.createFile(
            this, wrapper.fileInfo!!.dictionary,
            wrapper.fileInfo!!.fileName, false
        )
        val fileUri = FileProvider.getUriForFile(
            this,
            this.getApplicationContext().getPackageName().toString() + ".provider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return intent

    }

    override fun onStart(wrapper: DownloaderWrapper) {
        Log.d(TAG, "on onStart in thread" + Thread.currentThread().name)

    }

    override fun onComplete(wrapper: DownloaderWrapper) {
        Log.d(TAG, "on complete in thread " + Thread.currentThread().name)
        upgradeDialog?.enableButton(true)
        upgradeDialog?.setButtonText("下载完成，立即安装？")
        NotificationManagerCompat.from(this).apply {
            notificationBuilder?.setProgress(100, 100, false)?.setContentText("下载完成")
            Log.d(TAG, "download complete")
            val pendingIntent = PendingIntent.getActivity(this@MainActivity, 1, installApkIntent(wrapper), PendingIntent.FLAG_UPDATE_CURRENT)
            notificationBuilder?.setContentIntent(pendingIntent)
            notify(10001, notificationBuilder?.build()!!)
        }
        upgradeDialog?.setOnButtonClick {
            Log.d(TAG, "install it")
            startActivity(installApkIntent(wrapper))
        }
    }

    override fun onFail(wrapper: DownloaderWrapper) {
        Log.d(TAG, "on onFail")
        upgradeDialog?.enableButton(true)
        upgradeDialog?.setButtonText("下载失败了，再试试？")
        upgradeDialog?.setOnButtonClick {
            doSomeThing()
        }
    }

    override fun onProgress(progress: Float) {
        upgradeDialog?.setProgress((progress * 100).toInt())
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