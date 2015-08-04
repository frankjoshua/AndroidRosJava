package com.tesseractmobile.pocketbot.activities;

import java.util.UUID;

import android.os.Bundle;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class FirebaseFaceActivity extends OpenCVFace {

    private static final String CHILD_PATH = "message";
    private Firebase mFirebaseRef;
    private final String userId = UUID.randomUUID().toString();
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        mFirebaseRef = new Firebase("https://boiling-torch-4457.firebaseio.com/");

        mFirebaseRef.child(CHILD_PATH).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(final DataSnapshot snapshot) {
                if(snapshot.getValue() instanceof Chat){
                    final Chat chat = (Chat) snapshot.getValue();
                    if(chat != null){
                        if(!chat.user.equals(userId)){
                            say(chat.text); 
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
    protected void onTextInput(final String input) {
        final Chat chat = new Chat(userId, input);
        mFirebaseRef.child(CHILD_PATH).setValue(chat);
    }

    private class Chat {
        public String text;
        public String user;
        
        Chat(final String user, final String text){
            this.user = user;
            this.text = text;
        }
    }
}
