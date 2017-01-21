package com.example.alex.motoproject;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import static com.example.alex.motoproject.R.id.map;


/**
 * The fragment that contains a map from Google Play Services.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback {

    public static final int PERMISSION_LOCATION_REQUEST_CODE = 10;

    private GoogleMap mMap;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //lines for custom fragment support
        MapView mMapView = (MapView) view.findViewById(map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();


        mMapView.getMapAsync(this);

        Button buttonStartDriving = (Button) view.findViewById(R.id.button_drive_start);
        buttonStartDriving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                getActivity().stopService(new Intent(getActivity(), LocationListenerService.class));

                handleLocation();
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        //make map accessible from other methods
        mMap = map;

        LatLng cherkasy = new LatLng(49.443, 32.0727);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(cherkasy, 11));

//        map.addMarker(new MarkerOptions()
//                .title("Sydney")
//                .snippet("The most populous city in Australia.")
//                .position(sydney));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_LOCATION_REQUEST_CODE) {
            // Check the request was not cancelled
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted
                handleLocation();

            }
        }
    }

    //handle location runtime permission and setup listener service
    private void handleLocation() {
        //the app is allowed to get location, setup service
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getActivity().startService(new Intent(getActivity(), LocationListenerService.class));
            mMap.setMyLocationEnabled(true);

            //the app is not allowed to get location, show permission prompt and rationale
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_LOCATION_REQUEST_CODE);

            //show rationale
            View view = getView();
            if (view != null) {
                Snackbar.make(getView(),
                        R.string.location_rationale,
                        Snackbar.LENGTH_SHORT).show();
            }

            //no need for rationale, just show the prompt
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_LOCATION_REQUEST_CODE);
        }
    }
}

