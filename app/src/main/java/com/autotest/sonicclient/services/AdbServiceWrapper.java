package com.autotest.sonicclient.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;

import com.autotest.sonicclient.adblibs.AdbBase64;
import com.autotest.sonicclient.adblibs.AdbConnection;
import com.autotest.sonicclient.adblibs.AdbCrypto;
import com.autotest.sonicclient.adblibs.AdbStream;
import com.autotest.sonicclient.interfaces.Assemble;
import com.autotest.sonicclient.interfaces.HandlerService;
import com.autotest.sonicclient.threads.MExecutor;
import com.autotest.sonicclient.utils.LogUtil;
import com.autotest.sonicclient.utils.ShellUtil;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Queue;


@HandlerService
public class AdbServiceWrapper {
    private static final String TAG = "AdbService";
    private AdbConnection connection;
    Context context;
    @Assemble
    private AdbService adbService;

    public AdbServiceWrapper(Context context) {
        this.context = context;
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
                    e.printStackTrace();
                }
            }
        });
    }

    private AdbCrypto createCrypto() {
        //
        AdbBase64 base64 = data -> Base64.encodeToString(data, Base64.NO_WRAP);
        AdbCrypto crypto = null;

        //
        File publicKey = new File(context.getFilesDir(), "adb_pub.key");
        File privateKey = new File(context.getFilesDir(), "adb.key");
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

    public void execCmd(String cmd, ShellUtil.OnStreamChangedListener listener, int timeout) {
        LogUtil.d(TAG, "execCmd: " + cmd);
        StringBuilder sb = new StringBuilder();
        MExecutor.execute(() -> {
            try {
                AdbStream stream = adbService.getConnection().open(String.format("shell:%s", cmd));
                wait(stream, timeout);
                // 获取stream所有输出
                Queue<byte[]> results = stream.getReadQueue();
                for (byte[] bytes : results) {
                    if (bytes != null) {
                        listener.onStreamChanged(new String(bytes));
                        sb.append(new String(bytes));
                    }
                }
                String string = sb.toString();
//                LogUtil.d(TAG, "execCmd: " + string);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, timeout);
    }

    public String execCmd(String cmd, int timeout) {
        StringBuilder sb = new StringBuilder();
        execCmd(cmd, new ShellUtil.OnStreamChangedListener() {
            @Override
            public void onStreamChanged(String line) {
                sb.append(line);
            }

            @Override
            public void onErrorStreamChanged(String line) {

            }
        }, timeout);
        return sb.toString();
    }

    public void execCmd(String cmd, ShellUtil.OnStreamChangedListener listener) {
        execCmd(cmd, listener, -1);
    }

    public String execCmd(String cmd) {
        return execCmd(cmd, -1);
    }

    void wait(AdbStream stream, int timeout) throws IOException {
        if (timeout == -1) {
            while (!stream.isClosed()) {
                SystemClock.sleep(10);
            }
        } else {
            long start = System.currentTimeMillis();
            while (!stream.isClosed() && System.currentTimeMillis() - start < timeout) {
                SystemClock.sleep(10);
            }

            if (!stream.isClosed()) {
                stream.close();
            }
        }
    }

    public boolean isConnected() {
        return adbService != null && adbService.getConnection() != null && adbService.getConnection().isFine();
    }

}
