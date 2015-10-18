package com.tesseractmobile.pocketbot.activities.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tesseractmobile.pocketbot.R;

import java.util.List;

/**
 * Created by josh on 10/18/2015.
 */
public class PreviewFragment extends CallbackFragment {

    private ListView mTextListView;

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.text_preview, null);
        mTextListView = (ListView) view.findViewById(R.id.textList);
        return view;
    }

    public ListView getListView(){
        return mTextListView;
    }
}
