package net.zsi.gpstest;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;


public class GPSService extends Service {
    private static final String TAG = "GPSService";
    LocationRequest locationRequest;
    LocationCallback locationCallBack;
    FusedLocationProviderClient fusedLocationProviderClient;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: begin");
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * 3);
        locationRequest.setFastestInterval(1000 * 5);
        locationRequest.setPriority(locationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallBack = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onLocationChanged(locationResult.getLastLocation());
            }
        };

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(GPSService.this);

        PackageManager pm = getPackageManager();
        if (pm.checkPermission(   Manifest.permission.ACCESS_FINE_LOCATION, getPackageName()) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        onLocationChanged(location);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "addOnSuccessListener.onFailure: ");
                    Log.d(TAG, "Error trying to get last GPS location");
                    e.printStackTrace();
                }
            });

            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallBack,null);
      }else{
           Log.d(TAG, "onCreate: No permission");

         }

    }// end onCreate method
    private void onLocationChanged(Location location) {
        Intent i = new Intent("location-update");

        i.putExtra("lat", location.getLatitude());
        i.putExtra("lng", location.getLongitude());
        i.putExtra("accuracy", location.getAccuracy());

        if(location.hasAltitude()){
            i.putExtra("altitude", location.getAltitude());
        }else{
            i.putExtra("altitude","Not available");
        }

        if(location.hasSpeed()){
            i.putExtra("speed", location.getSpeed());
        }else{
            i.putExtra("speed", "Not available");
        }

        Geocoder geocoder = new Geocoder(GPSService.this);
        try{
            List<Address> addresses =  geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            i.putExtra("address", addresses.get(0).getAddressLine(0));

        }
        catch (Exception e){
            i.putExtra("address","Unable to get street address");
        }
        GPSService.this.sendBroadcast(i);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(this.locationCallBack);
        }

    }
    private boolean isGranted(int permission) {
        return permission == PackageManager.PERMISSION_GRANTED;
    }
    private boolean checkPermission() {
        return isGranted(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION))
                 && isGranted(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION))
                ;

    }


}
