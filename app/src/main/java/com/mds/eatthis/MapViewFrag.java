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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;
import static com.mds.eatthis.AppConfig.*;
/**
 * Created by Darren on 1/22/2017.
 */
public class MapViewFrag extends Fragment implements OnMapReadyCallback{

    private GoogleMap gMap;
    MapView mMapView;
    private ImageButton heartButton;
    private Button changeRestaurant;
    private TextView restaurant;
    private TextView address;
    int testid = 1;
    JSONObject result;
    //set random number to new place
    int newPlace = 9000;
    int oldPlace;

    double currentLatitude, currentLongtitude;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        //Initializing map
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

        //getting nearby places from loadingfrag
        String nearbyPlaces = getArguments().getString("nearbyPlaces");
        try {
            result = new JSONObject(nearbyPlaces);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        restaurant = (TextView) v.findViewById(R.id.restaurant);
        address = (TextView) v.findViewById(R.id.address);

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

        changeRestaurant = (Button) v.findViewById(R.id.changerestaurant);
        changeRestaurant.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                gMap.clear();
                mMapView.getMapAsync(MapViewFrag.this);
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
    public void onStart(){
        super.onStart();
        System.out.println("ONSTART");
    }

    public void onResume(){
        super.onResume();
        System.out.println("ONRESUME");
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LatLng", MODE_PRIVATE);
        currentLatitude = Double.parseDouble(sharedPreferences.getString("cLat", ""));
        currentLongtitude = Double.parseDouble(sharedPreferences.getString("cLng", ""));
        System.out.println(currentLatitude + currentLongtitude + "latlonginmapready");
        // Add a marker in user's coordinates and move the camera
        LatLng latLng = new LatLng(currentLatitude, currentLongtitude);
        MarkerOptions option = new MarkerOptions().position(latLng).title("You are here");
        gMap.addMarker(option);
        float zoomlevel = (float) 15.0;
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomlevel));
        //start plotting nearby place
        parseLocationResult(result);
    }

    private void parseLocationResult(JSONObject result){
        double placeLatitude, placeLongitude;
        String placeName, vicinity;

        try {
            JSONArray jsonArray = result.getJSONArray("results");
            Random rand = new Random();
            if(newPlace == 9000){
                newPlace = rand.nextInt(jsonArray.length() - 0) + 0;
                oldPlace = newPlace;
            }else{
                //make sure new place generated is a not the same as the previous one
                while(newPlace == oldPlace){
                    newPlace = rand.nextInt(jsonArray.length() - 0) + 0;
                }
                oldPlace = newPlace;
            }
            JSONObject place = jsonArray.getJSONObject(newPlace);
            placeName = place.getString(NAME);
            vicinity = place.getString(VICINITY);
            placeLatitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION).getDouble(LATITUDE);
            placeLongitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION).getDouble(LONGITUDE);
            restaurant.setText(placeName);
            address.setText(vicinity);

            MarkerOptions markerOptions = new MarkerOptions();
            LatLng latLng = new LatLng(placeLatitude, placeLongitude);
            markerOptions.position(latLng);
            markerOptions.title(placeName + " : " + vicinity);
            markerOptions.visible(true);
            gMap.addMarker(markerOptions);

        }catch(JSONException e){
            e.printStackTrace();
        }

    }
}
