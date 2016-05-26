package com.tesseractmobile.pocketbot.robot;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.StringBufferInputStream;

/**
 * Created by josh on 5/23/16.
 */
public class AuthData {

    final private String mUid;
    final private String mDisplayName;
    final private String mProfileImageUrl;

    public AuthData(final GoogleSignInAccount googleSignInAccount) {
        mUid = googleSignInAccount.getId();
        mDisplayName = googleSignInAccount.getDisplayName();
        mProfileImageUrl = googleSignInAccount.getPhotoUrl().toString();
    }

    /**
     *
     * @return user id
     */
    public String getUid() {
        return mUid;
    }

    /**
     *
     * @return display name
     */
    public String getDisplayName() {
        return mDisplayName;
    }

    /**
     *
     * @return url to profile image
     */
    public String getProfileImageURL() {
        return mProfileImageUrl;
    }
}
