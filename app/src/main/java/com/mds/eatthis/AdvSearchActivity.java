package com.mds.eatthis;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

/**
 * Created by Darren, Ming Kiang and Stanley.
 */

public class AdvSearchActivity extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advsearch);

        View done = findViewById(R.id.Done);
        done.setOnClickListener(this);

        Spinner restaurantspinner = (Spinner)findViewById(R.id.restaurantspinner);
        Spinner radiusspinner = (Spinner) findViewById(R.id.radiusspinner);

        SharedPreferences sharedPref = getSharedPreferences("SpinnerData",MODE_PRIVATE);
        String restaurant = sharedPref.getString("cuisine","");
        String radius = sharedPref.getString("radius","");


        ArrayAdapter myAdapRS = (ArrayAdapter)restaurantspinner.getAdapter();
        ArrayAdapter myAdapR = (ArrayAdapter)radiusspinner.getAdapter();
        int spinnerPositionRS = myAdapRS.getPosition(restaurant);
        int spinnerPositionR = myAdapR.getPosition(radius);
        restaurantspinner.setSelection(spinnerPositionRS);
        radiusspinner.setSelection(spinnerPositionR);

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.Done:
                Spinner restaurantspinner = (Spinner)findViewById(R.id.restaurantspinner);
                Spinner radiusspinner = (Spinner) findViewById(R.id.radiusspinner);

                String cuisine = restaurantspinner.getSelectedItem().toString();
                String radius = radiusspinner.getSelectedItem().toString();
                SharedPreferences sharedPref = getSharedPreferences("SpinnerData",0);
                SharedPreferences.Editor prefEditor = sharedPref.edit();
                prefEditor.putString("cuisine",cuisine);
                prefEditor.putString("radius",radius);
                prefEditor.commit();

                finish();
                break;
        }
    }
}
