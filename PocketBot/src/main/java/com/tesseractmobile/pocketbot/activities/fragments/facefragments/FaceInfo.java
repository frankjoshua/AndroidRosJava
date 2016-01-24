package com.tesseractmobile.pocketbot.activities.fragments.facefragments;

/**
 * Created by josh on 1/24/2016.
 */
class FaceInfo {
    final public int id;
    final public String name;

    FaceInfo(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
