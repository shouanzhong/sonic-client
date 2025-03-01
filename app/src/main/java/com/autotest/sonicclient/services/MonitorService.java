package com.autotest.sonicclient.services;

import com.autotest.sonicclient.interfaces.CustomService;

@CustomService
public class MonitorService {
    Status status = new Status();

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public static class Status {
        boolean isRunning = false;
        StatusListener statusListener;

        public boolean isRunning() {
            return statusListener.isRunning();
        }

        public void setRunning(boolean running) {
            isRunning = running;
        }

        public void setListener(StatusListener listener) {
            this.statusListener = listener;
        }
    }

    public static interface StatusListener {
        boolean isRunning();
    }
}
