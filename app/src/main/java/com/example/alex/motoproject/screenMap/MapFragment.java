package com.example.alex.motoproject.screenMap;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.alex.motoproject.R;
import com.example.alex.motoproject.app.App;
import com.example.alex.motoproject.dialog.MapUserDetailsDialogFragment;
import com.example.alex.motoproject.event.GpsStatusChangedEvent;
import com.example.alex.motoproject.firebase.FirebaseDatabaseHelper;
import com.example.alex.motoproject.retainFragment.FragmentWithRetainInstance;
import com.example.alex.motoproject.screenMain.MainActivity;
import com.example.alex.motoproject.transformation.PicassoCircleTransform;
import com.example.alex.motoproject.util.ArgKeys;
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
import static com.example.alex.motoproject.util.ArgKeys.KEY_UID;
import static com.example.alex.motoproject.util.ArgKeys.KEY_USER_COORDS;
import static com.example.alex.motoproject.util.ArgKeys.LATITUDE;
import static com.example.alex.motoproject.util.ArgKeys.LONGITUDE;
import static com.example.alex.motoproject.util.ArgKeys.MAP_TYPE;
import static com.example.alex.motoproject.util.ArgKeys.SOS_COOL_DOWN;
import static com.example.alex.motoproject.util.ArgKeys.TILT;
import static com.example.alex.motoproject.util.ArgKeys.ZOOM;


/**
 * The fragment that contains a map from Google Play Services.
 */

