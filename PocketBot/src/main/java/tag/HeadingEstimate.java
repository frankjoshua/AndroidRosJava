package tag;

import android.os.SystemClock;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by josh on 9/30/2015.
 */
public class HeadingEstimate {

    private static long DATA_TIMEOUT = 1000;
    private Queue<HeadingData> mDataQueue = new LinkedList<HeadingData>();
    private int mLastEstimateHeading = 0;

    public synchronized void newData(int heading, double distanceChange) {
        //Simplify heading
        if(heading > 315 && heading <= 45){
            heading = 0;
        } else if(heading > 45 && heading <= 135){
            heading = 90;
        } else if(heading > 135 && heading <= 225){
            heading = 180;
        } else if(heading > 225 && heading <= 315){
            heading = 270;
        }
        if(distanceChange > 0){
            heading += 180;
            if(heading > 360){
                heading -= 360;
            }
        }
        final long timeStamp = SystemClock.uptimeMillis();
        mDataQueue.add(new HeadingData(heading, (int) (Math.abs(distanceChange) / 0.1d), timeStamp));

        //Remove stale data
        Iterator<HeadingData> iterator = mDataQueue.iterator();
        while(iterator.hasNext()){
            final HeadingData headingData = iterator.next();
            if(timeStamp - headingData.mTimeStamp > DATA_TIMEOUT){
                iterator.remove();
            }
        }
    }

    public synchronized int getHeadingEstimate() {
        int dataUnitCount = 0;
        int headingDataSum = 0;
        for(HeadingData headingData : mDataQueue){
            if(headingData.mDistanceChange > dataUnitCount){
                dataUnitCount = headingData.mDistanceChange;
                headingDataSum = headingData.mHeading;
            }
        }
        if(dataUnitCount == 0){
            //Catch divide by 0 error
            return mLastEstimateHeading;
        }
        mLastEstimateHeading = headingDataSum;
        return mLastEstimateHeading;
    }

    private class HeadingData {
        final private int mHeading;
        final private int mDistanceChange;
        final private long mTimeStamp;

        public HeadingData(final int heading, final int distanceChange, final long timeStamp) {
            mHeading = heading;
            mDistanceChange = distanceChange;
            mTimeStamp = timeStamp;
        }
    }
}
