package com.tesseractmobile.pocketbot.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by josh on 9/26/2015.
 */
public class RobotService extends Service{

    private final LocalBinder             BINDER                = new LocalBinder();

    @Override
    public IBinder onBind(final Intent intent) {
        return BINDER;
    }

    public class LocalBinder extends Binder {
        public RobotService getService() {
            return RobotService.this;
        }
    }
}
