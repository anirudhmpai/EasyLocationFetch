package com.anirudh.locationfetch;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.ContentValues.TAG;

/***
 * created on 12/02/2020
 */
public class EasyLocationFetch extends AppCompatActivity {
    private LocationManager locationManager;
    private Context context;
    private SettingsClient mSettingsClient;
    private static final int REQUEST_LOCATION = 1;
    private LocationSettingsRequest mLocationSettingsRequest;
    private static final int REQUEST_CHECK_SETTINGS = 214;
    private static final int REQUEST_ENABLE_GPS = 516;
    private String address;
    private String city;
    double lattitude = 0;
    double longitude = 0;
    private Activity activity;
    private boolean googleApiKeyAvailable = false;

    public EasyLocationFetch(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        activity = (Activity) context;
    }

    public EasyLocationFetch(Context context, String googleApiKey) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        activity = (Activity) context;
        try {
            // Initialize the SDK
            if (googleApiKey != null && !googleApiKey.equals("")) {
                Places.initialize(getApplicationContext(), googleApiKey);
                googleApiKeyAvailable = true;
            }
        } catch (Exception er) {
            er.printStackTrace();
            googleApiKeyAvailable = false;
        }
    }

    public GeoLocationModel getLocationData() {

        //Initialize Location manager
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        GeoLocationModel data = new GeoLocationModel();
        if (locationManager != null) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locPermission();
            } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {


                if (ActivityCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                        (context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    locPermission();
                    ActivityCompat.requestPermissions(activity, new String[]{ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

                } else {

                    //  Looking for location from Network Provides or GPS or Passive Provider

                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Location location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);


                    if (location != null) {

                        //Location data available from Network provider
                        lattitude = location.getLatitude();
                        longitude = location.getLongitude();

                    } else if (location1 != null) {

                        //Location data available from GPS
                        lattitude = location1.getLatitude();
                        longitude = location1.getLongitude();

                    } else if (location2 != null) {

                        //Location data available from Passive provider
                        lattitude = location2.getLatitude();
                        longitude = location2.getLongitude();

                    } else {
                        locPermission();
                    }

                    if (lattitude != 0 && longitude != 0) {
                        addressFetch(lattitude, longitude);
                        data.setLattitude(lattitude);
                        data.setLongitude(longitude);
                        if (!address.equals("")) {
                            data.setAddress(address);
                        } else {
                            data.setAddress("");
                        }
                    } else {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                2000,
                                10, locationListenerGPS);
                        if (lattitude != 0 && longitude != 0) {
                            addressFetch(lattitude, longitude);
                            data.setLattitude(lattitude);
                            data.setLongitude(longitude);
                            if (!address.equals("")) {
                                data.setAddress(address);
                                data.setCity(city);
                            } else {
                                data.setAddress("");
                                data.setCity("");
                            }
                        } else {
                            data.setLongitude(0);
                            data.setLongitude(0);
                            data.setAddress("");
                            data.setCity("");
                            if (googleApiKeyAvailable) {
                                googleLocationFinder(data);
                            }
                        }
                    }
                }
            }
        } else {
            locPermission();
        }
        return data;
    }

    private GeoLocationModel googleLocationFinder(GeoLocationModel data) {
        try {
            /***
             * google location finder
             */
            // Create a new PlacesClient instance
            PlacesClient placesClient = Places.createClient(context);
            // Use fields to define the data types to return.
            List<Place.Field> placeFields = Collections.singletonList(Place.Field.LAT_LNG);

            // Use the builder to create a FindCurrentPlaceRequest.
            FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

            // Call findCurrentPlace and handle the response (first check that the user has granted permission).
            if (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
                placeResponse.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FindCurrentPlaceResponse response = task.getResult();
                        for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                            LatLng latLng = placeLikelihood.getPlace().getLatLng();
                            if (latLng != null) {
                                lattitude = latLng.latitude;
                                longitude = latLng.longitude;
                            }

                            if (lattitude != 0 && longitude != 0) {
                                addressFetch(lattitude, longitude);
                                data.setLattitude(lattitude);
                                data.setLongitude(longitude);
                                if (!address.equals("")) {
                                    data.setAddress(address);
                                    data.setCity(city);
                                } else {
                                    data.setAddress("");
                                    data.setCity("");
                                }
                            } else {
                                data.setLongitude(0);
                                data.setLongitude(0);
                                data.setAddress("");
                                data.setCity("");
                            }
                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                        }
                    }
                });
            } else {
                // A local method to request required permissions;
                // See https://developer.android.com/training/permissions/requesting
                locPermission();
            }
        } catch (Exception ert) {
            ert.printStackTrace();
            data.setLongitude(0);
            data.setLongitude(0);
            data.setAddress("");
            data.setCity("");
        }
        return data;
    }

    public void addressFetch(double lattitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(lattitude, longitude, 1);
            if (addresses.size() > 0) {
                address = addresses.get(0).getAddressLine(0);
                city = addresses.get(0).getLocality();
                          }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void locPermission() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY));
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();

        mSettingsClient = LocationServices.getSettingsClient(context);

        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(locationSettingsResponse -> {
                    try {
                        getLocationData();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .addOnFailureListener(e -> {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException rae = (ResolvableApiException) e;

                                rae.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
//                                    startIntentSenderForResult(rae.getResolution().getIntentSender(), REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null);
                            } catch (Exception sie) {
                                Log.e("GPS", "Unable to execute request.");
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            Log.e("GPS", "Location settings are inadequate, and cannot be fixed here. Fix in Settings.");
                    }
                })
                .addOnCanceledListener(() -> Log.e("GPS", "checkLocationSettings -> onCanceled"));
    }

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            lattitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

}

