package com.mds.eatthis;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;
import static android.provider.BaseColumns._ID;
import static com.mds.eatthis.DatabaseConstants.PlaceID;
import static com.mds.eatthis.DatabaseConstants.RestaurantLat;
import static com.mds.eatthis.DatabaseConstants.RestaurantLocation;
import static com.mds.eatthis.DatabaseConstants.RestaurantLong;
import static com.mds.eatthis.DatabaseConstants.RestaurantName;
import static com.mds.eatthis.DatabaseConstants.TABLE_NAME;
import static com.mds.eatthis.R.id.address;
import static com.mds.eatthis.R.id.favmap;

/**
 * Created by Darren on 2/6/2017.
 */

public class FavMapViewFrag extends Fragment implements OnMapReadyCallback{
    private GoogleMap gMap;
    MapView mMapView;
    private ImageButton heartButton;
    private TextView restaurant;
    private TextView address;
    String placeid;
    String placeName;
    String vicinity;
    double placeLatitude;
    double placeLongitude;
    LatLng placeLatLng;
    int favheartid = 0;
    private DatabaseEventsData locationdetails;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        View v = inflater.inflate(R.layout.fragment_favmap, container, false);
        placeid = getArguments().getString("placeid");
        placeName = getArguments().getString("placeName");
        vicinity = getArguments().getString("address");
        placeLatitude = getArguments().getDouble("lat");
        placeLongitude = getArguments().getDouble("lng");

        //Initializing map
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e){
            e.printStackTrace();
        }
        mMapView = (MapView) v.findViewById(favmap);
        mMapView.onCreate(savedInstanceState);
        //start mapView immediately
        mMapView.onResume();
        //call onMapReady
        mMapView.getMapAsync(this);

        restaurant = (TextView) v.findViewById(R.id.restaurant);
        address = (TextView) v.findViewById(R.id.address);

        heartButton = (ImageButton) v.findViewById(R.id.favourited);

        locationdetails = new DatabaseEventsData(FavMapViewFrag.this.getActivity());

        heartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(favheartid == 1){
                    try{
                        addEvent();
                        Toast.makeText(FavMapViewFrag.this.getActivity(),"Added to favourites", Toast.LENGTH_SHORT).show();
                        heartButton.setBackgroundResource(R.drawable.favourited);
                        favheartid = 0;

                    }finally{
                        locationdetails.close();
                    }
                }else{
                    //delete restaurant name and location from database
                    removeEvent(placeid);

                    Toast.makeText(FavMapViewFrag.this.getActivity(),"Removed from favourites", Toast.LENGTH_SHORT).show();
                    heartButton.setBackgroundResource(R.drawable.favouriteborder);
                    favheartid = 1;
                }

            }
        });

        restaurant.setOnClickListener(new TextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                findWebSite(placeid);
            }
        });

        locationdetails.close();
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
        // Add a marker on restaurant and move the camera
        placeLatLng = new LatLng(placeLatitude, placeLongitude);
        MarkerOptions option = new MarkerOptions().position(placeLatLng).title("Restaurant location");
        Marker userLocMarker = gMap.addMarker(option);
        //set text view to appropriate text
        address.setText(vicinity);
        restaurant.setText(placeName);
        userLocMarker.showInfoWindow();
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(placeLatitude, placeLongitude), 17.0f));
    }


    private void addEvent() {
        //wanted to check if record alr fav
        SQLiteDatabase db = locationdetails.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RestaurantName, placeName);
        values.put(RestaurantLocation, vicinity);
        values.put(PlaceID, placeid);
        values.put(RestaurantLat, placeLatitude);
        values.put(RestaurantLong, placeLongitude);
        db.insertOrThrow(TABLE_NAME, null, values);

    }

    private void removeEvent(String placeid){
        SQLiteDatabase db = locationdetails.getWritableDatabase();

        try{
            db.delete(TABLE_NAME,"PlaceID = ?",new String[]{placeid});
        }finally {
            db.close();
        }
    }

    private void findWebSite(String placeid){
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        googlePlacesUrl.append("placeid=").append(placeid);
        googlePlacesUrl.append("&key=AIzaSyCO4NSMZ1u7SGC4pmBO9bqSdaNRrzJuCoE");

        JsonObjectRequest request = new JsonObjectRequest( googlePlacesUrl.toString(),null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject result) {

                        Log.i(TAG, "onResponse: Result= " + result.toString());
                        try {
                            if(result.getString("status").equalsIgnoreCase("OK")){
                                if (result.getJSONObject("result").has("website")) {
                                    String website = result.getJSONObject("result").getString("website");

                                    Uri uri = Uri.parse(website);
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    startActivity(intent);


                                } else {
                                    Log.i(TAG, "No website found.");

                                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                                    String term = placeName + " "+vicinity;   // term which you want to search for
                                    intent.putExtra(SearchManager.QUERY, term);
                                    startActivity(intent);

                                }

                            }else if(result.getString("status").equalsIgnoreCase("ZERO_RESULTS")){
                                //TODO do something
                            }
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse: Error= " + error);
                        Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());
                    }
                });

        System.out.println(request + "REQUEST RIGHT HERE");
        AppController.getInstance().addToRequestQueue(request);
    }
}
