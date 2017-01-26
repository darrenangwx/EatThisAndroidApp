package com.mds.eatthis;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Created by Darren on 1/22/2017.
 */

public class AdvSearchActivity extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advsearch);

        View done = findViewById(R.id.Done);
        done.setOnClickListener(this);

        Spinner restaurantspinner = (Spinner)findViewById(R.id.restaurantspinner);
        Spinner ratingspinner = (Spinner) findViewById(R.id.ratingspinner);

        SharedPreferences sharedPref = getSharedPreferences("SpinnerData",MODE_PRIVATE);
        String restaurant = sharedPref.getString("cusine","");
        String rating = sharedPref.getString("rating","");


        ArrayAdapter myAdapRS = (ArrayAdapter)restaurantspinner.getAdapter();
        ArrayAdapter myAdapR = (ArrayAdapter)ratingspinner.getAdapter();
        int spinnerPositionRS = myAdapRS.getPosition(restaurant);
        int spinnerPositionR = myAdapR.getPosition(rating);
        restaurantspinner.setSelection(spinnerPositionRS);
        ratingspinner.setSelection(spinnerPositionR);

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.Done:
                Spinner restaurantspinner = (Spinner)findViewById(R.id.restaurantspinner);
                Spinner ratingspinner = (Spinner) findViewById(R.id.ratingspinner);

                String cusine = restaurantspinner.getSelectedItem().toString();
                String rating = ratingspinner.getSelectedItem().toString();
                SharedPreferences sharedPref = getSharedPreferences("SpinnerData",0);
                SharedPreferences.Editor prefEditor = sharedPref.edit();
                prefEditor.putString("cusine",cusine);
                prefEditor.putString("rating",rating);
                prefEditor.commit();

                finish();
                break;
        }
    }
}
