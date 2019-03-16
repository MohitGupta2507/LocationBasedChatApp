package com.example.mohit.chatapp;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toolbar;

import com.facebook.login.widget.ProfilePictureView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Picasso;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatBox extends AppCompatActivity {

    TextView Username;
    CircleImageView ProfileImage;
    AppBarLayout appBarLayout;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_box);
        String Uid=getIntent().getExtras().get("Uid").toString();
        appBarLayout=(AppBarLayout)findViewById(R.id.appBar);
        //    android.support.v7.widget.Toolbar t =(android.support.v7.widget.Toolbar)findViewById(R.id.toolbar2);
      //  setSupportActionBar(t);
        Username=(TextView)findViewById(R.id.Username);
        ProfileImage =(CircleImageView)findViewById(R.id.UserImage);
        final DocumentReference documentReference=FirebaseFirestore.getInstance().collection("Users").document(Uid);
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    String name=documentSnapshot.getString("name");
                    String picture=documentSnapshot.getString("picture");
                    Username.setText(name);
                    Picasso.get().load(picture).into(ProfileImage);
            }
        });
    }
}
