package com.GPSspeed;

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextPaint;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.activelook.activelooksdk.Glasses;
import com.activelook.activelooksdk.types.DeviceInformation;
import com.activelook.activelooksdk.types.ImgStreamFormat;
import com.activelook.activelooksdk.types.Rotation;
import com.activelook.activelooksdk.types.holdFlushAction;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Glasses connectedGlasses;
    private TextView glassesidTextView, fwTextView, largeText;
    private TextView GlassesBattery, longitudeTV, lattitudeTV, altitudeTV,
            altitudeMiniTV, altitudeMaxiTV, speedTV, courseTV, maxSpeedTV, chgeUnitTV;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch sensorSwitch;
    private SeekBar luminanceSeekBar;
    private Spinner unit;
    private ArrayAdapter adapter_unit;
    private String unitShort = "km/h", prev_packageName="", prev_titleData="", prev_textData="";
    private boolean notification = false;
    private int counter=0, gbattery=0, notif_cntr=0, topmrg=0, botmrg=0, lftmrg=0, rgtmrg=0;
    private double latitude=0.0, longitude=0.0, altitude=0.0,  altitudeAccuracy=0.0,
            course=0.0, courseAccuracy=0.0, unitRatio=3.6, speed=0.0, maxSpeed=0.0;
    List<Double> latitudeList= new ArrayList<Double>(), longitudeList= new ArrayList<Double>(),
            altitudeList= new ArrayList<Double>();
    Handler clockHandler = new Handler();
    Runnable clockRunnable;
    Handler messageHandler = new Handler();
    Runnable messageRunnable;
    // GPSTracker class
    GPSTracker gps;

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
//    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        /*
         * Check location permission (needed for BLE scan)
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN}, 0); }

        // If BT is not on, request that it be enabled.
        if (!mBluetoothAdapter.isEnabled()) {Toast.makeText(getApplicationContext(),
                "Your BlueTooth is not open !!!", Toast.LENGTH_LONG).show();}

        if (savedInstanceState != null && ((DemoApp) this.getApplication()).isConnected()) {
            this.connectedGlasses = savedInstanceState.getParcelable("connectedGlasses");
            this.connectedGlasses.setOnDisconnected(glasses -> {
                glasses.disconnect();
                MainActivity.this.disconnect();
            });
        }
        setContentView(R.layout.activity_scrolling);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toolbar toolbar = this.findViewById(R.id.toolbar);

        glassesidTextView = this.findViewById(R.id.glasses_id);
        GlassesBattery = this.findViewById(R.id.GlassesBattery);
        fwTextView = this.findViewById(R.id.fw);
        largeText = this.findViewById(R.id.largeText);
        luminanceSeekBar = this.findViewById(R.id.luminanceSeekBar);
        sensorSwitch = this.findViewById(R.id.sensorSwitch);
        lattitudeTV = this.findViewById(R.id.latitude);
        longitudeTV = this.findViewById(R.id.longitude);
        altitudeTV = this.findViewById(R.id.altitude);
        altitudeMaxiTV = this.findViewById(R.id.altitudeMaxi);
        altitudeMiniTV = this.findViewById(R.id.altitudeMini);
        courseTV = this.findViewById(R.id.course);
        speedTV = this.findViewById(R.id.speed);
        chgeUnitTV = this.findViewById(R.id.chgeunit);
        maxSpeedTV = this.findViewById(R.id.maxSpeed);
        unit = this.findViewById(R.id.unit);
        String[] choices = getResources().getStringArray(R.array.unit_array);
        adapter_unit = new ArrayAdapter(this, R.layout.list_item, choices);
        unit.setAdapter(adapter_unit);
        adapter_unit.setDropDownViewResource(R.layout.spinner_dropdown_item);

        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());
        this.updateVisibility();

        // If BT is not on, request that it be enabled.
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {Toast.makeText(getApplicationContext(),
                    "Your BLUETOOTH is not open !!!/n>>>relaunch the application", Toast.LENGTH_LONG).show();}
        this.findViewById(R.id.scan).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ScanningActivity.class);
            MainActivity.this.startActivityForResult(intent, Activity.RESULT_FIRST_USER);
        });

        final Glasses g = this.connectedGlasses;
        if (g != null) {g.clear();
            clockRunnable = new Runnable() { @Override
                public void run() {miseAJourDeLaPosition(); clockHandler.postDelayed(this, 10000);} };
            clockHandler.removeCallbacks(clockRunnable);
            clockHandler.postDelayed(clockRunnable, 10000); // on redemande toutes les 10000ms
        }

        NotificationManager notif = (NotificationManager)
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (!notif.isNotificationPolicyAccessGranted()) {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("Msg"));

        // select the speed unit
        unit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(ContextCompat.getColor(MainActivity.this,R.color.grey));
                unitRatio = 3.6; unitShort = " km/h";
                if (position==0) {unitRatio = 3.6;
                    unitShort = getResources().getStringArray(R.array.unit_array)[0];}
                if (position==1) {unitRatio = 1.0;
                    unitShort = getResources().getStringArray(R.array.unit_array)[1];}
                if (position==2) {unitRatio = 2.236936292;
                    unitShort = getResources().getStringArray(R.array.unit_array)[2];}
                if (position==3) {unitRatio = 1.943844492;
                    unitShort = getResources().getStringArray(R.array.unit_array)[3];}
                chgeUnitTV.setText(getText(R.string.click_on_km_hr_to_change_the_speed_unit) + "  " + unitShort + "  " + getText(R.string.to_change_the_speed_unit));
            }
            public void onNothingSelected(AdapterView<?> parent) { unitRatio = 3.6; }
        });

        this.findViewById(R.id.resetMax).setOnClickListener(view -> {
            latitudeList.clear(); longitudeList.clear(); altitudeList.clear();
            maxSpeed=0.0;  maxSpeedTV.setText("0.0 " + unitShort);});

        this.findViewById(R.id.button_disconnect).setOnClickListener(view -> {
            MainActivity.this.sensorSwitch(true);
            connectedGlasses.sensor(true);
            MainActivity.this.connectedGlasses.disconnect();
            MainActivity.this.connectedGlasses = null;
            MainActivity.this.updateVisibility();
            this.snack();
        });
        sensorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> MainActivity.this.sensorSwitch(isChecked));
        // select the luminance brightness
        luminanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
                MainActivity.this.lumaButton(progressChangedValue);
            }

            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

    }

