package com.autotest.sonicclient.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AppCompatActivity;

public class MDialog extends AlertDialog.Builder {
    public MDialog(Context context) {
        super(context);
    }

    public MDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public void checkAdbStatus(DialogInterface.OnClickListener pListener) {
        setTitle("连接失败")
                .setMessage("请在控制台下发命令:\n adb tcpip 5555")
                .setPositiveButton("已下发", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pListener.onClick(dialog, which);
                    }
                })
                .setNegativeButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((Activity) getContext()).finish(); // 关闭当前 Activity
                    }
                })
                .create()
                .show();
    }
}
