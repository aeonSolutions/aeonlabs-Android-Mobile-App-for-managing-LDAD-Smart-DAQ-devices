package aeonlabs.iot.data.acquisition.enterprise.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import aeonlabs.barcodescanner.common.libraries.activity.BarCodeScanActivity;
import aeonlabs.barcodescanner.common.libraries.utils.CheckAPIconnectivity;
import aeonlabs.common.libraries.userInterface.AeonLabsViewPager;
import aeonlabs.common.libraries.activities.activityBase;
import aeonlabs.common.libraries.activities.activityBaseObservable;
import aeonlabs.common.libraries.libraries.utils.prefManager;
import aeonlabs.iot.data.acquisition.enterprise.R;
import aeonlabs.iot.data.acquisition.enterprise.database.SettingsDatabaseHelper;
import aeonlabs.iot.data.acquisition.enterprise.model.SettingsDB;

public class WelcomeActivity extends activityBase  implements activityBaseObservable {

    private AeonLabsViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnSkip, btnNext;
    private prefManager prefManager;
    private activityBase thisActivity;

    private final Boolean AuthDone=false;
    private final Boolean paymentServiceDone=false;
    private final Boolean contactsDone=false;

    private final int GOOGLE_SIGN_POS=3;
    private final int PHONE_CONTACTS_POS=4;
    private final int PAYMENT_SERVICE_POS=5;

    private String QrCodeUrl="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        setContentView(R.layout.activity_welcome);

        viewPager = findViewById(R.id.view_pager);
        dotsLayout = findViewById(R.id.layoutDots);
        btnSkip = findViewById(R.id.btn_skip);
        btnNext = findViewById(R.id.btn_next);

        // Checking for first time launch - before calling setContentView()
        prefManager = new prefManager(this);
        if (!prefManager.isFirstTimeLaunch() & extras == null) {
            launchHomeScreen();
            return;
        }else{
            // layouts of all welcome sliders
            // add few more layouts if you want
            layouts = new int[]{
                    R.layout.welcome_slide1,
                    R.layout.welcome_slide2,
                    R.layout.welcome_slide3};
        }

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        thisActivity=this;
        // adding bottom dots
        addBottomDots(0);
        // making notification bar transparent
        changeStatusBarColor();

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        if (extras != null) {
            viewPager.setCurrentItem(layouts.length-1);
            saveCloudServerUrl(extras.getString("barcode"));
        }
        viewPager.setPagingEnabled(true);

