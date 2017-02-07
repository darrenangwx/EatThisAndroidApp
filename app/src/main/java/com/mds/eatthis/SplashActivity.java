package com.mds.eatthis;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Darren, Ming Kiang and Stanley.
 */

public class SplashActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread splashThread = new Thread() {
            @Override
            public void run(){
                try {
                    int waited = 0;
                    while (waited < 1000) {
                        sleep(100);
                        waited +=200;
                    }
                } catch(InterruptedException e){

                }finally {
                    finish();
                    Intent i = new Intent();
                    i.setClassName("com.mds.eatthis", "com.mds.eatthis.Navigation");
                    startActivity(i);
                }
            }
        };
        splashThread.start();
    }
}
