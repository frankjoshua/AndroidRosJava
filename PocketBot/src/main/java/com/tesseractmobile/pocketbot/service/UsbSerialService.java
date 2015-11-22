package com.tesseractmobile.pocketbot.service;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.tesseractmobile.pocketbot.robot.BodyInterface;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by josh on 11/18/2015.
 */
public class UsbSerialService extends BodyService implements Runnable, BodyInterface, SerialInputOutputManager.Listener {

    private static final String TAG = UsbSerialService.class.getSimpleName();
    private SerialInputOutputManager mSerialIoManager;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate() {
        super.onCreate();

        final UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        final List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        final UsbSerialPort usbSerialPort = drivers.get(0).getPorts().get(0);
        UsbDeviceConnection connection = mUsbManager.openDevice(usbSerialPort.getDriver().getDevice());
        try {
            usbSerialPort.open(connection);
            usbSerialPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (IOException e) {

        }
        mSerialIoManager = new SerialInputOutputManager(usbSerialPort, this);
        mExecutor.submit(mSerialIoManager);
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
        mSerialIoManager.writeAsync(bytes);
    }

    @Override
    public void run() {
        while(true){

        }
    }

    @Override
    public void onNewData(byte[] data) {

    }

    @Override
    public void onRunError(Exception e) {

    }
}
