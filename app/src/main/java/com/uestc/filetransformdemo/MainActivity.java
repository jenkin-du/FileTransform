package com.uestc.filetransformdemo;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.uestc.net.api.DownloadManager;
import com.uestc.net.api.UploadManager;
import com.uestc.net.callback.TransportListener;
import com.uestc.net.protocol.ExceptionMessage;
import com.uestc.util.ToastUtil;


public class MainActivity extends AppCompatActivity {


    private ProgressBar downloadProgress;
    private ProgressBar uploadProgress;

    private UploadManager uploadManager;
    private DownloadManager downloadManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloadProgress = findViewById(R.id.downloadProgress);
        uploadProgress = findViewById(R.id.uploadProgress);


        Button downloadBtn = findViewById(R.id.btn_download);
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download();
            }


        });

        Button uploadBtn = findViewById(R.id.btn_upload);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });

        final Button downloadPauseBtn = findViewById(R.id.btn_download_pause);
        downloadPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                downloadManager.onPause();
            }
        });

        Button uploadPauseBtn = findViewById(R.id.btn_upload_pause);
        uploadPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uploadManager.onPause();
            }
        });

        downloadProgress.setProgress(0);
        downloadProgress.setIndeterminate(false);
        downloadProgress.setMax(100);

        uploadProgress.setProgress(0);
        uploadProgress.setMax(100);
    }


    private void upload() {

        uploadProgress.setProgress(0);
        String filePath = Environment.getExternalStorageDirectory() + "/20190125-6.zip";

        uploadManager = new UploadManager("192.168.0.114", 20001, filePath, "20190125-6.zip", new TransportListener() {

            /**
             * 传输过程
             *
             * @param percentage 传输百分比
             * @param totalSize  总大小 字节
             */
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onProgress(double percentage, long totalSize) {
                uploadProgress.setProgress((int) (percentage * 100), true);
            }

            /**
             * 传输结束
             */
            @Override
            public void onComplete() {
                runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "上传成功！！！", Toast.LENGTH_SHORT).show();
                        uploadProgress.setProgress(100, true);
                    }

                });
            }

            /**
             * 传输失败
             *
             */
            @Override
            public void onExceptionCaught(final ExceptionMessage exceptionMessage) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        switch (exceptionMessage) {
                            //网络不可用
                            case NETWORK_UNREACHABLE:

                                ToastUtil.showLong("网络不可用，请检查，然后继续下载");

                                break;
                            case CONNECTION_REFUSED:

                                ToastUtil.showLong("服务器拒绝连接，请检查服务器是否开启，然后继续下载");

                                break;

                            case FILE_NOT_EXIST:

                                ToastUtil.showLong("没有找到下载的文件");
                                break;

                            case FILE_MD5_WRONG:
                                ToastUtil.showLong("文件下载出现错误，正在重新下载");
                                break;

                            //没有赋予存取权限
                            case STORAGE_PERMISSION_DENIED:
                                ToastUtil.showLong("没有获取存取权限");
                                break;
                        }



                    }
                });


            }


        });
        uploadManager.onStart();
    }


    private void download() {

        downloadManager = new DownloadManager("192.168.0.114", 20001,
                "WeChatOpenSdkSample.7z", null, new TransportListener() {

            /**
             * 传输过程
             *
             * @param percentage 传输百分比
             * @param totalSize  总大小 字节
             */
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onProgress(double percentage, long totalSize) {

                downloadProgress.setProgress((int) (percentage * 100), true);
            }

            /**
             * 传输结束
             */
            @Override
            public void onComplete() {

                runOnUiThread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "下载成功！！！", Toast.LENGTH_LONG).show();
                        downloadProgress.setProgress(100, true);
                    }

                });

            }

            /**
             * 传输出现异常
             *
             */
            @Override
            public void onExceptionCaught(final ExceptionMessage exception) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        switch (exception) {
                            //网络不可用
                            case NETWORK_UNREACHABLE:

                                ToastUtil.showLong("网络不可用，请检查，然后继续下载");

                                break;
                            case CONNECTION_REFUSED:

                                ToastUtil.showLong("服务器拒绝连接，请检查服务器是否开启，然后继续下载");

                                break;

                            case FILE_NOT_EXIST:

                                ToastUtil.showLong("没有找到下载的文件");
                                break;

                            case FILE_MD5_WRONG:
                                ToastUtil.showLong("文件下载出现错误，正在重新下载");
                                break;

                            //没有赋予存取权限
                            case STORAGE_PERMISSION_DENIED:
                                ToastUtil.showLong("没有获取存取权限");
                                break;
                        }

                    }
                });
            }


        });
        downloadManager.onStart();
    }


}
