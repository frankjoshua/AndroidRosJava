package com.tesseractmobile.pocketbot.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tesseractmobile.ble.BleDevicesScanner;
import com.tesseractmobile.ble.BleManager;
import com.tesseractmobile.ble.BleUtils;
import com.tesseractmobile.pocketbot.robot.BodyConnectionListener;
import com.tesseractmobile.pocketbot.robot.BodyInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by josh on 8/29/2015.
 */
public class BluetoothService extends Service implements BleManager.BleManagerListener, BodyInterface {

    private static final String TAG = BluetoothService.class.getName();

    public static final String UUID_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String UUID_RX = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String UUID_TX = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String UUID_DFU = "00001530-1212-EFDE-1523-785FEABCD123";

    final private IBinder binder = new LocalBinder();
    private BleManager mBleManager;
    private BleDevicesScanner mScanner;
    private ArrayList<BluetoothDeviceData> mScannedDevices;

    private BodyConnectionListener mBodyConnectionListener;

    @Override
    public void onCreate() {
        super.onCreate();
        mBleManager = BleManager.getInstance(this);
        mBleManager.setBleListener(this);
        startScan(null, null);
    }

    private void startScan(final UUID[] servicesToScan, final String deviceNameToScanFor) {
        Log.d(TAG, "startScan");

        // Stop current scanning (if needed)
        stopScanning();

        // Configure scanning
        BluetoothAdapter bluetoothAdapter = BleUtils.getBluetoothAdapter(getApplicationContext());
        if (BleUtils.getBleStatus(this) != BleUtils.STATUS_BLE_ENABLED) {
            Log.w(TAG, "startScan: BluetoothAdapter not initialized or unspecified address.");
        } else {
            mScanner = new BleDevicesScanner(bluetoothAdapter, servicesToScan, new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    final String deviceName = device.getName();
                    //Log.d(TAG, "Discovered device: " + (deviceName != null ? deviceName : "<unknown>"));

                    BluetoothDeviceData previouslyScannedDeviceData = null;
                    if (deviceNameToScanFor == null || (deviceName != null && deviceName.equalsIgnoreCase(deviceNameToScanFor))) {       // Workaround for bug in service discovery. Discovery filtered by service uuid is not working on Android 4.3, 4.4
                        if (mScannedDevices == null) mScannedDevices = new ArrayList<>();       // Safeguard

                        // Check that the device was not previously found
                        for (BluetoothDeviceData deviceData : mScannedDevices) {
                            if (deviceData.device.getAddress().equals(device.getAddress())) {
                                previouslyScannedDeviceData = deviceData;
                                break;
                            }
                        }

                        BluetoothDeviceData deviceData;
                        if (previouslyScannedDeviceData == null) {
                            // Add it to the mScannedDevice list
                            deviceData = new BluetoothDeviceData();
                            mScannedDevices.add(deviceData);
                            mBodyConnectionListener.onBluetoothDeviceFound();
                        } else {
                            deviceData = previouslyScannedDeviceData;
                        }

                        deviceData.device = device;
                        deviceData.rssi = rssi;
                        deviceData.scanRecord = scanRecord;
                        decodeScanRecords(deviceData);

                    }
                }
            });