//---------------------------------------------------------------------------------

    public final BroadcastReceiver onNotice = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            final Glasses g = connectedGlasses;
            String packageName = intent.getStringExtra("package");
            String titleData = intent.getStringExtra("title");
            String textData = intent.getStringExtra("text");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
            String time = sdf2.format(new Date());
            Log.d("MainActivity", "receivedNotif : " + time + " : " + packageName
                    + " : " + titleData + " : " + textData);
            boolean displayData = true;


            // filter some messages
            if (packageName.equals("com.android.systemui")) {displayData = false;}
            if (packageName.equals("com.android.vending")) {displayData = false;}
            if (packageName.equals(prev_packageName) && titleData.equals(prev_titleData)
                    && textData.equals(prev_textData)) {displayData = false;}

            if(displayData) {
                prev_packageName = packageName; prev_titleData = titleData; prev_textData = textData;

                //================= PREPARE TO WRITE IN THE GLASSES

                // shape the notification text then turn into image
                String textfr;
                textfr = titleData + " : " + textData + ' ';
                textfr = textfr.replaceAll("\n\r"," ");
                textfr = textfr.replaceAll("\r\n"," ");
                textfr = textfr.replaceAll("\n"," ");
                textfr = textfr.replaceAll("\t"," ");
                textfr = textfr.replaceAll("\r"," ");
                for (int j = 0; j < 32; j++) {
                    textfr = textfr.replaceAll(String.valueOf((char) j),""); }
                Bitmap txtimg = textAsBitmap(textfr,22);
                int linWidth = txtimg.getWidth();

                notification = true; notif_cntr=0;
                messageHandler.removeCallbacks(messageRunnable);
                int delay = 2 * 500; // 2=shift notif_cntr++ ; 500 = delay of 5 seconds
                final Bitmap[] sub_txtimg = new Bitmap[1];
                messageRunnable = new Runnable() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void run() {
                        if (g!=null && notification && linWidth > 0) {
                            // write NOTIFICATION
                            if (linWidth < 305 && notif_cntr == 0) {
                                g.imgStream(txtimg, ImgStreamFormat.MONO_4BPP_HEATSHRINK,
                                    (short)(304+notif_cntr-txtimg.getWidth()), (short) 231);}
                            if (linWidth > notif_cntr+303) {
                                Log.d("MainActivity", "receivedNotif : " + sdf2.format(new Date())
                                        + " notif_cntr = " + notif_cntr + " - linWidth = " + linWidth );
                                g.imgStream(Bitmap.createBitmap(txtimg, notif_cntr, 0, 304,24),
                                        ImgStreamFormat.MONO_4BPP_HEATSHRINK, (short) 0, (short) 231);}
                            // end of NOTIFICATION
                            if ((linWidth < 305 && notif_cntr > delay) || (linWidth > 304 && notif_cntr > linWidth-303 + delay)) {
                                notification=false;
                                notif_cntr =0;
                                messageHandler.removeCallbacks(messageRunnable);
                                g.color((byte)0); g.rectf((short)0,(short)224,(short)304,(short)255); g.color((byte)15);
                                displayClock();}
                        }
                        notif_cntr++;
                        notif_cntr++; // shift by 2 pixels the message
//                        notif_cntr++; // shift by 3 pixels the message
                        messageHandler.postDelayed(this, 200);
                    }
                }; // new message Runnable

                messageHandler.removeCallbacks(messageRunnable);
                messageHandler.postDelayed(messageRunnable,200); // on redemande toutes les 200ms
            }
        }
    };

