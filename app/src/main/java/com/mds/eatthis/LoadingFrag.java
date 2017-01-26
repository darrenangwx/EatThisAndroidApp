package com.mds.eatthis;

import android.content.SharedPreferences;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import static android.content.Context.MODE_PRIVATE;

public class LoadingFrag extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private ProgressBar spinner;
    int switchValue;
    String currentLatitude, currentLongtitude;

    //TODO: what is nullable
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //TODO remove all comments under here for all frag with all debugging sysoutprintln
        //returning our layout file
        View v = inflater.inflate(R.layout.fragment_loading, container, false);
        //loading spinner
        spinner = (ProgressBar)v.findViewById(R.id.progressBar1);
        spinner.setVisibility(View.VISIBLE);
        System.out.println("LoadingFrag");
        //get switch value
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("switchData", MODE_PRIVATE);
        switchValue = Integer.parseInt(sharedPreferences.getString("switchValue", ""));
        System.out.println(switchValue + "SWITCH VALUE HERE");
        savePreferences("cLat", "0");
        savePreferences("cLng", "0");

        if(switchValue == 1){
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }else{
            System.out.println("FOR NOT USING GPS");
        }

        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //TODO same here remove all comments under here for all frag
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Search");
    }

    @Override
    public void onStart(){
        super.onStart();
        if(switchValue == 1){
            mGoogleApiClient.connect();
        }

    }

    //replace fragment when button is clicked
    public void replaceFragment(Fragment fragment){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    private void startLocationUpdates(){
        System.out.println("Start location updates");
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(2000);
        // Remove location updates once gotten updated location
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(LoadingFrag.this.getActivity(), "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(LoadingFrag.this.getActivity(), "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        System.out.println(location + "In loadingfrag onlocchange");
        currentLatitude = Double.toString(location.getLatitude());
        currentLongtitude = Double.toString(location.getLongitude());
        System.out.println(currentLongtitude+"currlong");
        System.out.println(currentLatitude+"currlat");
        //once location is updated, stop updating location
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        savePreferences("cLat", currentLatitude);
        savePreferences("cLng", currentLongtitude);
        //After lat and lng stored in preferences, replace fragment
        Fragment fragment = new MapViewFrag();
        replaceFragment(fragment);

    }

    public void savePreferences(String key, String value){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LatLng", MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        prefEditor.putString(key, value);
        prefEditor.commit();
        System.out.println(sharedPreferences.getString("cLat", "") + "LOADINGFRAGLATVALUE");
        System.out.println(sharedPreferences.getString("cLng", "") + "LOADINGFRAGLATVALUE");
    }
}
