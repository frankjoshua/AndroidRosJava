package com.tesseractmobile.pocketbot.activities.fragments.facefragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.tesseractmobile.pocketbot.R;
import com.tesseractmobile.pocketbot.activities.PocketBotSettings;

import java.util.ArrayList;
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
        listView.setAdapter(new ArrayAdapter<FaceInfo>(getActivity(), android.R.layout.simple_list_item_1, FaceFragmentFactory.getFaceInfoList()));
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

}
