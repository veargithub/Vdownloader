package com.vart.vartdownloader;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.vart.library.vdownloader.customer.VartProgressDialog;
import com.vart.library.vdownloader.download.DownloaderEntity;
import com.vart.library.vdownloader.download.DownloaderManager;

public class MainJavaActivity extends AppCompatActivity {

    private static final String TAG = "VART_download";
    DownloaderEntity.FileInfo fileInfo = new DownloaderEntity.FileInfo(
            "http://stb-video.joowing.com/video/56f9ca22-2fa0-4625-9e08-c644476e04ee.mp4",
            "",
            "video0"
    );
    VartProgressDialog progressDialog = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloaderManager.INSTANCE.deleteDownloaderInfoTest(this, fileInfo);
    }
}
