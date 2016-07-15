package com.tesseractmobile.pocketbot.activities.fragments.facefragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;

import java.util.List;

/**
 * Created by josh on 1/24/2016.
 */
public class FaceListFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ListView listView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.face_selector, container);
        listView = (ListView) view.findViewById(R.id.lvFaces);
        //Add all emotions to the list view
        final ArrayAdapter<FaceInfo> adapter = new FaceArrayAdapter<FaceInfo>(getActivity(), android.R.layout.simple_list_item_1, FaceFragmentFactory.getFaceInfoList());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        return view;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Activity activity = getActivity();
        if(activity != null) {
            final FaceInfo faceInfo = (FaceInfo) listView.getItemAtPosition(position);
            PocketBotSettings.setSelectedFace(activity, faceInfo.id);
        }
    }

    private class FaceArrayAdapter<T> extends ArrayAdapter<FaceInfo> {
        public FaceArrayAdapter(final Activity activity, final int simple_list_item_1, final List<FaceInfo> faceInfoList) {
            super(activity, simple_list_item_1, faceInfoList);
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            final View view;
            final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if(convertView != null){
                view = convertView;
            } else {
                view = inflater.inflate(R.layout.face_list_item, null);
            }
            final TextView tvName = (TextView) view.findViewById(R.id.tvName);
            final TextView tvInfo = (TextView) view.findViewById(R.id.tvInfo);
            final ImageView ivIcon = (ImageView) view.findViewById(R.id.ivFaceIcon);
            final LinearLayout llLock = (LinearLayout) view.findViewById(R.id.llLock);
            final FaceInfo faceInfo = (FaceInfo) listView.getItemAtPosition(position);
            //view.setBackgroundResource(faceInfo.background);
            tvName.setText(faceInfo.name);
            tvInfo.setText(faceInfo.info);
            ivIcon.setImageResource(faceInfo.icon);

            if(faceInfo.locked){
                llLock.setVisibility(View.VISIBLE);
            } else {
                llLock.setVisibility(View.GONE);
            }

//            final ImageView imageView = (ImageView) view.findViewById(R.id.ivBackground);
//            imageView.setImageResource(faceInfo.background);

            return view;
        }
    }
}
