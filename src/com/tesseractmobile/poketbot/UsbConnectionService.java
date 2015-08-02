package com.tesseractmobile.poketbot;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;

import com.tesseractmobile.poketbot.robot.CommandContract;
import com.tesseractmobile.poketbot.robot.RobotCommand;
import com.tesseractmobile.poketbot.robot.RobotCommandInterface;
import com.tesseractmobile.poketbot.robot.RobotEvent;
import com.tesseractmobile.poketbot.robot.RobotEventListener;

public class UsbConnectionService extends Service implements RobotCommandInterface, Runnable{


    private static final String     ACTION_USB_PERMISSION = "com.tesseractmobile.poketbot.action.USB_PERMISSION";
    private UsbManager              mUsbManager;

    private PendingIntent           mPermissionIntent;

    private boolean                 mPermissionRequestPending;

    private ParcelFileDescriptor    mFileDescriptor;

    private UsbAccessory            mAccessory;

    private FileInputStream         mInputStream;

    private FileOutputStream        mOutputStream;

    private final BroadcastReceiver usbReciever           = new BroadcastReceiver() {

                                                              @Override
                                                              public void onReceive(final Context context, final Intent intent) {
                                                                  final String action = intent.getAction();
                                                                  //log(action);
                                                                  if (ACTION_USB_PERMISSION.equals(action)) {
                                                                      final UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);

                                                                      if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                                                          if(accessory != null){
                                                                              //call method to set up accessory communication
                                                                              log("Accessor null");
                                                                          }
                                                                      } else {
                                                                          log("Permission denied");
                                                                      }
                                                                  } else {
                                                                     if(robotEventListener != null){
                                                                         robotEventListener.onRobotEvent(RobotEvent.createDisconectEvent());
                                                                     }
                                                                  }
                                                              }

                                                          };

    private final LocalBinder             binder                = new LocalBinder();
    private RobotEventListener robotEventListener;
    private final Object listenerLock = new Object();
    private final LinkedList<Command> commandQueue = new LinkedList<Command>();
    private final AtomicBoolean run = new AtomicBoolean(true);
    private long lastCommandTime;
    
    @Override
    public IBinder onBind(final Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public RobotCommandInterface getService() {
            return UsbConnectionService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        final IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        registerReceiver(usbReciever, filter);

        //connectAccessory();
    }

    @Override
    public void onDestroy() {
        closeAccessory();
        unregisterReceiver(usbReciever);
        run.set(false);
        super.onDestroy();
    }

    private void closeAccessory() {

        try {
            if (mFileDescriptor != null) {
                mFileDescriptor.close();
            }
        } catch (final IOException e) {
        } finally {
            mFileDescriptor = null;
            mAccessory = null;
        }
    }
    
    /**
     * 
     */
    private void connectAccessory() {
        final UsbAccessory[] accessories = mUsbManager.getAccessoryList();
        final UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory != null) {
            if (mUsbManager.hasPermission(accessory)) {
                openAccessory(accessory);
            } else {
                synchronized (mUsbManager) {
                    if (!mPermissionRequestPending) {
                        log("Request Permission");
                        mUsbManager.requestPermission(accessory, mPermissionIntent);
                        mPermissionRequestPending = true;
                    }
                }
            }
        } else {
            log("Accessory == null");
        }
    }

    private void openAccessory(final UsbAccessory accessory) {
        mFileDescriptor = mUsbManager.openAccessory(accessory);
        if (mFileDescriptor != null) {
            mAccessory = accessory;
            final FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
            startThread();
            log("accessory opened");
        } else {
            final String name = accessory != null ? accessory.toString() : "null";
            log("accessory open fail: " + name);
        }
    }

    /**
     * 
     */
    protected void startThread() {
        lastCommandTime = System.currentTimeMillis();
        final Thread thread = new Thread(null, this, "DemoKit");
        thread.start();
    }

    @Override
    public boolean sendCommand(final byte command, final byte target, final int value, final int time) {
        final byte[] buffer = new byte[3];

        if (value > 255 || value < 0){
            log("Value out of range " + value);
            return false;
        }
        

        buffer[0] = target;
        buffer[1] = command;
        buffer[2] = (byte) value;
       // buffer[3] = (byte) time;
        if (mOutputStream != null && buffer[1] != -1) {
            try {
                mOutputStream.write(buffer);
                log("Command: " + command + " Target: " + target + " Value: " + value);
                return true;
            } catch (final IOException e) {
                log("write failed:" + e.getMessage());
            }
        } else {
            
            //Try to open Accessory
            if(mAccessory != null){
                log("Send Command Failed: mOutputStream is null");
                openAccessory(mAccessory);
            } else {
                log("Send Command Failed: mAccessory is null");
                connectAccessory();
            }
        }
        return false;
    }

    private void log(final String string) {
        final RobotEvent robotEvent = RobotEvent.createErrorEvent(string);
        final RobotEventListener eventListener = robotEventListener;
        if(eventListener != null){
            eventListener.onRobotEvent(robotEvent);
        }
    }

    @Override
    public void registerRobotEventListener(final RobotEventListener robotEventListener) {
        synchronized (listenerLock) {
            this.robotEventListener = robotEventListener; 
        } 
    }

    @Override
    public void unregisterRobotEventListener(final RobotEventListener robotEventListener) {
        synchronized (listenerLock) {
            this.robotEventListener = robotEventListener; 
        } 
    }

    @Override
    public boolean sendCommand(final RobotCommand robotCommand) {
        switch (robotCommand.getCommandType()) {
        case NOD:
            //nod();
            backup();
            return true;
        case SHAKE:
            //shake();
            backup();
            break;
        case LEFT:
            left();
            break;
        case RIGHT:
            right();
            break;
        }
        return false;
    }


    private void right() {
        synchronized (commandQueue) {
            commandQueue.add(new Command(CommandContract.TAR_MOTOR_LEFT, CommandContract.CMD_RIGHT, 30, 50));
            commandQueue.add(new Command(CommandContract.TAR_MOTOR_RIGHT, CommandContract.CMD_PAUSE, 0, 50));
            commandQueue.add(new Command(CommandContract.TAR_MOTOR_LEFT, CommandContract.CMD_FORWARD, 0, 50));
            log("Adding Commands. Queue size: " + commandQueue.size());
        }
    }

    private void left() {
        synchronized (commandQueue) {
            commandQueue.add(new Command(CommandContract.TAR_MOTOR_LEFT, CommandContract.CMD_LEFT, 30, 50));
            commandQueue.add(new Command(CommandContract.TAR_MOTOR_RIGHT, CommandContract.CMD_PAUSE, 0, 50));
            commandQueue.add(new Command(CommandContract.TAR_MOTOR_LEFT, CommandContract.CMD_FORWARD, 0, 50));
            log("Adding Commands. Queue size: " + commandQueue.size());
        }
    }

    private void backup() {
        synchronized (commandQueue) {
            commandQueue.add(new Command(CommandContract.TAR_MOTOR_LEFT, CommandContract.CMD_BACKWARD, 30, 50));
            commandQueue.add(new Command(CommandContract.TAR_MOTOR_RIGHT, CommandContract.CMD_PAUSE, 0, 500));
            commandQueue.add(new Command(CommandContract.TAR_MOTOR_LEFT, CommandContract.CMD_FORWARD, 0, 50));
            log("Adding Commands. Queue size: " + commandQueue.size());
        }
    }

    /**
     * 
     */
    private void shake() {
        synchronized (commandQueue) {
            commandQueue.add(new Command(CommandContract.TAR_SERVO_PAN, (byte) 0, 175, 300));
            commandQueue.add(new Command(CommandContract.TAR_SERVO_PAN, (byte) 0, 100, 300));
            commandQueue.add(new Command(CommandContract.TAR_SERVO_PAN, (byte) 0, 175, 300));
            commandQueue.add(new Command(CommandContract.TAR_SERVO_PAN, (byte) 0, 100, 300));
            commandQueue.add(new Command(CommandContract.TAR_SERVO_PAN, (byte) 0, 175, 300));
            commandQueue.add(new Command(CommandContract.TAR_SERVO_PAN, (byte) 0, 100, 150));
            commandQueue.add(new Command(CommandContract.TAR_SERVO_PAN, (byte) 0, 127, 0));
            log("Adding Commands. Queue size: " + commandQueue.size());
        }
    }

    /**
     * 
     */
    private void nod() {
        synchronized (commandQueue) {
            commandQueue.add(new Command(CommandContract.TAR_SERVO_TILT, (byte) 0, 175, 300));
            commandQueue.add(new Command(CommandContract.TAR_SERVO_TILT, (byte) 0, 100, 300));
            commandQueue.add(new Command(CommandContract.TAR_SERVO_TILT, (byte) 0, 175, 300));
            commandQueue.add(new Command(CommandContract.TAR_SERVO_TILT, (byte) 0, 100, 300));
            commandQueue.add(new Command(CommandContract.TAR_SERVO_TILT, (byte) 0, 175, 300));
            commandQueue.add(new Command(CommandContract.TAR_SERVO_TILT, (byte) 0, 100, 150));
            commandQueue.add(new Command(CommandContract.TAR_SERVO_TILT, (byte) 0, 127, 0));
            log("Adding Commands. Queue size: " + commandQueue.size());
        }
    }

    @Override
    public void reconnectRobot() {
        closeAccessory();
        connectAccessory();
    }

    @Override
    public void run() {
        while (run.get()) {
            final long currentTimeMillis = System.currentTimeMillis();
            final long timeElapsed = currentTimeMillis - lastCommandTime;
            lastCommandTime = currentTimeMillis;
            //Read input Stream and Clear
//            final InputStream inputStream = mInputStream;
//            if (inputStream != null) {
//                final byte[] buffer = new byte[16384];
//                int ret = 0;
//                //log("Clearing Input Stream");
//                if (ret >= 0) {
//                    try {
//                        //Clear Buffer      
//                        ret = inputStream.read(buffer);
//                        //log("Clearing Input Stream: " + ret);
//                    } catch (final IOException e1) {
//                        ret = -1;
//                    }
//                }
//            }
            
            synchronized (commandQueue) {
                if (commandQueue.isEmpty() == false) {
                    //log("Running Commands");
                    final Command command = commandQueue.peek();
                    if(command.cmd != CommandContract.CMD_PAUSE){
                        sendCommand(command.cmd, command.tar, command.val, command.delay);
                    }
                    command.delay -= timeElapsed;
                    if (command.delay <= 0) {   
                        commandQueue.poll();
                    }
                } else {
                    //log("No Commands");
                }
            }
            try {
                Thread.sleep(1);
            } catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        log("Thread Exit");
    }
   

    public class Command {
        public byte cmd;
        public byte tar;
        public int val;
        public int delay;
        
        public Command(final byte tar, final byte cmd, final int val, final int delay) {
            this.cmd = cmd;
            this.tar = tar;
            this.val = val;
            this.delay = delay;
        }
        
        public Command() {
            
        }
        
    }
}