//---------------------------------------------------------------------------------

    @SuppressLint("DefaultLocale")
    private void updateVisibility() {
        final Glasses g = this.connectedGlasses;
        if (g == null) {
            this.findViewById(R.id.connected_content).setVisibility(View.GONE);
            this.findViewById(R.id.disconnected_content).setVisibility(View.VISIBLE);
        } else {
            this.findViewById(R.id.connected_content).setVisibility(View.VISIBLE);
            this.findViewById(R.id.disconnected_content).setVisibility(View.GONE);
            g.clear(); displayClock();
            try {g.loadConfiguration(new BufferedReader(
                    new InputStreamReader(getAssets().open("fontGPSspeed2.txt"))));}
            catch (IOException e) {e.printStackTrace();}

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {miseAJourDeLaPosition();}
//            g.txt(new Point(250, 144), Rotation.TOP_LR, (byte) 2, (byte) 0x0F, "GPS speed");
        }
    }

//  UPDATED EVERY SECOND :
// ---------------------------------------------------------------------
//    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void miseAJourDeLaPosition() {
        final Glasses g = this.connectedGlasses;
        gps = new GPSTracker(this, false);
        double latitudeMin, latitudeMax, longitudeMin, longitudeMax,
                altitudeMin, altitudeMax, scale1, scale2, x1, y1, x2, y2;
        int nbPoints, mapSize=127, xc=275, yc=98, lencomp=28, x3, y3, x4, y4, x5, y5;

        if (gbattery !=0 ) {GlassesBattery.setText("Glasses battery : "+String.format("%d",gbattery)+"%");}

        // GPS data - speed - course
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            altitude = gps.getAltitude();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                altitudeAccuracy = gps.getVerticalAccuracyMeters();
                courseAccuracy = gps.getBearingAccuracyDegrees();
            }
            speed = gps.getSpeed();
            course = gps.getBearing();
            if (speed > maxSpeed) {maxSpeed = speed;}

            latitudeList.add(latitude); latitudeMin = getMinValue(latitudeList);
            latitudeMax = getMaxValue(latitudeList);
            longitudeList.add(longitude); longitudeMin = getMinValue(longitudeList);
            longitudeMax = getMaxValue(longitudeList);
            altitudeList.add(altitude); altitudeMin = getMinValue(altitudeList);
            altitudeMax = getMaxValue(altitudeList);
            scale1 = max(max(latitudeMax-latitudeMin, longitudeMax-longitudeMin),0.001);
            // recalculate latitudeMin and longitudeMin to center the way shape
            latitudeMin = (latitudeMin + latitudeMax - scale1) / 2;
            longitudeMin = (longitudeMin + longitudeMax - scale1) / 2;
            nbPoints = latitudeList.size();

            lattitudeTV.setText(getString(R.string.latitude)+": " + String.format("%3.4f", latitude) + "°");
            longitudeTV.setText(getString(R.string.longitude)+": " + String.format("%3.4f", longitude) + "°");
            altitudeTV.setText(getString(R.string.altitude)+": " + String.format("%.1f", altitude) + " "
                    + getString(R.string.alt_short) + "   +/-" + String.format("%.1f", altitudeAccuracy));
            altitudeMaxiTV.setText(getString(R.string.max)+": " + String.format("%.1f", altitudeMax));
            altitudeMiniTV.setText(getString(R.string.min)+": " + String.format("%.1f", altitudeMin));
            courseTV.setText(getString(R.string.bearing)+": " + String.format("%.1f", course) + "°   +/-"
                     + String.format("%.1f", courseAccuracy) + "°"); // orientation
            speedTV.setText(getString(R.string.speed)+": " + String.format("%.2f", unitRatio * speed));   // Speed
            maxSpeedTV.setText(getString(R.string.fastest)+ " "+ getString(R.string.spd_short)+ ": "
                    + String.format("%.2f", unitRatio * maxSpeed) + unitShort); // Maximum Speed

            if (g != null) {
                g.holdFlush(holdFlushAction.HOLD);
                displayClock();
                // clear the square with the way and the course orientation
                g.cfgSet("cfgGPSspeed");
                g.color((byte) 0);
                counter ++; // clear the whole screen every minute, in case of problem ...
                if(counter==6) {g.rectf(new Point(0, 0), new Point(303, 226)); counter=0;}
                g.rectf(new Point(0, 224-mapSize), new Point(mapSize+1, 226));
                g.rectf(new Point(0, 69), new Point(244, 96));
                g.circf(new Point(xc, yc), (byte) (lencomp+1));
                g.color((byte) 1);
                // square for the longitude/latitude way shape
                g.rect(new Point(1, 225-mapSize), new Point(mapSize, 225));
                // rectangle for the altitude shape
                g.rect(new Point(1, 70), new Point(243, 95));
                // circle for the course orientation
                g.circ(new Point(xc, yc), (byte) lencomp);
                g.color((byte) 13);
                // trace all lines of the way
                if (nbPoints > 3 && (latitudeMax-latitudeMin) > 0.0001 && (longitudeMax-longitudeMin) > 0.0001) {
                    y1 = 225 - mapSize + ((latitudeList.get(0)-latitudeMin)*mapSize/scale1);
                    x1 = mapSize - ((longitudeList.get(0)-longitudeMin)*mapSize/scale1);
                    // starting Point with a 3 points full circle
                    g.circf(new Point((int) (Math.round(x1-1.0)), (int) (Math.round(y1-1))), (byte) 3);
                    // drawing the shape of the way
                    for(int d = 0; d < nbPoints-1; d++){
                        y1 = 225 - mapSize + ((latitudeList.get(d)-latitudeMin)*mapSize/scale1);
                        x1 = mapSize - ((longitudeList.get(d)-longitudeMin)*mapSize/scale1);
                        y2 = 225 - mapSize + ((latitudeList.get(d+1)-latitudeMin)*mapSize/scale1);
                        x2 = mapSize -((longitudeList.get(d+1)-longitudeMin)*mapSize/scale1);
                        g.line(new Point((int)Math.round(x1), (int)Math.round(y1)),
                                new Point((int)Math.round(x2), (int)Math.round(y2)));
                    }
                }
                // recalculate altitudeMin to center the altitude shape
                scale2 = max(altitudeMax - altitudeMin, 10);
                altitudeMin = (altitudeMin + altitudeMax - scale2) / 2;
                // trace all lines of the altitude
                if (nbPoints > 3) {
                    // drawing the shape of the way
                    g.color((byte) 8);
                    for(int d = 0; d < nbPoints-1; d++){
                        y1 = 70 + ((altitudeList.get(d)-altitudeMin)*26/scale2);
                        x1 = 243.0 - d * 242 / max(nbPoints, 24.0);
                        y2 = 70 + ((altitudeList.get(d+1)-altitudeMin)*26/scale2);
                        x2 = 243.0 - (d+1) * 242 / max(nbPoints, 24.0);
                        g.line(new Point((int)Math.round(x1), (int)Math.round(y1)),
                                new Point((int)Math.round(x2), (int)Math.round(y2)));
                    }
                }
                // trace the course orientation
                x3 = (int) Math.round(xc - lencomp * Math.sin(course * 3.14159 / 180));
                y3 = (int) Math.round(yc + lencomp * Math.cos(course * 3.14159 / 180));
                x4 = (int) Math.round(xc - lencomp * Math.sin((course + 170) * 3.14159 / 180));
                y4 = (int) Math.round(yc + lencomp * Math.cos((course + 170) * 3.14159 / 180));
                x5 = (int) Math.round(xc - lencomp * Math.sin((course + 190) * 3.14159 / 180));
                y5 = (int) Math.round(yc + lencomp * Math.cos((course + 190) * 3.14159 / 180));
                g.color((byte) 12);
                g.line(new Point(x3, y3), new Point(x4, y4));
                g.line(new Point(x3, y3), new Point(x5, y5));
                g.line(new Point(x4, y4), new Point(x5, y5));
                g.color((byte) 15);

                // give all indications in the glasses
                g.cfgSet("cfgGPSspeed");
                g.txt(new Point(300, 215), Rotation.TOP_LR, (byte) 4, (byte) 0x0F,
                        special_char(getString(R.string.lat_short)) + ": " + String.format("%3.4f", latitude) + "@ ");
                g.txt(new Point(300, 185), Rotation.TOP_LR, (byte) 4, (byte) 0x0F,
                        special_char(getString(R.string.long_short)) + ": " + String.format("%3.4f", longitude) + "@ ");
                g.txt(new Point(300, 155), Rotation.TOP_LR, (byte) 4, (byte) 0x0F,
                        special_char(getString(R.string.crs_short)) + ": " + String.format("%.1f", course) + "@ ");
                g.txt(new Point(240, 120), Rotation.TOP_LR, (byte) 4, (byte) 0x0F,
                        special_char(getString(R.string.alt_short))+": " + String.format("%.0f", altitude)
                                + special_char(getString(R.string.meter)) + " ");
                g.txt(new Point(304, 66), Rotation.TOP_LR, (byte) 5, (byte) 0x0F,
                        special_char(getString(R.string.spd_short))+": "
                                + String.format("%.2f", unitRatio * speed) + special_char(unitShort) + "     ");
                g.txt(new Point(266, 25), Rotation.TOP_LR, (byte) 4, (byte) 0x0F,
                        special_char(getString(R.string.fastest))+": " + String.format("%.2f", unitRatio * maxSpeed)
                                + special_char(unitShort) + "     ");
                g.color((byte) 1);
                g.rect(new Point(0, 225-mapSize), new Point(mapSize, 225));
                g.color((byte) 15);

                g.holdFlush(holdFlushAction.FLUSH);

                }
            } else {
                gps.showSettingsAlert();
            // Can't get location.  GPS or network is not enabled.
                if (g != null) { g.clear(); displayClock();
                    g.txt(new Point(250, 200), Rotation.TOP_LR, (byte) 2, (byte) 0x0F, "GPS data");
                    g.txt(new Point(250, 150), Rotation.TOP_LR, (byte) 2, (byte) 0x0F, "unknown");
                }
            }
    }


    public Bitmap textAsBitmap(String text, int textSize) {
        TextPaint tp = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        tp.setTextSize(textSize);
        tp.setColor(Color.WHITE); // white for text
        tp.setTextAlign(Paint.Align.LEFT);
        float baseline = -tp.ascent(); // ascent() is negative
        int width = max((int) (tp.measureText(text) + 0.5f),1); // round with 1 as min
        int height = max((int) (baseline + tp.descent() + 0.5f),1);
        Paint bp = new Paint(Paint.ANTI_ALIAS_FLAG);
        bp.setStyle(Paint.Style.FILL);
        bp.setColor(Color.BLACK); // black for background
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(image);
        c.drawPaint(bp);
        c.drawText(text, 0, baseline, tp);
        return image;
    }


    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth(), height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width, scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);
        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    public Bitmap getDefaultBitmap(Drawable d) {
        if (d instanceof BitmapDrawable) {
            return ((BitmapDrawable) d).getBitmap();
        } else if ((Build.VERSION.SDK_INT >= 26)
                && (d instanceof AdaptiveIconDrawable)) {
            AdaptiveIconDrawable icon = ((AdaptiveIconDrawable)d);
            int w = icon.getIntrinsicWidth();
            int h = icon.getIntrinsicHeight();
            Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            canvas.drawColor(0);
            canvas.drawBitmap(result, 0F, 0F, null);
            icon.setBounds(0, 0, w, h);
            icon.draw(canvas);
            return result;
        }
        float density = this.getResources().getDisplayMetrics().density;
        int defaultWidth = (int)(48* density);
        int defaultHeight = (int)(48* density);
        return Bitmap.createBitmap(defaultWidth, defaultHeight, Bitmap.Config.ARGB_8888);
    }


    // ---------------------------------------------------------------------

    /////////  LUMINANCE  bar and switch
    private void lumaButton(int luma){
        final Glasses glasses = this.connectedGlasses;
        if( glasses!=null) {glasses.luma((byte) luma);}
    }

    private void sensorSwitch(boolean on){
        final Glasses g = this.connectedGlasses;
        if( g!=null) {g.sensor(on);}
    }

    // display the batteries level and the clock in the glasses
    @SuppressLint("DefaultLocale")
    private void displayClock(){
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY); // phone battery level
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String clock = sdf.format(new Date()); // clock in text format
        int top=255-topmrg;
        final Glasses g = connectedGlasses;
        if (g != null && !notification) {
            messageHandler.removeCallbacks(messageRunnable);
            g.battery(r1 -> { gbattery=r1;
                connectedGlasses.cfgSet("ALooK");
                if (r1 < 25) {connectedGlasses.imgDisplay((byte) 1, (short) (272-lftmrg), (short) (top-26));}
                else {connectedGlasses.imgDisplay((byte) 0, (short) (272-lftmrg), (short) (top-26));}
                connectedGlasses.txt(new Point((263-lftmrg), top), Rotation.TOP_LR, (byte) 1, (byte) 0x0F,
                        String.format("%d", r1) + "% / " + String.format("%d", batLevel) + "%  ");
                connectedGlasses.txt(new Point(100+rgtmrg, top), Rotation.TOP_LR, (byte) 1, (byte) 0x0F, clock);
            });//Glasses Battery
        }
    }

    @SuppressLint("SetTextI18n")
    private void setUIGlassesInformations(){
        final Glasses glasses = this.connectedGlasses;
        glassesidTextView.setText(glasses.getName());
        DeviceInformation di = glasses.getDeviceInformation();
        fwTextView.setText(di.getFirmwareVersion());

        glasses.settings(r -> sensorSwitch.setChecked(r.isGestureEnable()));
        glasses.settings(r -> luminanceSeekBar.setProgress(r.getLuma()));
    }

    //---------------------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == requestCode && requestCode == Activity.RESULT_FIRST_USER) {
            if (data != null && data.hasExtra("connectedGlasses")) {

                this.connectedGlasses= data.getExtras().getParcelable("connectedGlasses");
                this.connectedGlasses.setOnDisconnected(glasses -> MainActivity.this.disconnect());
                runOnUiThread(MainActivity.this::setUIGlassesInformations);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        if (this.connectedGlasses != null) {
            savedInstanceState.putParcelable("connectedGlasses", this.connectedGlasses);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If BT is not on, request that it be enabled.
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {Toast.makeText(getApplicationContext(),
                "Your BlueTooth is not open !!!", Toast.LENGTH_LONG).show();}
        if (!((DemoApp) this.getApplication()).isConnected()) {this.connectedGlasses = null;}
        this.updateVisibility();
        clockRunnable = new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {miseAJourDeLaPosition();}
                clockHandler.postDelayed(this, 10000);
            }
        };
        clockHandler.removeCallbacks(clockRunnable);
        clockHandler.postDelayed(clockRunnable, 10000); // on redemande toutes les 10000ms
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart() {
        super.onStart();
        latitudeList.clear();
        longitudeList.clear();
        altitudeList.clear();
        // If BT is not on, request that it be enabled.
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Your BlueTooth is not open !!!", Toast.LENGTH_LONG).show();
            largeText.setText("Your BlueTooth is not open !!\n\n" +
                    "Please open BlueTooth and\n\n relaunch the application.");
            largeText.setTextColor(Color.parseColor("#FF0000"));
            largeText.setTypeface(largeText.getTypeface(), Typeface.BOLD);
        }
        if (!((DemoApp) this.getApplication()).isConnected()) {this.connectedGlasses = null;}
        this.updateVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {miseAJourDeLaPosition();}
    }

    protected void onPause() {
        super.onPause();
        if(clockHandler != null) {clockHandler.removeCallbacks(clockRunnable);} // On arrete le callback
        if(connectedGlasses !=null) {connectedGlasses.unsubscribeToSensorInterfaceNotifications();}
    }

    protected void onStop() {
        super.onStop();
        latitudeList.clear();
        longitudeList.clear();
        altitudeList.clear();
        if(connectedGlasses!=null){connectedGlasses.cfgDelete("cfgGPSspeed");}
        if(clockHandler != null){clockHandler.removeCallbacks(clockRunnable);} // On arrete le callback
        if(connectedGlasses !=null) {connectedGlasses.unsubscribeToSensorInterfaceNotifications();}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        final Glasses g = this.connectedGlasses;

        //noinspection SimplifiableIfStatement
        if (id == R.id.about_app) {Toast.makeText(this.getApplicationContext(),
                getString(R.string.app_name) + "\nVersion " + getString(R.string.app_version),
                Toast.LENGTH_LONG).show();
            return true;}
        if (id == R.id.about_glasses) {
            if( g!=null) {Toast.makeText(this.getApplicationContext(),
                getString(R.string.glasses_id) + " : " + g.getName() + "\n"
                    + getString(R.string.firmware) + " : " + g.getDeviceInformation().getFirmwareVersion(),
                Toast.LENGTH_LONG).show();}
            else {Toast.makeText(this.getApplicationContext(),
                    "No connected glasses found yet!",
                    Toast.LENGTH_LONG).show();}
            return true;}
        return super.onOptionsItemSelected(item);
    }


    private void disconnect() {
        latitudeList.clear();
        longitudeList.clear();
        altitudeList.clear();
        runOnUiThread(() -> {
            if(connectedGlasses !=null) {connectedGlasses.unsubscribeToSensorInterfaceNotifications();}
            ((DemoApp) this.getApplication()).onDisconnected();
            MainActivity.this.connectedGlasses = null;
            MainActivity.this.updateVisibility();
            MainActivity.this.snack();
        });
    }

    private void snack() {this.snack(null, "Disconnected");}

    private Snackbar snack(View snackView, Object data) {
        snackView = this.findViewById(R.id.toolbar);
        final String msg = data == null ? "" : data.toString();
        Snackbar snack = Snackbar.make(snackView, msg, Snackbar.LENGTH_LONG);
        snack.show();
        if (data != null) { Log.d("MainActivity", data.toString()); }
        else { snack.dismiss(); }
        return snack;
    }

    public double getMaxValue(List<Double> values){
        double maxValue = Double.MIN_VALUE;
        for(Double d : values){if(d > maxValue){maxValue = d;}}
        return maxValue;
    }

    public double getMinValue(List<Double> values){
        double minValue = Double.MAX_VALUE;
        for(Double d : values){if(d < minValue){minValue = d;}}
        return minValue;
    }

    public static String special_char(String text) {
        text = text.replaceAll("ó",String.valueOf((char) 2));
        text = text.replaceAll("æ",String.valueOf((char) 3));
        text = text.replaceAll("ä",String.valueOf((char) 4));
        text = text.replaceAll("ø",String.valueOf((char) 5));
        text = text.replaceAll("ö",String.valueOf((char) 6));
        text = text.replaceAll("公",String.valueOf((char) 7));
        text = text.replaceAll("里",String.valueOf((char) 8));
        text = text.replaceAll("小",String.valueOf((char) 9));
        text = text.replaceAll("时",String.valueOf((char) 11));
        text = text.replaceAll("米",String.valueOf((char) 12));
        text = text.replaceAll("秒",String.valueOf((char) 13));
        text = text.replaceAll("英",String.valueOf((char) 14));
        text = text.replaceAll("结",String.valueOf((char) 15));
        text = text.replaceAll("纬",String.valueOf((char) 16));
        text = text.replaceAll("高",String.valueOf((char) 17));
        text = text.replaceAll("课",String.valueOf((char) 18));
        text = text.replaceAll("程",String.valueOf((char) 19));
        text = text.replaceAll("速",String.valueOf((char) 20));
        text = text.replaceAll("度",String.valueOf((char) 21));
        text = text.replaceAll("经",String.valueOf((char) 22));
        text = text.replaceAll("最",String.valueOf((char) 23));
        text = text.replaceAll("低",String.valueOf((char) 24));
        text = text.replaceAll("大",String.valueOf((char) 25));
        text = text.replaceAll("快",String.valueOf((char) 26));
        text = text.replaceAll("方",String.valueOf((char) 27));
        text = text.replaceAll("向",String.valueOf((char) 28));
        text = text.replaceAll("°",String.valueOf((char) 64));

        return text;
    }

}