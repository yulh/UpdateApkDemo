package com.update.apk.update.download;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.update.apk.R;
import com.update.apk.update.http.DownLoadOperate;
import com.update.apk.update.utils.StringUtils;

import java.io.File;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

import static com.update.apk.update.download.DownLoadUtil.APP_NAME;
import static com.update.apk.update.download.DownLoadUtil.MESSAGE_PROGRESS;


/**
 * Created by jason_syf on 2017/9/28.
 * Email:jason_sunyf@163.com
 */

public class DownFileService extends IntentService {
    private static final String TAG = "DownloadApi";
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    int downloadCount = 0;
    private DownloadCompleteReceiver receiver;

    public DownFileService() {
        super("DownloadApi");
    }

    public class DownloadCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MESSAGE_PROGRESS)) {
                DownLoadBean download = intent.getParcelableExtra("download");
                if (download.getCurrentFileSize() == download.getTotalFileSize()) {
                    Toast.makeText(context, "下载完成", Toast.LENGTH_SHORT).show();
                    DownLoadUtil.insrance.installAPK(context);
                } else {
                    Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show();
                }
                DownFileService.this.stopSelf();
            }
        }
    }

    ;


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
//     通知栏样式设定
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//     通知初始化
        receiver = new DownloadCompleteReceiver();
        notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("下载")
                .setContentText("正在下载应用")
                .setAutoCancel(true);
        notificationManager.notify(0, notificationBuilder.build());
        download();
        DownLoadUtil.insrance.registerReceiver(this, receiver);
    }

    private void download() {
        DownProgressListener listener = new DownProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
                //不频繁发送通知，防止通知栏下拉卡顿
                int progress = (int) ((bytesRead * 100) / contentLength);
                if ((downloadCount == 0) || progress > downloadCount) {
                    DownLoadBean download = new DownLoadBean();
                    download.setTotalFileSize(contentLength);
                    download.setCurrentFileSize(bytesRead);
                    download.setProgress(progress);
                    //此处注释掉后只执行一次，但是进度不再显示了
                    sendNotification(download);
                }
            }
        };
//      安装包名
        String fileName = APP_NAME + ".apk";
        File outputFile = new File(Environment.getExternalStorageDirectory()
//                (Environment.DIRECTORY_DOWNLOADS)
                , fileName);

        if (outputFile.exists()) {
            outputFile.delete();
        }

        String baseUrl = StringUtils.getHostName(DownLoadUtil.insrance.getApkUrl());
        new DownLoadOperate(baseUrl, listener).downloadAPK(DownLoadUtil.insrance.getApkUrl(), outputFile, new Observer() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
            }

            @Override
            public void onNext(@NonNull Object o) {
            }

            @Override
            public void onComplete() {
                downloadCompleted("下载完成");
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                notificationManager.cancel(0);
                notificationBuilder.setProgress(0, 0, false);
                notificationBuilder.setContentText("下载出错");
                Log.e(TAG, "onError: " + e.getMessage());
            }
        });
    }

    private void downloadCompleted(String msg) {
        DownLoadBean download = new DownLoadBean();
        download.setProgress(100);
        sendIntent(download);
        notificationBuilder.setProgress(0, 0, false);
        notificationBuilder.setContentText(msg);
        notificationManager.notify(0, notificationBuilder.build());
        notificationManager.cancel(0);

//        installAPK(getApplicationContext());
    }

    private void sendNotification(DownLoadBean download) {
        //此处重复发送intent 导致安装时候重复调用
//        sendIntent(download);
        notificationBuilder.setProgress(100, download.getProgress(), false);
        notificationBuilder.setContentText(
                StringUtils.getDataSize(download.getCurrentFileSize()) + "/" +
                        StringUtils.getDataSize(download.getTotalFileSize()));
        notificationManager.notify(0, notificationBuilder.build());
    }


    private void sendIntent(DownLoadBean download) {

        Intent intent = new Intent(MESSAGE_PROGRESS);
        intent.putExtra("download", download);
        LocalBroadcastManager.getInstance(DownFileService.this).sendBroadcast(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        notificationManager.cancel(0);
    }
}
