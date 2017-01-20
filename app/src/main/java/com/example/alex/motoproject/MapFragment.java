package com.example.alex.motoproject;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.MarkerOptions;

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
        MapView mMapView = (MapView) view.findViewById(map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();
        mMapView.getMapAsync(this);//when you already implement OnMapReadyCallback in your fragment

        Button buttonStartDriving = (Button) view.findViewById(R.id.button_drive_start);
        buttonStartDriving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLocation();
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        //TODO: check if this line of code is needed at all
        mMap = map;

        LatLng sydney = new LatLng(-33.867, 151.206);

        //TODO: fix the error (fixed?)
//        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            map.setMyLocationEnabled(true);
//        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));

        map.addMarker(new MarkerOptions()
                .title("Sydney")
                .snippet("The most populous city in Australia.")
                .position(sydney));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_LOCATION_REQUEST_CODE) {
            // Check the request wasn`t cancelled
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted
                handleLocation();

            }
        }
    }

    private void handleLocation() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getActivity().startService(new Intent(getActivity(), LocationListenerService.class));
            mMap.setMyLocationEnabled(true);
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_LOCATION_REQUEST_CODE);
            View view = getView();
            if (view != null) {
                Snackbar.make(getView(),
                        "Без цього ми не зможемо показувати вас на карті!",
                        Snackbar.LENGTH_SHORT).show();
            }
        } else {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_LOCATION_REQUEST_CODE);
        }
    }
}

