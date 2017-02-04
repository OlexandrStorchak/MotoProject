package com.example.alex.motoproject.fragments;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.broadcastReceiver.NetworkStateReceiver;
import com.example.alex.motoproject.services.LocationListenerService;
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
    public static final String LOG_TAG = MapFragment.class.getSimpleName();

//    static final String STATE_SERVICE = "isServiceOn";

    private static MapFragment mapFragmentInstance;
    private final BroadcastReceiver mNetworkStateReceiver = new NetworkStateReceiver();
    MapFragmentListener mMapFragmentListener;
    Marker marker;
    boolean isServiceOn;

    //for methods calling, like creating pins
    private GoogleMap mMap;
    //for map lifecycle
    private MapView mMapView;
    private DatabaseReference mDatabase;
    private String userUid;
    private HashMap<String, Marker> hashMap;

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
//        isServiceOn = ((App) getActivity().getApplication()).isLocationListenerServiceOn();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        outState.putBoolean(STATE_SERVICE, isServiceOn);
//        super.onSaveInstanceState(outState);
//    }
//
//    @Override
//    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
//        if (savedInstanceState != null) {
//            isServiceOn = savedInstanceState.getBoolean(STATE_SERVICE);
//        }
//        super.onViewStateRestored(savedInstanceState);
//    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //add Google map
        mMapView = (MapView) view.findViewById(map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        if (checkLocationPermission() && isServiceOn) {
            mMap.setMyLocationEnabled(true);
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            userUid = auth.getCurrentUser().getUid();
        } else {
            auth.signOut();
        }

        FloatingActionButton drivingToggleButton =
                (FloatingActionButton) view.findViewById(R.id.button_drive_toggle);
        drivingToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isServiceOn) {
                    mMapFragmentListener.handleLocation();
                } else if (checkLocationPermission()) {
                    mMap.setMyLocationEnabled(false);
                    getActivity().stopService(
                            new Intent(getActivity(), LocationListenerService.class));
                }
            }
        });

        hashMap = new HashMap<>();

        fetchUserLocations();

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        //make map accessible from other methods
        mMap = map;
        mMap.getUiSettings().setMapToolbarEnabled(false);

        LatLng cherkasy = new LatLng(49.443, 32.0727);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(cherkasy, 11));
    }

    @Override

    
    public void setMarker(double lat,double lon,String name){
        LatLng location = new LatLng(lat,lon);

        mMap.addMarker(new MarkerOptions().position(location).title(name));
        Log.d(TAG, "setMarker: ");
    }

    
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
        super.onStart();
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    private void fetchUserLocations() {
        mDatabase.child("location").addChildEventListener(new ChildEventListener() {
            // TODO: do not receive updates for only one updated value
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(LOG_TAG, dataSnapshot.toString());
                String uid = dataSnapshot.getKey();
                if (!uid.equals(userUid)) {
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
                if (!uid.equals(userUid)) {
                    Double lat = (Double) dataSnapshot.child("lat").getValue();
                    Double lng = (Double) dataSnapshot.child("lng").getValue();
                    if (lat != null && lng != null) {
                        LatLng latLng = new LatLng(lat, lng);
                        if (hashMap.containsKey(uid)) {
                            Marker changeableMarker = hashMap.get(uid);
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
        });
    }

    private void createPinOnMap(LatLng latLng, String uid) {
        marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(uid));
        Log.d(LOG_TAG, "pin created!");
        hashMap.put(uid, marker);
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void onLocationAllowed() {
        getActivity().startService(new Intent(getActivity(), LocationListenerService.class));
        try {
            getActivity().unregisterReceiver(mNetworkStateReceiver);
        } catch (IllegalArgumentException e) {
            Log.v(LOG_TAG, "mNetworkReceiver has already been unregistered");
        }

        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
        }
    }

    //TODO a better interface name
    public interface MapFragmentListener {
        void showAlert(int alertType);

        void handleLocation();
    }
}

