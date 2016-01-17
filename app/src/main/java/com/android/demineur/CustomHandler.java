package com.android.demineur;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

/**
 * Custom Handler for timer update
 */
public class CustomHandler extends Handler {

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        TextView timer = (TextView) msg.obj;
        timer.setText(timer.getContext().getString(R.string.timer, msg.arg1, msg.arg2));
    }
}