package com.tesseractmobile.pocketbot.activities.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;
import com.tesseractmobile.pocketbot.robot.DataStore;
import com.tesseractmobile.pocketbot.robot.RobotInfo;
import com.tesseractmobile.pocketbot.views.FirebaseRecyclerAdapter;
import com.tesseractmobile.pocketbot.views.RobotInfoViewHolder;

/**
 * Created by josh on 12/29/2015.
 */
public class RobotSelectionDialog extends DialogFragment implements DataStore.OnAuthCompleteListener {

    private RecyclerView mRobotRecyclerView;
    private OnRobotSelectedListener mOnRobotSelectedListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.robot_selector, null);

        mRobotRecyclerView = (RecyclerView) view.findViewById(R.id.rvRobots);
        mRobotRecyclerView.setHasFixedSize(true);
        mRobotRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //Setup list view after logging in
        DataStore.get().registerOnAuthCompleteListener(this);

        return new AlertDialog.Builder(getActivity())
                .setTitle("Select your robot")
                .setView(view)
                .create();
    }

    @Override
    public void onAuthComplete() {
        mRobotRecyclerView.setAdapter(new FirebaseRecyclerAdapter<RobotInfo, RobotInfoViewHolder>(RobotInfo.class, R.layout.robot_list_item, RobotInfoViewHolder.class, DataStore.get().getRobotListRef()) {
            @Override
            protected void populateViewHolder(final RobotInfoViewHolder viewHolder, final RobotInfo model, final int position) {
                viewHolder.robotName.setText(model.Name);
                viewHolder.robotName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PocketBotSettings.setRobotId(getContext(), model.Id);
                        dismiss();
                        if (mOnRobotSelectedListener != null) {
                            mOnRobotSelectedListener.onRobotSelected(model);
                        }
                    }
                });
            }
        });
    }

    /**
     * Register to be notified when a robot is selected
     * @param onRobotSelectedListener
     */
    public void setOnRobotSelectedListener(final OnRobotSelectedListener onRobotSelectedListener){
        mOnRobotSelectedListener = onRobotSelectedListener;
    }

    public interface OnRobotSelectedListener {
        /**
         * Called when robot is selected from the dialog
         * @param robotinfo
         */
        void onRobotSelected(final RobotInfo robotinfo);
    }
}
