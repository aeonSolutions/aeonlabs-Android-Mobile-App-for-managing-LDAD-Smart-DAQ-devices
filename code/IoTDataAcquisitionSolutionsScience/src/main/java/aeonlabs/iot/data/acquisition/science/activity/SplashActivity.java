package aeonlabs.iot.data.acquisition.science.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import aeonlabs.iot.data.acquisition.science.R;
import aeonlabs.common.libraries.System.GetDeviceSerialNumber;
import aeonlabs.common.libraries.data.SessionData;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initializing the Google Admob SDK
        MobileAds.initialize (this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete( InitializationStatus initializationStatus ) {
                //Showing a simple Toast Message to the user when The Google AdMob Sdk Initialization is Completed
            }
        });

        GetDeviceSerialNumber serial = new GetDeviceSerialNumber(this);
        SessionData.System.DEVICE_SERIAL= serial.get();

        startMainActivity();
    }

    private void startMainActivity() {
        // Start Main Screen
        Intent i = new Intent(SplashActivity.this, WelcomeActivity.class);
        startActivity(i);
        this.finish();
    }
}
