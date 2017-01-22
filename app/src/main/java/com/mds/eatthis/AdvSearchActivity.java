package com.mds.eatthis;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

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
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.Done:
                break;
        }
    }
}
