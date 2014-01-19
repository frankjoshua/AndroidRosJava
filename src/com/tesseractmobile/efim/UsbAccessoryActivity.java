package com.tesseractmobile.efim;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;

public class UsbAccessoryActivity extends Activity {

    private static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";
    
    private final BroadcastReceiver usbReciever = new BroadcastReceiver(){

        @Override
        public void onReceive(final Context context, final Intent intent) {
            view.setBackgroundColor(Color.CYAN);
        }
        
    };
    
    private View view;

    private UsbManager mUsbManager;

    private PendingIntent mPermissionIntent;
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                ACTION_USB_PERMISSION), 0);
        final IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(usbReciever, filter);
        
        view = new View(this);
        
        view.setBackgroundColor(Color.WHITE);
        
        setContentView(view);
    }

}
