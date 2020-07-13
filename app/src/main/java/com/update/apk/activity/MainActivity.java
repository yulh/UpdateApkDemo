package com.update.apk.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.update.apk.R;
import com.update.apk.update.download.DownLoadUtil;

public class MainActivity extends AppCompatActivity {
    private String m360SafeUrl =
            "http://msoftdl.360.cn/mobile/shouji360/360safesis/198227/360MobileSafe_7.7.3.1016.apk";
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        AppCompatButton btn_download = (AppCompatButton) findViewById(R.id.btn_download);
//        DownLoadUtil.insrance.registerReceiver(this, DownloadCompleteReceiver.class);
        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "点击了下载按钮", Toast.LENGTH_SHORT).show();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // 检查是否有存储和拍照权限
                    if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            && context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    ) {
                        //有权限
                        //                此处放在需要监听安装的逻辑上，比如后台提供的接口检查版本号与最新的不一致，调用此方法
                        DownLoadUtil.insrance.startInstall(MainActivity.this, m360SafeUrl);
                    } else {
                        //没有权限，开始申请
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                } else {
                    //                此处放在需要监听安装的逻辑上，比如后台提供的接口检查版本号与最新的不一致，调用此方法
                    DownLoadUtil.insrance.startInstall(MainActivity.this, m360SafeUrl);
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //                此处放在需要监听安装的逻辑上，比如后台提供的接口检查版本号与最新的不一致，调用此方法
                DownLoadUtil.insrance.startInstall(MainActivity.this, m360SafeUrl);
            } else {
                Toast.makeText(this, "权限授予失败！", Toast.LENGTH_SHORT).show();
            }
        }
    }

}