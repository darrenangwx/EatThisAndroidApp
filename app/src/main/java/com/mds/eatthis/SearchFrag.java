package com.mds.eatthis;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;
import static com.google.android.gms.wearable.DataMap.TAG;

/**
 * Created by Darren on 1/22/2017.
 */

public class SearchFrag extends Fragment {
    EditText editText;
    Switch switchffnm;
    CheckBox checkBox;
    Button buttonGo;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        View v = inflater.inflate(R.layout.fragment_menu_search, container, false);

        //for text box
        editText = (EditText) v.findViewById(R.id.editText5);
        editText.setVisibility(View.VISIBLE);

        //for find food near me switch
        switchffnm = (Switch) v.findViewById(R.id.switchffnm);
        switchffnm.setOnClickListener(new Switch.OnClickListener() {
            @Override
            public void onClick(View view) {
                GPS(view);
            }
        });

        //for adv search checkbox
        checkBox = (CheckBox) v.findViewById(R.id.checkBox);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((CheckBox) view).isChecked()){
                    startActivity(new Intent(SearchFrag.this.getActivity(), AdvSearchActivity.class));
                }
            }
        });

        //For autocomplete
        editText.setOnClickListener(new EditText.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                    .build(getActivity());
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                    
                } catch (GooglePlayServicesRepairableException e) {
                    // TODO: Handle the error.
                    System.out.println("error");
                } catch (GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                    System.out.println("error");
                }
            }

        });

        //for Go button
        buttonGo = (Button) v.findViewById(R.id.buttonGo);
        buttonGo.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new MapViewFrag();
                replaceFragment(fragment);
                hideKeyboard(getContext());

            }
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(SearchFrag.this.getActivity(), data);

                editText.setText(place.getName());
                Log.i(TAG, "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(SearchFrag.this.getActivity(), data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    //replace fragment when button is clicked
    public void replaceFragment(Fragment fragment){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Search");
    }

    public void GPS(View view){
        boolean checked = ((Switch)view).isChecked();
        if(checked){
            editText.setVisibility(View.INVISIBLE);
            LocationManager locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Toast.makeText(this.getActivity(), "Using GPS", Toast.LENGTH_SHORT).show();
            }else{

                showGPSDisabledAlertToUser();
            }
        }
        else{
            editText.setVisibility(View.VISIBLE);
        }

    }

    //Show popup to ask for gps enabling
    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.getActivity());
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Go to Settings",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id){
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


    public static void hideKeyboard(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }




}
