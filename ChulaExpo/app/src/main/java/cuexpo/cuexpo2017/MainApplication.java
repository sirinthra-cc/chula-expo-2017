package cuexpo.cuexpo2017;

import android.app.Application;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.LocationSource;
import com.inthecheesefactory.thecheeselibrary.manager.Contextor;

import java.util.Timer;

import cuexpo.cuexpo2017.utility.LocationTask;
import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public class MainApplication extends Application implements
        LocationSource,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static WifiManager wifiManager;
    public static GoogleApiClient googleApiClient;
    public static Location lastKnownLocation;
    private OnLocationChangedListener mListener;

    public static String getCurrentLocationDetail() {
        return currentLocationDetail;
    }

    public static void setCurrentLocationDetail(String currentLocationDetail) {
        MainApplication.currentLocationDetail = currentLocationDetail;
    }

    private static String currentLocationDetail = "Loading...";
    private static String apiToken = "";

    public static String getApiToken() {
        return apiToken;
    }

    public static void setApiToken(String apiToken) {
        MainApplication.apiToken = apiToken;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize things
        Contextor.getInstance().init(getApplicationContext());

        // facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        // font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/ThaiSansNeue-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        // Wifi
        wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

        // GeoLocation
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        googleApiClient.connect();

        // start background task
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new LocationTask(), 30000, 30000); // 30s

        // Crashlytics
        Fabric.with(this, new Crashlytics());
    }

    @Override
    public void onTerminate() {
        googleApiClient.disconnect();
        super.onTerminate();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        } catch (SecurityException e) {
//            Snackbar.make(MainActivity.this.rootView, "" + e, Snackbar.LENGTH_SHORT);
        }

        if (lastKnownLocation != null) {
            Log.d("geoLocation", String.valueOf(lastKnownLocation.getLatitude()) + " " + String.valueOf(lastKnownLocation.getLongitude()));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public static Location getCurrentLocation() {
        return lastKnownLocation;
    }
    public static void setCurrentLocation(Location location) { lastKnownLocation = location; }

    @Override
    public void onLocationChanged(Location location) {
        lastKnownLocation = location;
        if (mListener != null) {
            mListener.onLocationChanged(location);
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
        mListener = null;
    }

}
