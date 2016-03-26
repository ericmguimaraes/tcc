package com.ericmguimaraes.gaso.maps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.ericmguimaraes.gaso.model.Location;
import com.ericmguimaraes.gaso.util.ConnectionDetector;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by ericm on 3/19/2016.
 */
public class LocationHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static LocationHelper INSTANCE;

    private GoogleApiClient googleApiClient;

    private Context context;

    private android.location.Location lastLocation;

    private boolean isConnected = false;

    private LocationHelper(Context context) {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
        this.context = context;
    }

    public static LocationHelper getINSTANCE(Context context){
        if(INSTANCE==null)
            INSTANCE = new LocationHelper(context);
        return INSTANCE;
    }

    public void disconnect() {
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        isConnected = true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        isConnected = false;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        isConnected = false;
    }

    public Location getLastKnownLocation() {
        if(!isConnected)
            return null;
        Location location = null;
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Log.e("getting location", "NO PERMISSION");
            return null;
        }
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
                googleApiClient);
        if (lastLocation != null) {
            location = new Location();
            location.setLat(lastLocation.getLatitude());
            location.setLng(lastLocation.getLongitude());
        }
        return location;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public static double distance(Location l1, Location l2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(l2.getLat() - l1.getLat());
        Double lonDistance = Math.toRadians(l2.getLng() - l1.getLng());
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(l1.getLat())) * Math.cos(Math.toRadians(l2.getLat()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }
}
