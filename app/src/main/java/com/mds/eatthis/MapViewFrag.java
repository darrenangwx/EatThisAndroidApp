package com.mds.eatthis;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;


import java.sql.SQLOutput;

import static android.content.ContentValues.TAG;


/**
 * Created by Darren on 1/22/2017.
 */
public class MapViewFrag extends Fragment implements OnMapReadyCallback,
GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private GoogleMap gMap;
    MapView mMapView;
    private ImageButton heartButton;
    int testid = 1;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    double currentLatitude, currentLongtitude;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e){
            e.printStackTrace();
        }
        mMapView = (MapView) v.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        //start mapView immediately
        mMapView.onResume();
        //call onMapReady
        mMapView.getMapAsync(this);

        heartButton = (ImageButton) v.findViewById(R.id.heartborder);
        heartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(testid == 1){
                    Toast.makeText(MapViewFrag.this.getActivity(),"Added to favourites", Toast.LENGTH_SHORT).show();
                    heartButton.setBackgroundResource(R.drawable.favourited);
                    testid = 0;

                }else{
                    Toast.makeText(MapViewFrag.this.getActivity(),"Removed from favourites", Toast.LENGTH_SHORT).show();
                    heartButton.setBackgroundResource(R.drawable.favouriteborder);
                    testid = 1;
                }

            }
        });
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Map");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        System.out.println(currentLatitude + currentLongtitude + "latlonginmapready");
        // Add a marker in Sydney and move the camera
        LatLng latLng = new LatLng(currentLatitude, currentLongtitude);
        MarkerOptions option = new MarkerOptions().position(latLng).title("Marker");
        gMap.addMarker(option);
        float zoomlevel = (float) 17.0;
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomlevel));
    }

    @Override
    public void onResume() {
        super.onResume();
        //start connection to googleapiclient
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            //TODO: check if gps is enabled or not,
            gMap.clear();
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        //get current location of phone
        //TODO: might not need this to work
        /*mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mCurrentLocation != null){
            currentLatitude = mCurrentLocation.getLatitude();
            currentLongtitude = mCurrentLocation.getLongitude();
        }*/
        //Begin polling for new location updates.
        startLocationUpdates();

    }

    private void startLocationUpdates(){
        System.out.println("Start location updates");
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(2000);
        // Request location updates
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(MapViewFrag.this.getActivity(), "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(MapViewFrag.this.getActivity(), "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
       // Log.i(LOG_TAG,"onConnectionFailed:"+connectionResult.getErrorCode()+","+connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        System.out.println(location + "In onlocationchanged");
        currentLatitude = location.getLatitude();
        currentLongtitude = location.getLongitude();
        //once location is updated, stop updating location
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        //call onMapReady
        //TODO: if gps is enabled
        mMapView.getMapAsync(this);
    }
}
