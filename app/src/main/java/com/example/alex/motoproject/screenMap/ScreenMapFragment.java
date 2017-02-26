package com.example.alex.motoproject.screenMap;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import com.example.alex.motoproject.event.MapMarkerEvent;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.mainActivity.AlertControl;
import com.example.alex.motoproject.mainActivity.MainActivity;
import com.example.alex.motoproject.service.LocationListenerService;
import com.example.alex.motoproject.util.CircleTransform;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import static com.example.alex.motoproject.R.id.map;



/**
 * The fragment that contains a map from Google Play Services.
 */

public class ScreenMapFragment extends Fragment implements OnMapReadyCallback {


    public static final LatLng CHERKASY = new LatLng(49.443, 32.0727);

    @Inject
    NetworkStateReceiver mNetworkStateReceiver;
    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;

    private App mApp;

    //for methods calling, like creating pins
    private GoogleMap mMap;
    //for map lifecycle
    private MapView mMapView;
    //stores created markers
    private HashMap<String, Marker> mMarkerHashMap;
    private CameraUpdate mCameraUpdate;

    public ScreenMapFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        App.getCoreComponent().inject(this);
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

        Bundle arguments = getArguments();
        if (arguments != null) {
            // TODO: 25.02.2017 handle this like that
            LatLng coordsFromChat = arguments.getParcelable(null);
            mCameraUpdate = CameraUpdateFactory.newLatLngZoom(coordsFromChat, 15);
        }

        //setup fab that starts or stops LocationListenerService
        FloatingActionButton drivingToggleButton =
                (FloatingActionButton) view.findViewById(R.id.button_drive_toggle);
        drivingToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mApp.isLocationListenerServiceOn()) {
                    //mMapFragmentListener.handleLocation();
                    new AlertControl((MainActivity) getActivity()).handleLocation();
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
            int zoom = 11;
            mCameraUpdate = CameraUpdateFactory.newLatLngZoom(CHERKASY, zoom);
        }
        map.moveCamera(mCameraUpdate);
    }

    public void onMapCk() {
        int zoom = 11;
        mCameraUpdate = CameraUpdateFactory.newLatLngZoom(CHERKASY, zoom);
        mMap.moveCamera(mCameraUpdate);
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
        EventBus.getDefault().unregister(this);
        mFirebaseDatabaseHelper.unregisterOnlineUsersLocationListener();
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
        EventBus.getDefault().register(this);
        mFirebaseDatabaseHelper.registerOnlineUsersLocationListener();
        super.onStart();
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Subscribe
    public void updateMapPin(MapMarkerEvent event) {
        if (mMarkerHashMap.containsKey(event.uid)) {
            Marker marker = mMarkerHashMap.get(event.uid);
            if (event.latLng == null) {
                marker.remove();
                mMarkerHashMap.remove(event.uid);
                return;
            }
            marker.setPosition(event.latLng);
            return;
        }
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(event.latLng)
                .title(event.userName)
                .anchor(0.5f, 0.5f));

        mMarkerHashMap.put(event.uid, marker);
        fetchAndSetMarkerIcon(event.uid, event.avatarRef);

    }

    private void fetchAndSetMarkerIcon(final String uid, String avatarRef) {
        final Set<Target> targetStrongReference = new HashSet<>();
        // TODO: 26.02.2017 handle garbage collecting
        Target iconTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mMarkerHashMap.get(uid).setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                targetStrongReference.remove(this);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                targetStrongReference.remove(this);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        targetStrongReference.add(iconTarget);
        Picasso.with(getContext()).load(avatarRef).resize(80, 80)
                .centerCrop().transform(new CircleTransform()).into(iconTarget);
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
            Log.d("log", "onLocationAllowed: ");
        }

        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
        }
    }

    //changes CameraUpdate so the map will be showing a chosen user location after gets ready
    public void moveToPosition(@NonNull String uid) {
        if (mMarkerHashMap.containsKey(uid)) {
            LatLng position = mMarkerHashMap.get(uid).getPosition();
            mCameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 15);
        }
    }

    public void moveToPosition(@NonNull LatLng latLng) {
        mCameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
    }

//    public void moveToPosition(@NonNull LatLng latLng) {
//        mCameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
//    }

    //TODO a better interface name
    public interface MapFragmentListener {
        void showAlert(int alertType);

        void handleLocation();
    }
}

