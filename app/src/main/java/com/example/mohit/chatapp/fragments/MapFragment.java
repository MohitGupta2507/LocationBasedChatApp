package com.example.mohit.chatapp.fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mohit.chatapp.ChatBox;
import com.example.mohit.chatapp.MainArea;
import com.example.mohit.chatapp.OnlineModel;
import com.example.mohit.chatapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment {

    GoogleMap googleMap;
    MapView mMapView;
    DocumentReference dbref;
    CollectionReference onlineref;
    public static Bitmap picture2;
    Circle circle;
    Marker markerMain;

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_map, container, false);
        dbref=FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineref=FirebaseFirestore.getInstance().collection("Online_Users");
        mMapView = (MapView) v.findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }




        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap2) {

                googleMap = googleMap2;

                dbref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            Double longitude = documentSnapshot.getDouble("longitude");

                            final Double latitude = documentSnapshot.getDouble("latitude");
                            String name = documentSnapshot.getString("name");
                            if (longitude != null && latitude != null) {
                                LatLng sydney = new LatLng(latitude, longitude);

                                googleMap.setMapStyle(
                                        MapStyleOptions.loadRawResourceStyle(
                                                getContext(), R.raw.theme));
                                googleMap.clear();
                                markerMain = googleMap.addMarker(new MarkerOptions().position(sydney).title(name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                                // For zooming automatically to the location of the marker
                                CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(12).build();
                                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                circle = googleMap.addCircle(new CircleOptions()
                                        .center(new LatLng(latitude, longitude))
                                        .radius(5000)
                                        .strokeColor(Color.RED));

                                onlineref.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                                        for (final QueryDocumentSnapshot q : queryDocumentSnapshots) {
                                            if (q.exists()) {

                                                if (q.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid().toString())) {

                                                    Toast.makeText(getActivity(), "Equals", Toast.LENGTH_LONG);
                                                } else {
                                                    final OnlineModel model = q.toObject(OnlineModel.class);
                                                    final Double lat = model.getLatitude();
                                                    final Double longi = model.getLongitude();
                                                    Location latLng = new Location("pointA");
                                                    latLng.setLatitude(lat);
                                                    latLng.setLongitude(longi);
                                                    Location CircleLat = new Location("Circle");
                                                    CircleLat.setLatitude(circle.getCenter().latitude);
                                                    CircleLat.setLongitude(circle.getCenter().longitude);
                                                    float distanceInMeters = latLng.distanceTo(CircleLat);
                                                    Toast.makeText(getActivity(), model.getUid() + "\nDistance:- " + distanceInMeters, Toast.LENGTH_LONG).show();
                                                    if (distanceInMeters <= circle.getRadius()) {
                                                        final String Uid = q.getId().toString();
                                                        final DocumentReference documentReference = FirebaseFirestore.getInstance().collection("Users").document(q.getId());
                                                        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                if (!(documentSnapshot.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))) {
                                                                    String name = documentSnapshot.getString("name");
                                                                    final String picture = documentSnapshot.getString("picture");

                                                                    LatLng latLng2 = new LatLng(lat, longi);
                                                                    googleMap.addMarker(new MarkerOptions().position(latLng2).title(name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                                                                    googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                                                                        @Override
                                                                        public View getInfoWindow(Marker marker) {
                                                                            View v = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.info, null);
                                                                            TextView nameUser = (TextView) v.findViewById(R.id.InfoUsername);
                                                                            CircleImageView circleImageView = (CircleImageView) v.findViewById(R.id.InfoUserImage);
                                                                            nameUser.setText(marker.getTitle());
                                                                            Picasso.get().load(picture).into(circleImageView);
                                                                            return v;
                                                                        }

                                                                        @Override
                                                                        public View getInfoContents(Marker marker) {

                                                                            return null;
                                                                        }

                                                                    });
                                                                    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                                                        @Override
                                                                        public boolean onMarkerClick(Marker marker) {

                                                                            if (marker != markerMain) {
                                                                                Intent intent = new Intent(getActivity(), ChatBox.class);
                                                                                intent.putExtra("Uid", q.getId());
                                                                                startActivity(intent);
                                                                                return true;
                                                                            } else {
                                                                                return false;
                                                                            }
                                                                        }
                                                                    });
                                                                }


                                                            }
                                                        });


                                                    }
                                                }
                                            }
                                        }
                                    }
                                });




                            }
                        }

                    }
                });



            }
        });

    }






    static class BackImage extends AsyncTask<String,Void,Bitmap>
    {

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap image = null;
            try {
                URL urll = new URL(strings[0]);
                image = BitmapFactory.decodeStream(urll.openConnection().getInputStream());
            } catch(IOException e) {
                System.out.println(e);
            }
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            picture2=bitmap;
        }
    }


    public Bitmap createCustomMarker(Context context, Bitmap resource, String _name) {

        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);

        CircleImageView markerImage = (CircleImageView) marker.findViewById(R.id.user_dp);
        markerImage.setImageBitmap(resource);
        TextView txt_name = (TextView)marker.findViewById(R.id.name);
        txt_name.setText(_name);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);

        return bitmap;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

}
