package com.tesseractmobile.pocketbot.activities;

import android.util.Log;

import com.tesseractmobile.pocketbot.robot.Constants;
import com.tesseractmobile.pocketbot.robot.RemoteControl;
import com.tesseractmobile.pocketbot.robot.SensorData;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by josh on 1/16/2016.
 */
public class KeepAliveThread extends Thread{

    final private KeepAliveListener mKeepAliveListener;
    final private AtomicBoolean mRunning = new AtomicBoolean(false);

    public KeepAliveThread(KeepAliveListener keepAliveListener) {
        super("KeepAliveThread");
        mKeepAliveListener = keepAliveListener;

        Thread netThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //Keep Arduino awake
                    if(mRunning.get()) {
                        if(RemoteControl.get().getLag() > 200 || executeCommand() ==  false){
                            mKeepAliveListener.onInternetTimeout();
                            Log.e("Thread", "Connection Lost, sending stop command!");
                        }
                    }
                }
            }
        });

        netThread.start();
        start();
    }

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Keep Arduino awake
            if(mRunning.get()) {
                mKeepAliveListener.onHeartBeat();
                if (Constants.LOGGING) {
                    Log.d("Thread", "Triggering a sensor send");
                }
            }

        }
    }

    /**
     * Pings internet and return true if sucessful
     * Also true if error
     * @return
     */
    private boolean executeCommand(){
        Runtime runtime = Runtime.getRuntime();
        try
        {
            Process  mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int mExitValue = mIpAddrProcess.waitFor();
            mIpAddrProcess.destroy();
            if(mExitValue==0){
                return true;
            }else{
                return false;
            }
        }
        catch (InterruptedException ignore)
        {
            ignore.printStackTrace();
            System.out.println(" Exception:"+ignore);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println(" Exception:"+e);
        }
        return true;
    }

    public void startThread() {
        Log.d("Thread", "Starting KeepAliveThread");
        mRunning.set(true);
    }

    public void stopThread() {
        Log.d("Thread", "Stopping KeepAliveThread");
        mRunning.set(false);
    }

    public interface KeepAliveListener {
        /**
         * Send on a regular basis to wake up robot
         */
        void onHeartBeat();

        /**
         * Called if internet ping times out
         */
        void onInternetTimeout();
    }
}