            // Start scanning
            mScanner.start();
        }

    }

    private void stopScanning() {
        // Stop scanning
        if (mScanner != null) {
            mScanner.stop();
            mScanner = null;
        }

    }

    private void decodeScanRecords(BluetoothDeviceData deviceData) {
        // based on http://stackoverflow.com/questions/24003777/read-advertisement-packet-in-android
        final byte[] scanRecord = deviceData.scanRecord;

        ArrayList<UUID> uuids = new ArrayList<>();
        byte[] advertisedData = Arrays.copyOf(scanRecord, scanRecord.length);
        int offset = 0;
        deviceData.type = BluetoothDeviceData.kType_Unknown;

        // Check if is an iBeacon ( 0x02, 0x0x1, a flag byte, 0x1A, 0xFF, manufacturer (2bytes), 0x02, 0x15)
        final boolean isBeacon = advertisedData[0] == 0x02 && advertisedData[1] == 0x01 && advertisedData[3] == 0x1A && advertisedData[4] == (byte) 0xFF && advertisedData[7] == 0x02 && advertisedData[8] == 0x15;

        // Check if is an URIBeacon
        final byte[] kUriBeaconPrefix = {0x03, 0x03, (byte) 0xD8, (byte) 0xFE};
        final boolean isUriBeacon = Arrays.equals(Arrays.copyOf(scanRecord, kUriBeaconPrefix.length), kUriBeaconPrefix) && advertisedData[5] == 0x16 && advertisedData[6] == kUriBeaconPrefix[2] && advertisedData[7] == kUriBeaconPrefix[3];

        if (isBeacon) {
            deviceData.type = BluetoothDeviceData.kType_Beacon;

            // Read uuid
            offset = 9;
            UUID uuid = BleUtils.getUuidFromByteArrayBigEndian(Arrays.copyOfRange(scanRecord, offset, offset + 16));
            uuids.add(uuid);
            offset += 16;

            // Skip major minor
            offset += 2 * 2;   // major, minor

            // Read txpower
            final int txPower = advertisedData[offset++];
            deviceData.txPower = txPower;
        } else if (isUriBeacon) {
            deviceData.type = BluetoothDeviceData.kType_UriBeacon;

            // Read txpower
            final int txPower = advertisedData[9];
            deviceData.txPower = txPower;
        } else {
            // Read standard advertising packet
            while (offset < advertisedData.length - 2) {
                // Length
                int len = advertisedData[offset++];
                if (len == 0) break;

                // Type
                int type = advertisedData[offset++];
                if (type == 0) break;

                // Data
//            Log.d(TAG, "record -> lenght: " + length + " type:" + type + " data" + data);

                switch (type) {
                    case 0x02: // Partial list of 16-bit UUIDs
                    case 0x03: {// Complete list of 16-bit UUIDs
                        while (len > 1) {
                            int uuid16 = advertisedData[offset++] & 0xFF;
                            uuid16 |= (advertisedData[offset++] << 8);
                            len -= 2;
                            uuids.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", uuid16)));
                        }
                        break;
                    }

                    case 0x06:          // Partial list of 128-bit UUIDs
                    case 0x07: {        // Complete list of 128-bit UUIDs
                        while (len >= 16) {
                            try {
                                // Wrap the advertised bits and order them.
                                UUID uuid = BleUtils.getUuidFromByteArraLittleEndian(Arrays.copyOfRange(advertisedData, offset, offset + 16));
                                uuids.add(uuid);

                            } catch (IndexOutOfBoundsException e) {
                                Log.e(TAG, "BlueToothDeviceFilter.parseUUID: " + e.toString());
                            } finally {
                                // Move the offset to read the next uuid.
                                offset += 16;
                                len -= 16;
                            }
                        }
                        break;
                    }

                    case 0x0A: {   // TX Power
                        final int txPower = advertisedData[offset++];
                        deviceData.txPower = txPower;
                        break;
                    }

                    default: {
                        offset += (len - 1);
                        break;
                    }
                }
            }

            // Check if Uart is contained in the uuids
            boolean isUart = false;
            for (UUID uuid : uuids) {
                if (uuid.toString().equalsIgnoreCase(UUID_SERVICE)) {
                    isUart = true;
                    break;
                }
            }
            if (isUart) {
                deviceData.type = BluetoothDeviceData.kType_Uart;
            }
        }

        deviceData.uuids = uuids;
    }

    /**
     * Listen for body connection events
     * @param bodyConnectionListener
     */
    public BodyInterface registerBodyConnectionListener(final BodyConnectionListener bodyConnectionListener){
        this.mBodyConnectionListener = bodyConnectionListener;
        return this;
    }

    /**
     * Stop listening for body connection events
     * @param bodyConnectionListener
     */
    public void unregisterBodyConnectionListener(final BodyConnectionListener bodyConnectionListener){
        this.mBodyConnectionListener = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onConnecting() {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onServicesDiscovered() {

    }

    @Override
    public void onDataAvailable(BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void onDataAvailable(BluetoothGattDescriptor descriptor) {

    }

    @Override
    public void onReadRemoteRssi(int rssi) {

    }

    public class LocalBinder extends Binder {
        public BluetoothService getService(){
            return BluetoothService.this;
        }
    }

    private class BluetoothDeviceData {
        public BluetoothDevice device;
        public int rssi;
        public byte[] scanRecord;

        // Decoded scan record (update R.array.scan_devicetypes if this list is modified)
        public static final int kType_Unknown = 0;
        public static final int kType_Uart = 1;
        public static final int kType_Beacon = 2;
        public static final int kType_UriBeacon = 3;

        public int type;
        public int txPower;
        public ArrayList<UUID> uuids;
    }
}
