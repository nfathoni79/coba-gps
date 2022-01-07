package id.nfathoni.cobagps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Locale;

import id.nfathoni.cobagps.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback, LocationListener {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_LOCATION = 0;

    private ActivityMainBinding binding;
    private LocationManager mLocationManager;
    private boolean mIsTracking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setInfo();

        binding.btTrack.setOnClickListener(view -> {
            toggleButton(!mIsTracking);

            if (mIsTracking) {
                getLocation();
            } else {
                mLocationManager.removeUpdates(this);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
                return;
            }
        }

        toggleButton(false);
        setInfo(R.string.info_grant_failed);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d(TAG, "Location changed: " + location.getLatitude() + ", " + location.getLongitude());
        binding.tvLat.setText(String.format(Locale.getDefault(), "%.8f", location.getLatitude()));
        binding.tvLon.setText(String.format(Locale.getDefault(), "%.8f", location.getLongitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "Status changed: " + provider + " " + status);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Log.d(TAG, "Provider enabled: " + provider);
        setInfo(R.string.info_tracking);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Log.d(TAG, "Provider disabled: " + provider);
        setInfo(R.string.info_gps_off);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        } else {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            boolean isGpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (isGpsEnabled) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, this);
            } else {
                toggleButton(false);
                setInfo(R.string.info_gps_off);
            }
        }
    }

    private void setInfo() {
        binding.tvInfo.setText("");
    }

    private void setInfo(int resId) {
        binding.tvInfo.setText(resId);
    }

    private void toggleButton(boolean isTracking) {
        mIsTracking = isTracking;

        if (mIsTracking) {
            binding.btTrack.setText(R.string.btn_stop);
            setInfo(R.string.info_tracking);
        } else {
            binding.btTrack.setText(R.string.btn_start);
            setInfo(R.string.info_stopped);
        }
    }
}