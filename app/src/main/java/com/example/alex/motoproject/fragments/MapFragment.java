package com.example.alex.motoproject.fragments;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.R;
import com.example.alex.motoproject.broadcastReceiver.NetworkStateReceiver;
import com.example.alex.motoproject.services.LocationListenerService;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import static com.example.alex.motoproject.R.id.map;


/**
 * The fragment that contains a map from Google Play Services.
 */

//TODO: custom pins
//TODO if user is offline, hide his pin
public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final String LOG_TAG = MapFragment.class.getSimpleName();
    public static MapFragment mapFragmentInstance;
    private final BroadcastReceiver mNetworkStateReceiver = new NetworkStateReceiver();
    private MapFragmentListener mMapFragmentListener;
    private App mApp;

    //for methods calling, like creating pins
    private GoogleMap mMap;
    //for map lifecycle
    private MapView mMapView;
    //stores created markers
    private HashMap<String, Marker> mMarkerHashMap;

    private DatabaseReference mDatabase;
    private String mUserUid;
    private ChildEventListener mUsersLocationsListener;
    private CameraUpdate mCameraUpdate;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment getInstance() {
        if (mapFragmentInstance == null) {
            mapFragmentInstance = new MapFragment();
        }
        return mapFragmentInstance;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mMapFragmentListener = (MapFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnMapFragmentListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mApp = (App) getContext().getApplicationContext();
        //add Google map
        mMapView = (MapView) view.findViewById(map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            mUserUid = auth.getCurrentUser().getUid();
        } else {
            auth.signOut();
        }
        //setup fab that starts or stops LocationListenerService
        FloatingActionButton drivingToggleButton =
                (FloatingActionButton) view.findViewById(R.id.button_drive_toggle);
        drivingToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mApp.isLocationListenerServiceOn()) {
                    mMapFragmentListener.handleLocation();
                } else if (checkLocationPermission()) {
                    mMap.setMyLocationEnabled(false);
                    getContext().stopService(
                            new Intent(getContext(), LocationListenerService.class));
                }
            }
        });

        mMarkerHashMap = new HashMap<>();

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        //make map accessible from other methods
        mMap = map;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        if (checkLocationPermission() && mApp.isLocationListenerServiceOn()) {
            mMap.setMyLocationEnabled(true);
        }
        if (mCameraUpdate == null) {
            LatLng cherkasy = new LatLng(49.443, 32.0727);
            int zoom = 11;
            mCameraUpdate = CameraUpdateFactory.newLatLngZoom(cherkasy, zoom);
        }
        map.moveCamera(mCameraUpdate);


//        LatLng cherkasy = new LatLng(49.443, 32.0727);
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(cherkasy, 11));
    }
//    public void setMarker(double lat,double lon,String name){
//        LatLng location = new LatLng(lat,lon);
//
//        mMap.addMarker(new MarkerOptions().position(location).title(name));
//        Log.d(TAG, "setMarker: ");
//    }

    @Override
    public void onDestroyView() {
        mMapView.onDestroy();
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        mMapView.onLowMemory();
        super.onLowMemory();
    }

    @Override
    public void onStop() {
        mMapView.onStop();
        mDatabase.removeEventListener(mUsersLocationsListener);
        super.onStop();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onStart() {
        mMapView.onStart();
        fetchUserLocations();
        super.onStart();
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    private void fetchUserLocations() {
        mUsersLocationsListener = new ChildEventListener() {
            // TODO: do not receive updates for only one updated value
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(LOG_TAG, dataSnapshot.toString());
                String uid = dataSnapshot.getKey();
                if (!uid.equals(mUserUid)) {
                    Double lat = (Double) dataSnapshot.child("lat").getValue();
                    Double lng = (Double) dataSnapshot.child("lng").getValue();
                    if (lat != null && lng != null) {
                        LatLng latLng = new LatLng(lat, lng);
                        createPinOnMap(latLng, uid);
                    }
                    Log.d(LOG_TAG, lat + " " + lng);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String uid = dataSnapshot.getKey();
                if (!uid.equals(mUserUid)) {
                    Double lat = (Double) dataSnapshot.child("lat").getValue();
                    Double lng = (Double) dataSnapshot.child("lng").getValue();
                    if (lat != null && lng != null) {
                        LatLng latLng = new LatLng(lat, lng);
                        if (mMarkerHashMap.containsKey(uid)) {
                            Marker changeableMarker = mMarkerHashMap.get(uid);
                            changeableMarker.setPosition(latLng);
                        } else {
                            createPinOnMap(latLng, uid);
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.child("location").addChildEventListener(mUsersLocationsListener);
    }

    private void createPinOnMap(LatLng latLng, String uid) {
        Marker mMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(uid));
        Log.d(LOG_TAG, "pin created!");
        mMarkerHashMap.put(uid, mMarker);
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void onLocationAllowed() {
        getContext().startService(new Intent(getContext(), LocationListenerService.class));
        try {
            getContext().unregisterReceiver(mNetworkStateReceiver);
        } catch (IllegalArgumentException e) {
            Log.v(LOG_TAG, "mNetworkReceiver has already been unregistered");
        }

        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
        }
    }

    //changes CameraUpdate so the map will be showing a chosen user location after gets ready
    public void moveToMarker(@NonNull String uid) {
        if (mMarkerHashMap.containsKey(uid)) {
            LatLng position = mMarkerHashMap.get(uid).getPosition();
            mCameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 15);
        }
    }

    //TODO a better interface name
    public interface MapFragmentListener {
        void showAlert(int alertType);

        void handleLocation();
    }
}

