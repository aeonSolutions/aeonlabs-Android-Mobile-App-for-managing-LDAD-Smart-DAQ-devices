package aeonlabs.iot.data.acquisition.science.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.akexorcist.screenorientationhelper.ScreenOrientationHelper;
import com.google.android.gms.vision.barcode.Barcode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;

import aeonlabs.barcodescanner.common.libraries.database.DatabaseHelper;
import aeonlabs.barcodescanner.common.libraries.fragment.aboutFragment;
import aeonlabs.barcodescanner.common.libraries.fragment.openDataFragment;
import aeonlabs.barcodescanner.common.libraries.fragment.viewPagerFragment;
import aeonlabs.barcodescanner.common.libraries.model.barCode;

import aeonlabs.common.libraries.helper.FragmentManagement;
import aeonlabs.common.libraries.activities.activityBase;
import aeonlabs.common.libraries.activities.activityBaseObservable;
import aeonlabs.common.libraries.data.SessionData;
import aeonlabs.common.libraries.geoLocation.GeoLocation;
import aeonlabs.common.libraries.network.NetworkStateReceiver;

import aeonlabs.iot.data.acquisition.science.R;

public class MainActivity extends activityBase implements activityBaseObservable, ScreenOrientationHelper.ScreenOrientationChangeListener {

    private final ScreenOrientationHelper helper = new ScreenOrientationHelper(this);

    private Context context ;
    private Toolbar toolbar;


    public static final String BARCODE_KEY = "BARCODE";
    private Barcode barcodeResult;
    private final String TAG = MainActivity.class.getSimpleName() ;
    private final int MY_PERMISSION_REQUEST_CAMERA = 1001;
    public static FragmentManagement fragmentManagement;

    private Boolean fromIntent=false;
    private Boolean loading=true;

    private BroadcastReceiver mNetworkReceiver;
    private GeoLocation gps;
    private final Timer updateCloudServersTimer = new Timer();
    private DatabaseHelper db ;
    private ArrayList<Object> barCodeArrayList;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        helper.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        helper.onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    public void onScreenOrientationChanged(int orientation) {
        loading=true;
        ResumeLoadedActivity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loading=true;
        fromIntent=false;

        helper.onCreate(savedInstanceState);
        helper.setScreenOrientationChangeListener(this);

        setContentView(R.layout.activity_main);
        context = this;
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.sessionData.activity= this;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        this.sessionData.Network.setNetworkStatus( activeNetworkInfo != null && activeNetworkInfo.isConnected());

        mNetworkReceiver = new NetworkStateReceiver();

        //initialize the observers list for comm data between activity and fragments
        fragmentManagement= new FragmentManagement(this, R.id.frgament_content_frame);
        fragmentManagement.emptyFragemntsList();
        fragmentManagement.AddAndStartFragment(new viewPagerFragment(), null);
    }

    @Override
    protected void onResume(){
        super.onResume();
        ResumeLoadedActivity();
    }


    private  void ResumeLoadedActivity() {
        // check if is the 1st of 2 runs that happen when loading an activity
        if (fromIntent.equals(false) && loading.equals(true)) {


            int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
            if (result != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }

            result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE);
            if (result != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
            }

            result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
            if (result != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }

            result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
            if (result != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            }

            gps = new GeoLocation();
            if (gps.getLocation(this) == null) {
                Toast.makeText(this, getResources().getString(R.string.error_location_disabled), Toast.LENGTH_LONG).show();
                // Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                // startActivity(intent);
            }
            loading=false;
        }

    }

   //*******************************************************************************************
    public String getScanTime() {
     DateFormat timeFormat = new SimpleDateFormat("hh:mm a" , Locale.getDefault());
        return  timeFormat.format(new Date());
    }

   //*****************************************************************************************
    public String getScanDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy",Locale.getDefault());
        return dateFormat.format(new Date());
    }

   //****************************************************************************************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true ;
    }
  //*************************************************************************************
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case R.id.item_share:
                openShare();
                break;
            case R.id.item_rate_app:
                openRate();
                break ;
            case R.id.item_submit_bug:
                openSubmitBug();
                break ;
            case R.id.item_open_data:
                openOpenData();
                break;

            case R.id.item_about:
                openAbout();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    /*************************************************************************************************/
    private void openSubmitBug() {
        String to = "mtpsilva@gmail.com";
        String subject = R.string.app_name+" - Bug Report";

        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
        email.putExtra(Intent.EXTRA_SUBJECT, subject);
        email.setType("message/rfc822");
        startActivity(Intent.createChooser(email, "Choose an Email client :"));
    }
    /*************************************************************************************************/
    private void openRate() {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }
    /*************************************************************************************************/
    private void openShare() {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        String appLink = "https://play.google.com/store/apps/details?id="+context.getPackageName();
        sharingIntent.setType("text/plain");
        String shareBodyText = "Check Out The Cool Barcode Reader App \n Link: "+appLink +" \n" +
                " #Barcode #Android";
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT,"Barcode Reader Android App");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBodyText);
        startActivity(Intent.createChooser(sharingIntent, "Share"));
    }
    /*************************************************************************************************/
    private void openAbout() {
        aboutFragment aboutFragment = new aboutFragment();
        aboutFragment.show(getSupportFragmentManager().beginTransaction(), "dialog_about");
    }

    /*************************************************************************************************/
    private void openOpenData() {
        openDataFragment openDataFragment = new openDataFragment();
        openDataFragment.show(getSupportFragmentManager().beginTransaction(), "dialog_open_data");
    }
    /*************************************************************************************************/
    private void showDialog(final String scanContent, final String currentTime, final String currentDate) {
        LayoutInflater linf = LayoutInflater.from(this);
        final View inflator = linf.inflate(R.layout.dialog_barcode_result_dialog, null);

        final Dialog dialog = new Dialog(this, android.R.style.Theme_DeviceDefault_Light_Dialog);

        // Setting dialogview
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);

        dialog.getWindow().setLayout(375, 350);

        window.setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT);
        dialog.setTitle(null);
        dialog.setContentView(R.layout.dialog_barcode_result_dialog);

        dialog.setTitle(getResources().getString(R.string.dialog_title));
        dialog.setContentView(inflator);

        final Button save = inflator.findViewById(R.id.saveBtn);
        final Button cancel = inflator.findViewById(R.id.cancelBtn);
        final TextView message = inflator.findViewById(R.id.dialog_message_text);
        message.setText(getResources().getString(R.string.scanned_code) + ": "+scanContent);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();

                String longitude="";
                String latitude="";
                if (gps.getLocation(SessionData.activity) != null) {
                    latitude=String.valueOf(gps.getLatitude());
                    longitude=String.valueOf(gps.getLongitude());
                }
                DatabaseHelper databaseHelper = new DatabaseHelper(context);
                databaseHelper.addBarCode(new barCode(scanContent,currentTime,currentDate, longitude, latitude, "0","0"));
                Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                Toast.makeText(MainActivity.this, "Not Saved", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

/*************************************************************************************************/
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Are You Sure? ")
                .setTitle(R.string.exit_title);
        builder.setPositiveButton(R.string.ok_title, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                  MainActivity.this.finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                      dialog.dismiss();
            }
        });

        builder.show();
    }

    //**********************************************************************************************
    @Override
    public void notifyObserversFragment(String TAG, String... args) {

    }

    @Override
    public void notifyObserversActivity(String... args) {
        //fragmentManagement.removeCurrentLoadedFragment();
        //fragmentManagement.AddAndStartFragment(new ViewPagerFragment(), null);
        showDialog(args[0], getScanTime(),  getScanDate());

    }
}
