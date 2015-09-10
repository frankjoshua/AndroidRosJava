package com.tesseractmobile.pocketbot.activities;

import java.util.UUID;

import android.os.Bundle;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class FirebaseFaceActivity extends GoogleFaceDetectActivity {

    private static final String CHILD_PATH = "chat";
    private Firebase mFirebaseRef;
    private String userId;
    boolean firstResponce;
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Read User Id
        userId = getPreferences(MODE_PRIVATE).getString("uuid", UUID.randomUUID().toString());
        //Save User Id
        getPreferences(MODE_PRIVATE).edit().putString("uuid", userId).commit();
        
        Firebase.setAndroidContext(this);
        mFirebaseRef = new Firebase("https://boiling-torch-4457.firebaseio.com/").child(CHILD_PATH);

        mFirebaseRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(final DataSnapshot snapshot) {
                Chat chat = null;
                for (final DataSnapshot childSnapshot: snapshot.getChildren()) {
                    chat = childSnapshot.getValue(Chat.class);
                }
                if(chat != null){
                    if(!chat.user.equals(userId)){
                        //Ignore first response
                        if(firstResponce){
                            say
                            (chat.text);
                        } else {
                            firstResponce = true;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(final FirebaseError error) {
            }

        });
    }

    @Override
    public void doTextInput(final String input) {
        final Chat chat = new Chat(userId, input);
        mFirebaseRef.push().setValue(chat);
    }
}
