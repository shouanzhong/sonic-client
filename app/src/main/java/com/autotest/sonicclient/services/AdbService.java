package com.autotest.sonicclient.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;

import com.autotest.sonicclient.adblibs.AdbBase64;
import com.autotest.sonicclient.adblibs.AdbConnection;
import com.autotest.sonicclient.adblibs.AdbCrypto;
import com.autotest.sonicclient.interfaces.BackgroundService;
import com.autotest.sonicclient.interfaces.HandlerService;
import com.autotest.sonicclient.threads.MExecutor;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.utils.LogUtil;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@BackgroundService
public class AdbService extends Service {
    private static final String TAG = "AdbService";

    private AdbConnection connection;
    private ExecutorService executorService;
    int TIMEOUT = 60;  // 秒

    public AdbService() {
    }

    void connect() {
        connect("localhost", 5555);
    }

    void connect(String ip, int port) {
        connection = null;
        MExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket address = new Socket(ip, port);
                    AdbCrypto crypto = createCrypto();

                    connection = AdbConnection.create(address, crypto);
                    connection.connect();
                } catch (Exception e) {
                    Log.e(TAG, "Connect fail...");
                }
            }
        });
    }

    private AdbCrypto createCrypto() {
        AdbBase64 base64 = data -> Base64.encodeToString(data, Base64.NO_WRAP);
        AdbCrypto crypto = null;

        File publicKey = new File(getFilesDir(), "adb_pub.key");
        File privateKey = new File(getFilesDir(), "adb.key");
        try {
            crypto = AdbCrypto.loadAdbKeyPair(base64, publicKey, privateKey);
        } catch (Exception e) {
            try {
                crypto = AdbCrypto.generateAdbKeyPair(base64);
                crypto.saveAdbKeyPair(publicKey, privateKey);
            } catch (NoSuchAlgorithmException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return crypto;
    }

    private boolean isConnected() {
        return connection != null && connection.isFine();
    }

    public AdbConnection getConnection() {
        return connection;
    }

    public void close() {
        if (isConnected()) {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        executorService = Executors.newSingleThreadExecutor();

        executorService.submit(() -> {
            long startTime = System.currentTimeMillis();

            while (true) {
                if (isConnected()) {
                    SystemClock.sleep(1000);
                    continue;
                }
                LogUtil.d(TAG, "Connecting... ");
                connect();
                if (isConnected()) {
                    LogUtil.d(TAG, "Connected !!");
                    try {
                        InjectorService.register(this, true);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    //
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(Constant.ACTION_ACC_PERMISSION);
                    sendBroadcast(broadcastIntent);
                }

                SystemClock.sleep(1000);
            }
        });

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdownNow();
        }
        close();
        LogUtil.d(TAG, "Service destroyed.");
    }
}