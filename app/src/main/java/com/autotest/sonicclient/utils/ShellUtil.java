package com.autotest.sonicclient.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class ShellUtil {
    private static final String TAG = "ShellUtil";
    private static MyThread myThread;
    private static String TERMINATOR = ">";
    private static int CMD_EXEC_TIMEOUT = 2000;

    public static String execCmd(String cmd) {
        return execCmd(cmd, CMD_EXEC_TIMEOUT);
    }

    public static String execCmd(String cmd, int timeout) {
        final StringBuilder sb = new StringBuilder();
        final StringBuilder err = new StringBuilder();
        execCmd(cmd, new OnStreamChangedListener() {
            @Override
            public void onStreamChanged(String line) {
                Log.d(TAG, "onStreamChanged: " + line);
                sb.append(line);
                sb.append("\n");
            }

            @Override
            public void onErrorStreamChanged(String line) {
                Log.e(TAG, "onErrorStreamChanged: " + line);
                err.append(line);
                err.append("\n");
            }
        }, timeout);
        stopCmd();
        return sb.append(err).toString();
    }

    public static void execCmd(String cmd, OnStreamChangedListener listener, int timeout) {
        LogUtil.d(TAG, "execCmd: " + cmd);
        myThread = new MyThread.Builder().setCmd(cmd).setTimeout(timeout).setTerminator(TERMINATOR).setListener(listener).create();
        myThread.setDaemon(true);
        myThread.start();
    }

    public static void stopCmd() {
        if (myThread != null) {
            myThread.interrupt();
        }
        if (myThread != null) {
            myThread.reset();
        }
    }

    public interface OnStreamChangedListener {
        void onStreamChanged(String line);

        void onErrorStreamChanged(String line);
    }

    static class MyThread extends Thread {
        String cmd;
        int timeout = 0;
        OnStreamChangedListener listener = null;
        String terminator;
        private BufferedReader bufferedStreamReader = null;
        private BufferedReader bufferedErrorReader = null;
        private Process process;

        public MyThread(Builder builder) {
            cmd = builder.cmd;
            timeout = builder.timeout;
            listener = builder.listener;
            terminator = builder.terminator;
        }

        public void execCmd() {
            process = null;
            try {
                process = Runtime.getRuntime().exec(this.cmd);
                InputStream inputStream = process.getInputStream();
                InputStream errorStream = process.getErrorStream();
                bufferedStreamReader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("GBK")));
                bufferedErrorReader = new BufferedReader(new InputStreamReader(errorStream, Charset.forName("GBK")));

                String line;
                if (null != this.listener) {
                    while (!isInterrupted() && null != (line = bufferedErrorReader.readLine())) {
                        this.listener.onErrorStreamChanged(line);
                        if (isInterrupted() || line.contains(this.terminator)) {
                            process.destroy();
                            break;
                        }
                    }

                    while (!isInterrupted() && null != (line = bufferedStreamReader.readLine())) {
                        this.listener.onStreamChanged(line);
                        if (isInterrupted() || line.contains(this.terminator)) {
                            process.destroy();
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, String.format("execCmd: IOException raise when execute command [%s]", this.cmd), e);
            }
            finally {
                if (process != null && process.isAlive()) {
                    process.destroy();
                }
                try {
                    if (bufferedStreamReader != null) {
                        bufferedStreamReader.close();
                    }
                    if (bufferedErrorReader != null) {
                        bufferedErrorReader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            super.run();
            execCmd();
        }

        @Override
        public synchronized void start() {
            super.start();
            if (this.timeout > 0){
                try {
                    join(this.timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                interrupt();
            }
        }

        public void reset() {
            if (!isInterrupted()) {
                interrupt();
            }
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }

        public static class Builder {
            public String cmd;
            public int timeout = 0;
            public OnStreamChangedListener listener;
            public String terminator;


            public Builder setCmd(String cmd) {
                this.cmd = cmd;
                return this;
            }

            /**
             * @param ms
             * @return
             */
            public Builder setTimeout(int ms) {
                if (ms < 0) {
                    return this;
                }
                    this.timeout = ms;
                return this;
            }

            public Builder setListener(OnStreamChangedListener listener) {
                this.listener = listener;
                return this;
            }

            public Builder setTerminator(String terminator) {
                this.terminator = terminator;
                return this;
            }

            public MyThread create() {
                return new MyThread(this);
            }
        }
    }
}
