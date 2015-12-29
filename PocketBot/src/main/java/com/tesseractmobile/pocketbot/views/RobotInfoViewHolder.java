package com.tesseractmobile.pocketbot.views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tesseractmobile.pocketbot.R;

/**
 * Created by josh on 12/29/2015.
 */
public class RobotInfoViewHolder extends RecyclerView.ViewHolder {
    public TextView robotName;

    public RobotInfoViewHolder(final View itemView) {
        super(itemView);
        robotName = (TextView) itemView.findViewById(R.id.tvRobotName);
    }
}