        btnSkip.setVisibility(View.GONE);

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewPager.getCurrentItem()==1){
                    OpenUrlDialog();
                }else{
                    moveSlide(-1);
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewPager.getCurrentItem()==1){
                    qrCodeInfo();
                }else{
                    moveSlide(+1);
                }
            }
        });

        int result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        if (result != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }
        result = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET);
        if (result != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
        }

    }

    /****************************************************************************/
    public void saveCloudServerUrl(String url) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(thisActivity);
        // Setting Dialog Title
        alertDialog.setTitle(getResources().getString(R.string.dialog_clould_url_title));
        // Setting Dialog Message
        alertDialog.setMessage(getResources().getString(R.string.dialog_clould_url_are_you_sure)+" "+ url);
        // On pressing the Settings button.
        alertDialog.setPositiveButton(getResources().getString(R.string.alertbox_continue), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                dialog.cancel();
                // check if address is valid
                thisActivity.displayProgress("",getResources().getString(R.string.commServer_connect_msg), false);
                QrCodeUrl=url;
                CheckAPIconnectivity checkAPIconnectivity= new CheckAPIconnectivity(thisActivity, url);
                checkAPIconnectivity.startCheck();
            }
        });

        // On pressing the cancel button
        alertDialog.setNegativeButton(getResources().getString(R.string.alertbox_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }

    /******************************************************************************/
    @Override
    public void notifyObserversActivity(String... args) {
        if(args[0].equals("BarCodeScanActivity")){
            // save to DB
            args[0]="saved";
            SettingsDatabaseHelper databaseHelper = new SettingsDatabaseHelper(this);
            databaseHelper.addSettings(new SettingsDB(args[1]));
            moveSlide(+1);
        } else if(args[1].equals("200")){
            // save to DB
            SettingsDatabaseHelper databaseHelper = new SettingsDatabaseHelper(this);
            databaseHelper.addSettings(new SettingsDB(QrCodeUrl));
            moveSlide(+1);
        }
        Toast.makeText(thisActivity, args[0], Toast.LENGTH_SHORT).show();
        QrCodeUrl="";

        thisActivity.dismissProgress();
    }

/****************************************************************************/
    public void qrCodeInfo() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(thisActivity);
        // Setting Dialog Title
        alertDialog.setTitle(getResources().getString(R.string.dialog_clould_url_title));
        // Setting Dialog Message
        alertDialog.setMessage(getResources().getString(R.string.dialog_clould_url_qr_msg));
        // On pressing the Settings button.
        alertDialog.setPositiveButton(getResources().getString(R.string.alertbox_continue), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                // Start Main Screen
                Intent i = new Intent(WelcomeActivity.this, BarCodeScanActivity.class);
                startActivity(i);
            }
        });

        // On pressing the cancel button
        alertDialog.setNegativeButton(getResources().getString(R.string.alertbox_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
/***********************************************************************************/
public void OpenUrlDialog(){
    LayoutInflater linf = LayoutInflater.from(this);
    final View inflator = linf.inflate(R.layout.dialog_cloud_url, null);
    AlertDialog.Builder alert = new AlertDialog.Builder(this);

    alert.setTitle(getResources().getString(R.string.dialog_clould_url_title));
    alert.setMessage(getResources().getString(R.string.dialog_clould_url_msg));
    alert.setView(inflator);

    final EditText url = inflator.findViewById(R.id.dialog_clould_url);

    alert.setPositiveButton(getResources().getString(R.string.button_save), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            if(!url.getText().toString().equals("")){
                dialog.cancel();
                thisActivity.displayProgress("",getResources().getString(R.string.commServer_connect_msg), false);
                // check if address is valid
                QrCodeUrl=url.getText().toString();
                CheckAPIconnectivity checkAPIconnectivity= new CheckAPIconnectivity(thisActivity, url.getText().toString());
                checkAPIconnectivity.startCheck();
            }else{
                Toast.makeText(thisActivity, getResources().getString(R.string.dialog_clould_url_error), Toast.LENGTH_SHORT).show();
            }

        }
    });

    alert.setNegativeButton(getResources().getString(R.string.alertbox_cancel), new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            dialog.cancel();
        }
    });
    alert.show();
}


 /**************************************************************************************************/
private void moveSlide(int move){
    // checking for last page
    // if last page home screen will be launched
    int current = getItem(move);
    if (current < layouts.length) {
        viewPager.setCurrentItem(current);
        // move to next screen
        viewPager.setPagingEnabled(true);
    } else {
        launchHomeScreen();
    }
}

    /**************************************************************************************************/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**************************************************************************************************/
    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    /**************************************************************************************************/
    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    /**************************************************************************************************/
    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch(false);
        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
        finish();
    }

    /**************************************************************************************************/
    //	viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);
            viewPager.setPagingEnabled(true);
            if(position==0){
                btnSkip.setVisibility(View.GONE);
            }else{
                btnSkip.setVisibility(View.VISIBLE);
            }

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                btnNext.setText(getString(R.string.gotIt));
                btnSkip.setVisibility(View.GONE);
            }else if(position== 1){
                viewPager.setPagingEnabled(false);
                btnSkip.setText(getString(R.string.manual_entry));
                btnNext.setText(getString(R.string.qr_code_entry));
            } else {
                // still pages are left
                btnNext.setText(getString(R.string.next));
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };
/**************************************************************************************************/
    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }
/**************************************************************************************************/

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}
