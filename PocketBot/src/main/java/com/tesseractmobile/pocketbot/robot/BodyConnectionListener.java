package com.tesseractmobile.pocketbot.robot;

import com.tesseractmobile.pocketbot.service.BluetoothService;

/**
 * Created by josh on 8/29/2015.
 */
public interface BodyConnectionListener {
    void onBluetoothDeviceFound();

    void onError(int i, String connected);

    void onBodyConnected(BodyInterface bluetoothService);
}
