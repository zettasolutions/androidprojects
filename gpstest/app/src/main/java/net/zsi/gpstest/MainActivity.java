package net.zsi.gpstest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int TOAST_DURATION = 0;
    private TextView tv_lat, tv_lon, tv_altitude,tv_accuracy, tv_speed,tv_distance,tv_address;
    private Button btn_start,btn_stop,btn_distance;

    private BroadcastReceiver broadcastReceiver;
    private Location lastLocation;
    private float distance=0;
    private Context cxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_distance = findViewById(R.id.tv_distance);
        tv_address = findViewById(R.id.tv_address);

        btn_start = findViewById(R.id.btn_start);
        btn_stop = findViewById(R.id.btn_stop);
        btn_distance = findViewById(R.id.btn_distance);

        if (!runtime_permissions()) {
            setButtonClickEvents();
        }
        cxt=getApplicationContext();

    }// end onCreate method
    public void onResume() {
        super.onResume();
        if (this.broadcastReceiver == null) {
            this.broadcastReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    updateUIValues(intent);
               }
            };
        }
        System.out.println("registerReceiver");
        registerReceiver(this.broadcastReceiver, new IntentFilter("location-update"));
    }


    private void setButtonClickEvents() {
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.startService(new Intent(MainActivity.this.getApplicationContext(), GPSService.class));
                Toast.makeText(cxt, "Service is started.", TOAST_DURATION).show();

            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_lat.setText( "0.00");
                tv_lon.setText("0.00");
                tv_accuracy.setText("0.00" );
                tv_distance.setText("0.00" );
                tv_altitude.setText("0.00" );
                tv_speed.setText("0.00" );
                tv_address.setText("");
                MainActivity.this.stopService(new Intent(MainActivity.this.getApplicationContext(), GPSService.class));

                 Toast.makeText(cxt, "Service is stop.", TOAST_DURATION).show();

            }
        });

        btn_distance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                distance=0;
                Toast.makeText(cxt, "Distance is now zero(0).", TOAST_DURATION).show();
            }
        });



    }
    private void updateUIValues(Intent intent) {
        float _tmpDistance=0;
        String  lat = intent.getExtras().get("lat").toString();
        String  lng = intent.getExtras().get("lng").toString();

        Location newLocation  = new Location("NL");
        newLocation.setLatitude(  Float.parseFloat(lat) );
        newLocation.setLongitude(Float.parseFloat(lng) );

        if(lastLocation != null){
            _tmpDistance = lastLocation.distanceTo(newLocation);
            if(_tmpDistance > 1 ) distance += _tmpDistance;
        }

        String  accuracy = intent.getExtras().get("accuracy").toString();
        String  altitude = intent.getExtras().get("altitude").toString();
        String  speed = intent.getExtras().get("speed").toString();
        String  address = intent.getExtras().get("address").toString();
        tv_lat.setText( lat);
        tv_lon.setText(lng);
        tv_accuracy.setText(accuracy );
        tv_distance.setText(distance  + "");
        tv_altitude.setText(altitude );
        tv_speed.setText(speed );
        tv_address.setText(address);

        //set last location
        lastLocation  = new Location("LL");
        lastLocation.setLatitude( Float.parseFloat(lat) );
        lastLocation.setLongitude(Float.parseFloat(lng) );

    }


    private boolean runtime_permissions() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
             requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return true;
        }
        return false;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != 100) {
            return;
        }
        if (grantResults[0] == 0 && grantResults[1] == 0) {
            setButtonClickEvents();
        } else {
            runtime_permissions();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(this.broadcastReceiver);
    }
}