package com.example.mohit.chatapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.ParseException;
import android.net.Uri;
import android.service.autofill.SaveCallback;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    CallbackManager mCallbackManager;
    LinearLayout fb;
    LoginButton loginButton;
    NewBroadCast broadCast;
    DocumentReference firebaseFirestore;
    FirebaseUser fbUser;
    StorageReference storageReference;
    private FirebaseAuth mAuth;

    public static Bitmap getFacebookProfilePicture(String userID) throws IOException {

        URL imageUrl;
        imageUrl = new URL("https://graph.facebook.com/" + userID + "/picture?type=large");
        Bitmap bitmap = BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream());

        return bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        fbUser = mAuth.getCurrentUser();

//        storageReference=FirebaseStorage.getInstance().getReference().child("Profile").child(FirebaseAuth.getInstance().getUid().toString());
        broadCast = new NewBroadCast();


        LoginManager.getInstance().logOut();

        //Our custom Facebook button
        fb = (LinearLayout) findViewById(R.id.fb);

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("TAG", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }


            @Override
            public void onCancel() {
                Log.d("xxxxxxxxx", "Check your Internet");
                Toast.makeText(MainActivity.this, "onCancel", Toast.LENGTH_LONG).show();
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("TAG", "Error :- ", error);
                // ...
            }
        });

        // ...
// Initialize Firebase Auth

// ...
    }

    public void onClickFacebookButton(View view) {
        if (view == fb) {
            Dialog d = new Dialog(MainActivity.this);
            d.setContentView(R.layout.pleasewaitdialog);
            d.setCancelable(false);
            d.show();
            loginButton.performClick();
        }
    }

    private void handleFacebookAccessToken(final AccessToken token) {
        Log.d("TAG", "handleFacebookAccessToken:" + token);

        AuthCredential credential;
        credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            getUserDetailFromFB();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    void getUserDetailFromFB() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String email = object.getString("email");
                    String name = object.getString("name");
                    String id = object.getString("id");
                    String picture = "https://graph.facebook.com/" + id + "/picture?type=large";

                    //Storing Data into Firestore
                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                    FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                            .setTimestampsInSnapshotsEnabled(true)
                            .build();
                    firestore.setFirestoreSettings(settings);
                    final FirebaseUser user = mAuth.getCurrentUser();
                    firebaseFirestore = FirebaseFirestore.getInstance().collection("Users").document(user.getUid().toString());
                    Map<String, String> data = new HashMap<>();
                    data.put("name", name);
                    data.put("email", email);
                    data.put("id", id);
                    data.put("picture", picture);
                    data.put("Uid", mAuth.getCurrentUser().getUid().toString());
                    firebaseFirestore.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_LONG).show();
                            updateUI(user);
                            Intent intent = new Intent(MainActivity.this, MainArea.class);
                            startActivity(intent);
                            finish();

                        }
                    });


                } catch (Exception e) {
                    Log.d("TAGG", e.getMessage().toString());
                }


            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
        if (NewBroadCast.isOnline(MainActivity.this) == false) {
            Intent i = new Intent(MainActivity.this,NewBroadCast.class);
            sendBroadcast(i);
        }
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(broadCast, intentFilter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadCast);
    }

    private void updateUI(FirebaseUser currentUser) {

        if (currentUser != null) {
            Intent i = new Intent(MainActivity.this, MainArea.class);
            startActivity(i);
            finish();
        }
    }

}
