package com.example.googlesearch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady! map is ready");
        mMap = googleMap;
        geoLocation();

        if (mLocationPermissionGranted) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
    }

    private static  final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static  final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private  static  final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    String geolocation;


    private  Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mfusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getLocationPermission();
    }

    public void getDeviceLocation(){
        mfusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted){
                Task location = mfusedLocationClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            Location currentlocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentlocation.getLatitude(), currentlocation.getLongitude()),
                                    DEFAULT_ZOOM,
                                    "My location");
                        }
                        else {
                            Toast.makeText(MapsActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        }catch (SecurityException e){
            Log.d(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));


        if (!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions().
                    position(latLng).
                    title(title);
            mMap.addMarker(options);
        }

    }
     private  void initMap(){
        Log.d(TAG, "initMap! Initializing map");
         SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

         mapFragment.getMapAsync(MapsActivity.this);
     }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission! getting location permissions");
        String [] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted = true;
                initMap();

            }else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private  void geoLocation(){
        Log.d(TAG, "geoLocate: geolocating");

        String geolocation =  getIntent().getExtras().getString("LocCoords");

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(geolocation, 1);
        }catch (IOException e){
            Log.e(TAG, "geolocate: IOException: " + e.getMessage());
        }

        if (list.size() > 0 ){
            Address address = list.get(0);

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       Log.d(TAG, "onRequestPermissionResult! called");
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionResult! permission failed");
                            return;
                        }
                    }

                    Log.d(TAG, "onRequestPermissionResult! permission failed");
                    mLocationPermissionGranted = true;
                    initMap();
                }
            }
        }
    }

}