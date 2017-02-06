package com.mds.eatthis;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyEvent;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static android.R.attr.fragment;
import static android.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;
import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;
import static com.mds.eatthis.DatabaseConstants.PlaceID;
import static com.mds.eatthis.DatabaseConstants.RestaurantLat;
import static com.mds.eatthis.DatabaseConstants.RestaurantLong;
import static com.mds.eatthis.R.id.map;

//db Stuff
import static android.provider.BaseColumns._ID;
import static com.mds.eatthis.DatabaseConstants.TABLE_NAME;
import static com.mds.eatthis.DatabaseConstants.RestaurantName;
import static com.mds.eatthis.DatabaseConstants.RestaurantLocation;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
    String placeName, vicinity, placeid;

    int favheartid = 1;
    JSONObject nearbyPlaceResult;

    int newPlace = 9000; //A random number as long as is not 20 or below as JSONArray returns 20 or less results
    int oldPlace;
    LatLng latLngUserLoc;
    LatLng latLngNearbyPlace;
    double placeLatitude, placeLongitude;

    double currentLatitude, currentLongtitude;

    //Relating to database Stuff
    String RestaurantName1;
    String RestaurantLocation1;

    private static String[] FROM =
            {_ID, DatabaseConstants.RestaurantName, DatabaseConstants.RestaurantLocation, DatabaseConstants.PlaceID, DatabaseConstants.RestaurantLat, DatabaseConstants.RestaurantLong};
    private static String ORDER_BY = DatabaseConstants.RestaurantName + " DESC";
    private DatabaseEventsData locationdetails;

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
        mMapView = (MapView) v.findViewById(map);
        mMapView.onCreate(savedInstanceState);
        //start mapView immediately
        mMapView.onResume();
        //call onMapReady
        mMapView.getMapAsync(this);

        //getting nearby places from loadingfrag
        String nearbyPlaces = getArguments().getString("nearbyPlaces");
        try {
            nearbyPlaceResult = new JSONObject(nearbyPlaces);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        restaurant = (TextView) v.findViewById(R.id.restaurant);
        address = (TextView) v.findViewById(R.id.address);


        heartButton = (ImageButton) v.findViewById(R.id.heartborder);

        locationdetails = new DatabaseEventsData(MapViewFrag.this.getActivity());

        heartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(favheartid == 1){
                        try{
                            addEvent();

                            Toast.makeText(MapViewFrag.this.getActivity(),"Added to favourites", Toast.LENGTH_SHORT).show();
                            heartButton.setBackgroundResource(R.drawable.favourited);
                            favheartid = 0;

                        }finally{
                            locationdetails.close();
                        }


                }else{
                    //delete restaurant name and location from database
                    removeEvent(placeid);

                    Toast.makeText(MapViewFrag.this.getActivity(),"Removed from favourites", Toast.LENGTH_SHORT).show();
                    heartButton.setBackgroundResource(R.drawable.favouriteborder);
                    favheartid = 1;
                }

            }
        });

        changeRestaurant = (Button) v.findViewById(R.id.changerestaurant);
        changeRestaurant.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                favheartid = 1;
                heartButton.setBackgroundResource(R.drawable.favouriteborder);
                gMap.clear();
                mMapView.getMapAsync(MapViewFrag.this);
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
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LatLng", MODE_PRIVATE);
        currentLatitude = Double.parseDouble(sharedPreferences.getString("cLat", ""));
        currentLongtitude = Double.parseDouble(sharedPreferences.getString("cLng", ""));
        System.out.println(currentLatitude + currentLongtitude + "latlonginmapready");
        // Add a marker in user's coordinates and move the camera
        latLngUserLoc = new LatLng(currentLatitude, currentLongtitude);
        MarkerOptions option = new MarkerOptions().position(latLngUserLoc).title("You are here");
        Marker userLocMarker = gMap.addMarker(option);
        userLocMarker.showInfoWindow();

        //start plotting nearby place
        parseLocationResult(nearbyPlaceResult);

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //open google maps
                showDirections(currentLatitude,currentLongtitude,placeLatitude,placeLongitude);
            }
        });
    }


    private void parseLocationResult(JSONObject nearbyPlaceResult){
        try {
            JSONArray jsonArray = nearbyPlaceResult.getJSONArray("results");
            Random rand = new Random();
            if(newPlace == 9000){
                newPlace = rand.nextInt(jsonArray.length() - 0) + 0;
                oldPlace = newPlace;
            }else{
                //make sure new place generated is a not the same as the previous one
                if(jsonArray.length() != 1){ //if there is more than 1 place switch restaurant
                    while(newPlace == oldPlace){
                        newPlace = rand.nextInt(jsonArray.length() - 0) + 0;
                    }
                }
                oldPlace = newPlace;
            }
            System.out.println(newPlace);
            JSONObject place = jsonArray.getJSONObject(newPlace);
            placeName = place.getString("name");
            vicinity = place.getString("vicinity");
            placeid = place.getString("place_id");
            placeLatitude = place.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
            placeLongitude = place.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
            //Underline restaurant name
            SpannableString content = new SpannableString(placeName);
            content.setSpan(new UnderlineSpan(),0,placeName.length(),0);
            restaurant.setText(content);
            address.setText(vicinity);
            RestaurantName1 = placeName;
            RestaurantLocation1 = vicinity;
            //Cursor cursor = getEvents();
            // Function getEvents is returning a null value;


            MarkerOptions markerOptions = new MarkerOptions();
            latLngNearbyPlace = new LatLng(placeLatitude, placeLongitude);
            markerOptions.position(latLngNearbyPlace);
            markerOptions.title(placeName);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.eticon));
            //add marker on nearby place
            gMap.addMarker(markerOptions);

            //get directions from user location to nearby place
            getDirections(latLngUserLoc,latLngNearbyPlace);

            // Prepared statement
            SQLiteDatabase db = locationdetails.getReadableDatabase();
            String table = DatabaseConstants.TABLE_NAME;
            String[] columnsToReturn = {DatabaseConstants.PlaceID};
            String selection = DatabaseConstants.PlaceID + "=?";
            String[] selectionArgs = {placeid};
            Cursor cursor = db.query(table, columnsToReturn, selection, selectionArgs, null, null, null);

            if(cursor.moveToNext()) {
                heartButton.setBackgroundResource(R.drawable.favourited);
                favheartid = 0;
            }


        }catch(JSONException e){
            e.printStackTrace();
        }

    }

    public void showDirections(double lat, double lng, double lat1, double lng1) {
        final Intent intent = new
                Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?" +
                "saddr=" + lat + "," + lng + "&daddr=" + lat1 + "," +
                lng1));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);

    }

    private void getDirections(LatLng origin,LatLng dest){
        StringBuilder googleDirectionsUrl =
                new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin=").append(origin.latitude+",").append(origin.longitude);
        googleDirectionsUrl.append("&destination=").append(dest.latitude+",").append(dest.longitude);
        googleDirectionsUrl.append("&sensor=false");
        googleDirectionsUrl.append("&mode=walking");
        //For testing
        System.out.println(googleDirectionsUrl.toString());

        JsonObjectRequest request = new JsonObjectRequest(googleDirectionsUrl.toString() ,null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject result) {

                        Log.i(TAG, "onResponse: Result= " + result.toString());
                        try{
                            //TODO remove if else
                            if(result.getString("status").equalsIgnoreCase("OK")){
                                System.out.println("INSIDE getDirectionsURL");

                                //start parsing directions
                                parsePolyLine(result);

                            }else if(result.getString("status").equalsIgnoreCase("ZERO_RESULTS")){
                                //TODO do something
                            }
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse: Error= " + error);
                        Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());
                    }
                });
        AppController.getInstance().addToRequestQueue(request);
    }

    private void parsePolyLine(JSONObject polyLineResult){
        System.out.println("in parsepolyline " + polyLineResult);

        List<List<HashMap<String, String>>> routes = null;
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;

        JSONParser parser = new JSONParser();
        // Starts parsing data
        routes = parser.parse(polyLineResult);
        // Traversing through all the routes
        for(int i=0;i<routes.size();i++){
            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = routes.get(i);

            // Fetching all the points in i-th route
            for(int j=0;j<path.size();j++){
                HashMap<String,String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(20);
            lineOptions.color(getContext().getResources().getColor(R.color.linecolor));
        }

        // Drawing polyline in the Google Map for the i-th route
        gMap.addPolyline(lineOptions);
        
        //After everything is set, set the boundary using the markers and polyline as reference so that everything can be shown on the map
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for(LatLng point : points){
            builder.include(point);
        }
        builder.include(latLngUserLoc);
        builder.include(latLngNearbyPlace);

        LatLngBounds bounds = builder.build();
        gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,150));

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

    public void replaceFragment(Fragment fragment){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
    }

    @Override
    public void onResume() {

        super.onResume();

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){

                    Fragment fragment = new SearchFrag();
                    replaceFragment(fragment);

                    return true;

                }

                return false;
            }
        });
    }

}
