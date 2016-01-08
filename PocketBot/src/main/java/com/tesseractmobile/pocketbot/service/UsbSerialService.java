package com.tesseractmobile.pocketbot.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.tesseractmobile.pocketbot.robot.BodyInterface;
import com.tesseractmobile.pocketbot.robot.Robot;
import com.tesseractmobile.pocketbot.robot.SensorData;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by josh on 11/18/2015.
 */
public class UsbSerialService extends BodyService implements Runnable, BodyInterface, SerialInputOutputManager.Listener {

    private static final String TAG = UsbSerialService.class.getSimpleName();
    private SerialInputOutputManager mSerialIoManager;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private boolean mErrorState = false;
    private AtomicBoolean mConnected = new AtomicBoolean(false);

    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    // call your method that cleans up and closes communication with the device
                    mSerialIoManager.stop();
                    mConnected.set(false);
                    error(0, "Usb Connection Lost");
                    unregisterReceiver(this);
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

    }

    private void startThread() {
        final Thread thread = new Thread(null, this, "DemoKit");
        thread.start();
    }

    @Override
    protected void bodyListenerRegistered() {
        startThread();
    }

    @Override
    public void sendObject(Object object) {

    }

    @Override
    public void sendJson(String json) {

    }

    @Override
    public void sendBytes(byte[] bytes) {
        //Check for error
        if(mErrorState || mConnected.get() == false){
            return;
        }
        //Send data to the Serial Manager
        Log.d("UsbSerialService", "Data " + bytes.length);
        mSerialIoManager.writeAsync(SensorData.wrapData(bytes));
    }

    @Override
    public void run() {

        while(true){
            if(mConnected.get() == false){
                connectUsb();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void connectUsb(){
        final UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        final List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        if(drivers.size() > 0) {
            final UsbSerialPort usbSerialPort = drivers.get(0).getPorts().get(0);
            UsbDeviceConnection connection = mUsbManager.openDevice(usbSerialPort.getDriver().getDevice());
            try {
                usbSerialPort.open(connection);
                usbSerialPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            } catch (IOException e) {
                //Just return and try again later
                mConnected.set(false);
                return;
            }
            mSerialIoManager = new SerialInputOutputManager(usbSerialPort, this);
            mExecutor.submit(mSerialIoManager);
            mConnected.set(true);
            mErrorState = false;
            bodyReady();

            //Listen for disconnect
            IntentFilter filter = new IntentFilter();
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            registerReceiver(mUsbReceiver, filter);
        }

    }

    @Override
    public void onNewData(byte[] data) {
        //throw new UnsupportedOperationException();
        try {
            String str = new String(data, "UTF-8");
            Robot.get().say(str);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRunError(Exception e) {
        //This probally means USB was disconected
        mErrorState = true;
    }
}