public class MapFragment extends FragmentWithRetainInstance implements
        OnMapReadyCallback, FirebaseDatabaseHelper.MapMarkersUpdateReceiver {

    private static final int MARKER_DIMENS_DP = 90;
    private static final int MARKER_DIMENS_PX = DimensHelper.dpToPx(MARKER_DIMENS_DP);

    @Inject
    FirebaseDatabaseHelper mFirebaseDatabaseHelper;

    //Picasso has no strong references to its targets, create a custom strong reference
    private List<Target> mTargetStrongRef = new ArrayList<>();
    //for methods calling, like creating pins
    private GoogleMap mMap;
    //for map lifecycle
    private MapView mMapView;
    //stores created markers
    private HashMap<String, Marker> mMarkerHashMap = new HashMap<>();

    private App mApp;
    private FloatingActionButton mSosToggleButton;
    private CameraUpdate mCameraUpdate;

    private boolean mSosButtonCoolDown;
    private String mUidToMove; //the map will be moved to a user with this id

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public String getDataTag() {
        return MapFragment.class.getName();
    }

    public GoogleMap getGoogleMap() {
        return mMap;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null) return;

        mCameraUpdate = CameraUpdateFactory.newCameraPosition(
                (CameraPosition) savedInstanceState.getParcelable(CAMERA_POSITION));

        if (savedInstanceState.getBoolean(SOS_COOL_DOWN, false)) {
            startSosCoolDown();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMap == null) return;

        CameraPosition position = mMap.getCameraPosition();

        outState.putParcelable(CAMERA_POSITION,
                new CameraPosition(new LatLng((float) position.target.latitude,
                        (float) position.target.longitude),
                        position.zoom,
                        position.tilt,
                        position.bearing));
        outState.putInt(MAP_TYPE, mMap.getMapType());

        outState.putBoolean(SOS_COOL_DOWN, mSosButtonCoolDown);
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
        //Init Google map
        mMapView = (MapView) view.findViewById(map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        //Get given coordinates to go to if they exist
        Bundle arguments = getArguments();
        if (arguments != null) {
            String uid = arguments.getString(KEY_UID);
            LatLng userCoords = arguments.getParcelable(KEY_USER_COORDS);
            if (uid != null) {
                setPosition(uid);
            } else if (userCoords != null) {
                setPosition(userCoords);
            }
        }

        //Init fab that sends sos
        mSosToggleButton = (FloatingActionButton) view.findViewById(R.id.button_drive_sos);
        mSosToggleButton.setImageResource(R.drawable.ic_help);
        mSosToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), R.string.sos_sent, Toast.LENGTH_SHORT).show();
                mFirebaseDatabaseHelper.sendSosMessage(
                        getString(R.string.notification_tittle_need_help));

                startSosCoolDown();
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    private void startSosCoolDown() {
        //Invisible button for 5 minutes
        mSosToggleButton.setVisibility(View.GONE);
        mSosButtonCoolDown = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSosToggleButton.setVisibility(View.VISIBLE);
                mSosButtonCoolDown = false;
            }
        }, 300000);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.getUiSettings().setMapToolbarEnabled(false);

        if (checkLocationPermission() && mApp.isLocationListenerServiceOn()) {
            mMap.setMyLocationEnabled(true);
            setSosVisibility(View.VISIBLE);
        }
        if (mCameraUpdate == null) {
            SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
            CameraPosition position = new CameraPosition(new LatLng(prefs.getFloat(LATITUDE, 49),
                    prefs.getFloat(LONGITUDE, 32)),
                    prefs.getFloat(ZOOM, 4),
                    prefs.getFloat(TILT, 0),
                    prefs.getFloat(BEARING, 0));

            mCameraUpdate = CameraUpdateFactory.newCameraPosition(position);
        }

        try { //Might occur if orientation changes too many times in a while
            mMap.moveCamera(mCameraUpdate);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
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

    @Override
    public void onDestroyView() {
        mMarkerHashMap.clear();
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

        //This situation might occur if changing orientation 20+ times in a short period of time
        if (mMap == null) return;

        //Save map position and other map preferences into SharedPreferences
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
        mFirebaseDatabaseHelper.registerOnlineUsersLocationListener(this);
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onMarkerChange(final MapMarkerModel model) {
        //The marker is already on the map, just need to change its coordinates
        if (mMarkerHashMap.containsKey(model.uid)) {
            Marker marker = mMarkerHashMap.get(model.uid);
            marker.setPosition(model.latLng);
            return;
        }
        //There is no such marker on the map, so create a new one
        final Marker marker = mMap.addMarker(new MarkerOptions()
                .position(model.latLng)
                .anchor(0.5f, 0.5f));
        marker.setVisible(false);

        Bundle markerData = new Bundle();
        markerData.putString(ArgKeys.KEY_UID, model.uid);
        markerData.putString(ArgKeys.KEY_NAME, model.userName);
        markerData.putString(ArgKeys.KEY_AVATAR_REF, model.avatarRef);
        marker.setTag(markerData);

        mMarkerHashMap.put(model.uid, marker);

        DimensHelper.getScaledAvatar(model.avatarRef,
                MARKER_DIMENS_PX, new DimensHelper.AvatarRefReceiver() {
                    @Override
                    public void onRefReady(String ref) {
                        if (marker.getTag() == null) return;
                        fetchAndSetMarkerIcon(ref, marker);

                        if (mUidToMove != null && mUidToMove.equals(model.uid)) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(model.latLng, 15));
                        }
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

    @Override
    public void onMarkerDelete(String uid) {
        if (mMarkerHashMap.containsKey(uid)) {
            Marker marker = mMarkerHashMap.get(uid);
            marker.remove();
            mMarkerHashMap.remove(uid);
        }
    }

    private void fetchAndSetMarkerIcon(String avatarRef, final Marker marker) {
        final Target iconTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
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
                .centerCrop().transform(new PicassoCircleTransform()).into(iconTarget);
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(mApp,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    //changes CameraUpdate so the map will be showing a chosen user location after gets ready
    public void setPosition(@NonNull String uid) {
        mUidToMove = uid;
    }

    public void setPosition(@NonNull LatLng latLng) {
        mCameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
    }

    public void setSosVisibility(int visibility) {
        if (!mSosButtonCoolDown && mSosToggleButton != null) {
            mSosToggleButton.setVisibility(visibility);
        }
    }

    @Subscribe(sticky = true)
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

    public interface MapFragmentHolder {
        void showAlert(int alertType);

        void handleLocation();
    }

}

