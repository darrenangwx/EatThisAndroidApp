package com.mds.eatthis;

import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataApi;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

public class LoadingFrag extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private ProgressBar spinner;
    int switchValue;
    String locationID;
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
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("searchFragData", MODE_PRIVATE);
        switchValue = Integer.parseInt(sharedPreferences.getString("switchValue", ""));
        System.out.println(switchValue + "SWITCH VALUE HERE");
        savePreferences("cLat", "0");
        savePreferences("cLng", "0");

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        if(switchValue == 0){
            locationID = sharedPreferences.getString("inputLocID", "");
        }


        /*else{
            String mylocation = sharedPreferences.getString("inputLocName", "");
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(mylocation, 1);
                System.out.println(addresses);
                Address address = addresses.get(0);
                currentLatitude = Double.toString(address.getLatitude());
                currentLongtitude = Double.toString(address.getLongitude());
                savePreferences("cLat", currentLatitude);
                savePreferences("cLng", currentLongtitude);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }*/

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
        mGoogleApiClient.connect();
    }

    //replace fragment when button is clicked
    public void replaceFragment(Fragment fragment){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(switchValue == 0){
            findLocationFromID(locationID);
        }else{
            startLocationUpdates();
        }
    }

    //get user inputted location coordinates from ID
    private void findLocationFromID(String locationID){
        Places.GeoDataApi.getPlaceById(mGoogleApiClient, locationID)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(@NonNull PlaceBuffer places) {
                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                            final Place myPlace = places.get(0);
                            LatLng gotLocation = myPlace.getLatLng();
                            currentLatitude = Double.toString(gotLocation.latitude);
                            currentLongtitude = Double.toString(gotLocation.longitude);
                            savePreferences("cLat", currentLatitude);
                            savePreferences("cLng", currentLongtitude);
                            //After lat and lng stored in preferences, replace fragment
                            Fragment fragment = new MapViewFrag();
                            replaceFragment(fragment);
                            Log.i(TAG, "Place found: " + myPlace.getName());
                        } else {
                            Log.e(TAG, "Place not found");
                        }
                        places.release();
                    }
                });
    }

    //request for location updates
    private void startLocationUpdates(){
        System.out.println("Start location updates");
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)
                .setFastestInterval(2000);
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
        // Remove location updates once gotten updated location
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
