package com.tesseractmobile.pocketbot.activities.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;
import com.tesseractmobile.pocketbot.robot.DataStore;
import com.tesseractmobile.pocketbot.robot.RobotInfo;
import com.tesseractmobile.pocketbot.views.FirebaseRecyclerAdapter;
import com.tesseractmobile.pocketbot.views.RobotInfoViewHolder;

/**
 * Created by josh on 12/29/2015.
 */
public class RobotSelectionDialog extends DialogFragment implements DataStore.OnAuthCompleteListener, View.OnClickListener {

    private RecyclerView mRobotRecyclerView;
    private OnRobotSelectedListener mOnRobotSelectedListener;
    private View mSignInView;
    private View.OnClickListener mSignInOnClickListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.robot_selector, null);

        mSignInView = view.findViewById(R.id.signInLayout);
        mSignInView.findViewById(R.id.sign_in_button).setOnClickListener(this);

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
        //Hide sign in
        mSignInView.setVisibility(View.GONE);
        //Set adapter
        final String currentRobotId = PocketBotSettings.getRobotId(getContext());
        final Firebase userListRef = DataStore.get().getUserListRef().child(DataStore.ROBOTS);
        mRobotRecyclerView.setAdapter(new FirebaseRecyclerAdapter<RobotInfo.Settings, RobotInfoViewHolder>(RobotInfo.Settings.class, R.layout.robot_list_item, RobotInfoViewHolder.class, userListRef) {
            @Override
            protected void populateViewHolder(final RobotInfoViewHolder viewHolder, final RobotInfo.Settings model, final int position) {
                viewHolder.robotName.setText(model.prefs.robot_name);

                String status = "Robot Status: ";
                final boolean isCurrentRobot = currentRobotId.equals(model.prefs.robot_id);
                if(model.isConnected){
                    viewHolder.robotName.setAlpha(1);
                    viewHolder.robotStatus.setAlpha(1);
                    viewHolder.btnDelete.setVisibility(View.INVISIBLE);
                    status += "Online";
                } else {
                    viewHolder.robotName.setAlpha(.5f);
                    viewHolder.robotStatus.setAlpha(.5f);
                    viewHolder.btnDelete.setVisibility(View.VISIBLE);
                    status += "Offline";
                }

                if(isCurrentRobot){
                    viewHolder.mainLayout.setBackgroundColor(Color.parseColor("#444400ff"));
                    viewHolder.btnDelete.setVisibility(View.INVISIBLE);
                } else {
                    viewHolder.mainLayout.setBackgroundColor(Color.parseColor("#44000000"));
                    viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DataStore.get().deleteRobot(model.prefs.robot_id);
                        }
                    });
                }

                viewHolder.robotStatus.setText(status);

                viewHolder.mainLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
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
     * Set the click listener to respond to sign in
     * R.layout.signInLayout
     * @param onClickListener
     */
    public void setSignInOnClickListener(final View.OnClickListener onClickListener){
        mSignInOnClickListener = onClickListener;
    }

    /**
     * Register to be notified when a robot is selected
     * @param onRobotSelectedListener
     */
    public void setOnRobotSelectedListener(final OnRobotSelectedListener onRobotSelectedListener){
        mOnRobotSelectedListener = onRobotSelectedListener;
    }

    @Override
    public void onClick(View view) {
        if(mSignInOnClickListener != null){
            ///Hide the button
            mSignInView.findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            //Pass on the click
            mSignInOnClickListener.onClick(view);
        }
    }

    public interface OnRobotSelectedListener {
        /**
         * Called when robot is selected from the dialog
         * @param robotinfo
         */
        void onRobotSelected(final RobotInfo.Settings robotinfo);
    }
}
