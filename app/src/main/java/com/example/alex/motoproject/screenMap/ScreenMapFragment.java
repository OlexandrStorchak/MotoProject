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
import android.widget.Toast;

import com.example.alex.motoproject.App;
import com.example.alex.motoproject.R;
import com.example.alex.motoproject.broadcastReceiver.NetworkStateReceiver;
import com.example.alex.motoproject.dialog.MapUserDetailsDialogFragment;
import com.example.alex.motoproject.event.GpsStatusChangedEvent;
import com.example.alex.motoproject.event.MapMarkerEvent;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.mainActivity.MainActivity;
import com.example.alex.motoproject.service.LocationListenerService;
import com.example.alex.motoproject.util.ArgumentKeys;
import com.example.alex.motoproject.util.CircleTransform;
import com.example.alex.motoproject.util.DipToPixels;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import static com.example.alex.motoproject.R.id.map;


/**
 * The fragment that contains a map from Google Play Services.
 */

public class ScreenMapFragment extends Fragment implements OnMapReadyCallback {

    private static final int MARKER_DIMENSIONS = 45;
    private static final LatLng CHERKASY = new LatLng(49.443, 32.0727);
    @Inject
    NetworkStateReceiver mNetworkStateReceiver;
    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;
    private int mMarkerDimensPx = DipToPixels.toPx(MARKER_DIMENSIONS);
    private App mApp;
    private FloatingActionButton sosToggleButton;
    private List<Target> mTargetStrongRef = new ArrayList<>();
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

    public GoogleMap getMap() {
        return mMap;
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
            String uid = arguments.getString("uid");
            LatLng userCoords = arguments.getParcelable("userCoords");
            if (uid != null) {
                setPosition(uid);
            } else if (userCoords != null) {
                setPosition(userCoords);
            }
        }

        //setup fab that starts or stops LocationListenerService
        sosToggleButton = (FloatingActionButton) view.findViewById(R.id.button_drive_sos);
        sosToggleButton.setImageResource(R.mipmap.ic_sos);
        sosToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), "Send SOS message!", Toast.LENGTH_SHORT).show();
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
            setSosVisibility(View.VISIBLE);
        }
        if (mCameraUpdate == null) {
            int zoom = 11;
            mCameraUpdate = CameraUpdateFactory.newLatLngZoom(CHERKASY, zoom);
        }
        map.moveCamera(mCameraUpdate);

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                ((MainActivity) getActivity())
                        .showDialogFragment(new MapUserDetailsDialogFragment(),
                                MapUserDetailsDialogFragment.class.getSimpleName(),
                                (Bundle) marker.getTag());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                return true;
            }
        });
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
        super.onStop();
        EventBus.getDefault().unregister(this);
        mFirebaseDatabaseHelper.unregisterOnlineUsersLocationListener();
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
        EventBus.getDefault().register(this);
        mFirebaseDatabaseHelper.registerOnlineUsersLocationListener();
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
                .anchor(0.5f, 0.5f));
        marker.setVisible(false);

        Bundle markerData = new Bundle();
        markerData.putString(ArgumentKeys.KEY_UID, event.uid);
        markerData.putString(ArgumentKeys.KEY_NAME, event.userName);
        markerData.putString(ArgumentKeys.KEY_AVATAR_REF, event.avatarRef);
        marker.setTag(markerData);

        mMarkerHashMap.put(event.uid, marker);
        fetchAndSetMarkerIcon(event.uid, event.avatarRef, marker);
    }

    private void fetchAndSetMarkerIcon(final String uid, String avatarRef, final Marker marker) {
        final Target iconTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                mMarkerHashMap.get(uid).setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                marker.setVisible(true);
                mTargetStrongRef.remove(this);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        mTargetStrongRef.add(iconTarget);
        Picasso.with(getContext()).load(avatarRef).resize(mMarkerDimensPx, mMarkerDimensPx)
                .centerCrop().transform(new CircleTransform()).into(iconTarget);
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(mApp,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void onLocationAllowed() {
        if (!mApp.isLocationListenerServiceOn()) {
            mApp.startService(new Intent(mApp, LocationListenerService.class));
            try {
                mApp.unregisterReceiver(mNetworkStateReceiver);
            } catch (IllegalArgumentException e) {
                Log.d("log", "onLocationAllowed: ");
            }
        }

//        if (checkLocationPermission()) {
//            mMap.setMyLocationEnabled(true);
//        }
    }

    //changes CameraUpdate so the map will be showing a chosen user location after gets ready
    public void setPosition(@NonNull String uid) {
        if (mMarkerHashMap.containsKey(uid)) {
            LatLng position = mMarkerHashMap.get(uid).getPosition();
            mCameraUpdate = CameraUpdateFactory.newLatLngZoom(position, 15);
        }
    }

    public void setPosition(@NonNull LatLng latLng) {
        mCameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
    }

    public void setSosVisibility(int visibility) {
        sosToggleButton.setVisibility(visibility);
    }

//    @Override
//    public void onCameraMove() {
//        int iconZoom = (int) mMap.getCameraPosition().zoom * 10;
//        for (String avatarRef : mTargetStrongRef.keySet()) {
//            Picasso.with(getContext()).load(avatarRef).resize(iconZoom, iconZoom)
//                    .centerCrop().transform(new CircleTransform()).into(mTargetStrongRef.get(avatarRef));
//        }
//        if (mMap.getCameraPosition().zoom > 14) {
//            for (String avatarRef : mTargetStrongRef.keySet()) {
//                Picasso.with(getContext()).load(avatarRef).resize(140, 140)
//                        .centerCrop().transform(new CircleTransform()).into(mTargetStrongRef.get(avatarRef));
//            }
//        }
//        if (mMap.getCameraPosition().zoom > 18) {
//            for (String avatarRef : mTargetStrongRef.keySet()) {
//                Picasso.with(getContext()).load(avatarRef).resize(180, 180)
//                        .centerCrop().transform(new CircleTransform()).into(mTargetStrongRef.get(avatarRef));
//            }
//            Toast.makeText(mApp, "Camera update zoom > 18", Toast.LENGTH_SHORT).show();
//            mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
//                @Override
//                public void onCameraMove() {
//                    if (mMap.getCameraPosition().zoom < 18){
//                        mMap.setOnCameraMoveListener(ScreenMapFragment.this);
//                    }
//                }
//            });
//        } else {
//            mMap.setOnCameraMoveListener(this);
//        }
//    }

    @Subscribe
    public void onGpsStatusChanged(GpsStatusChangedEvent event) {
        if (!checkLocationPermission()) {
            return;
        }
        if (event.isGpsOn() && mApp.isLocationListenerServiceOn()) {
            mMap.setMyLocationEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
        }
    }

    //TODO a better interface name
    public interface MapFragmentListener {
        void showAlert(int alertType);

        void handleLocation();
    }
}

