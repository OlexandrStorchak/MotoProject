package com.example.alex.motoproject.screenMap;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.alex.motoproject.util.ArgKeys;
import com.example.alex.motoproject.util.CircleTransform;
import com.example.alex.motoproject.util.DimensHelper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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
import static com.example.alex.motoproject.util.ArgKeys.BEARING;
import static com.example.alex.motoproject.util.ArgKeys.CAMERA_POSITION;
import static com.example.alex.motoproject.util.ArgKeys.LATITUDE;
import static com.example.alex.motoproject.util.ArgKeys.LONGITUDE;
import static com.example.alex.motoproject.util.ArgKeys.MAP_TYPE;
import static com.example.alex.motoproject.util.ArgKeys.TILT;
import static com.example.alex.motoproject.util.ArgKeys.ZOOM;


/**
 * The fragment that contains a map from Google Play Services.
 */

public class ScreenMapFragment extends Fragment implements OnMapReadyCallback {

    private static final int MARKER_DIMENS_DP = 90;
    private static final int MARKER_DIMENS_PX = DimensHelper.dpToPx(MARKER_DIMENS_DP)/2;

    @Inject
    NetworkStateReceiver mNetworkStateReceiver;
    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;
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
    private int mMapType;

    public ScreenMapFragment() {
        // Required empty public constructor
    }

    public GoogleMap getxMap() {
        return mMap;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }
        mCameraUpdate = CameraUpdateFactory.newCameraPosition(
                (CameraPosition) savedInstanceState.getParcelable(CAMERA_POSITION));
        mMapType = savedInstanceState.getInt(MAP_TYPE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMap == null) {
            return;
        }
        CameraPosition position = mMap.getCameraPosition();

        outState.putParcelable(CAMERA_POSITION,
                new CameraPosition(new LatLng((float) position.target.latitude,
                        (float) position.target.longitude),
                        position.zoom,
                        position.tilt,
                        position.bearing));
        outState.putInt(MAP_TYPE, mMap.getMapType());
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
        mMapView.getMapAsync(this); // TODO: 25.03.2017 do not call this if map is not null

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
                mFirebaseDatabaseHelper.sendSosMessage();
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
            SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);

            mMapType = prefs.getInt(MAP_TYPE, 1);

            CameraPosition position = new CameraPosition(new LatLng(prefs.getFloat(LATITUDE, 49),
                    prefs.getFloat(LONGITUDE, 32)),
                    prefs.getFloat(ZOOM, 4),
                    prefs.getFloat(TILT, 0),
                    prefs.getFloat(BEARING, 0));

            mCameraUpdate = CameraUpdateFactory.newCameraPosition(position);
        }
        map.moveCamera(mCameraUpdate);
        mMap.setMapType(mMapType);

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
//        int zoom = 11;
//        mCameraUpdate = CameraUpdateFactory.newLatLngZoom(CHERKASY, zoom);
//        mMap.moveCamera(mCameraUpdate);
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


        CameraPosition position = mMap.getCameraPosition();
        mCameraUpdate = CameraUpdateFactory.newCameraPosition(position);

        SharedPreferences.Editor editor = getActivity().getPreferences(Context.MODE_PRIVATE).edit();

        editor.putFloat(LATITUDE, (float) position.target.latitude);
        editor.putFloat(LONGITUDE, (float) position.target.longitude);
        editor.putFloat(ZOOM, position.zoom);
        editor.putFloat(TILT, position.tilt);
        editor.putFloat(BEARING, position.bearing);
        editor.putInt(MAP_TYPE, mMap.getMapType());

        editor.apply();

//        if (getArguments() != null) {
//            getArguments().clear();
//        }

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
        if (event.latLng == null) {
            return;
        }
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(event.latLng)
                .anchor(0.5f, 0.5f));
        marker.setVisible(false);

        Bundle markerData = new Bundle();
        markerData.putString(ArgKeys.KEY_UID, event.uid);
        markerData.putString(ArgKeys.KEY_NAME, event.userName);
        markerData.putString(ArgKeys.KEY_AVATAR_REF, event.avatarRef);
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
        Picasso.with(getContext()).load(avatarRef).resize(MARKER_DIMENS_PX, MARKER_DIMENS_PX)
                .centerCrop().transform(new CircleTransform()).into(iconTarget);
//              Glide.with(getContext()).load(avatarRef)
//                .asBitmap()
//                .override(MARKER_DIMENS_DP, MARKER_DIMENS_DP)
//                .transform(new CropCircleTransformation(getContext()))
//                .into(new SimpleTarget<Bitmap>() {
//                    @Override
//                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
//                        mMarkerHashMap.get(uid).setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
//                        marker.setVisible(true);
//                    }
//                });
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
    public interface MapFragmentHolder {
        void showAlert(int alertType);

        void handleLocation();
    }
}

