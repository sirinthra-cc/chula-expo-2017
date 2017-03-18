package cuexpo.cuexpo2017.fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.indoorlocalization.Localization;
import com.example.indoorlocalization.OnTaskCompleteListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.inthecheesefactory.thecheeselibrary.manager.Contextor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bolts.Capture;
import cuexpo.cuexpo2017.MainApplication;
import cuexpo.cuexpo2017.R;
import cuexpo.cuexpo2017.dao.ActivityItemCollectionDao;
import cuexpo.cuexpo2017.dao.ActivityItemDao;
import cuexpo.cuexpo2017.dao.ActivityItemLocationDao;
import cuexpo.cuexpo2017.dao.ActivityItemResultDao;
import cuexpo.cuexpo2017.dao.FacilityDao;
import cuexpo.cuexpo2017.dao.FacilityResult;
import cuexpo.cuexpo2017.manager.HttpManager;
import cuexpo.cuexpo2017.utility.FacultyMapEntity;
import cuexpo.cuexpo2017.utility.IMapEntity;
import cuexpo.cuexpo2017.utility.LocationTask;
import cuexpo.cuexpo2017.utility.NormalPinMapEntity;
import cuexpo.cuexpo2017.utility.PermissionUtils;
import cuexpo.cuexpo2017.utility.PopbusRouteMapEntity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MapFragment extends Fragment implements
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private View rootView;
    protected static GoogleMap googleMap;
    private CardView pinList;
    private RelativeLayout infoCard;
    private boolean isShowingPinList = false;
    private boolean isShowingInfoCard = false;
    private ImageView showFaculty, showInterest, showCanteen, showRegis, showToilet, showInfo,
            showRally, showCarPark, showEmer, showPrayer, showBusStop,
            showBusLine1, showBusLine2, showBusLine3, showBusLine4, closeInfoCard, pinIcon;
    private TextView facility, description;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public static WifiManager wifiManager;

//    private Application mainApp = getActivity().getApplication();
    HashMap<String, PopbusRouteMapEntity> popbusRoutes = new HashMap<>();
    HashMap<Integer, FacultyMapEntity> faculties = new HashMap<>();
    ArrayList<NormalPinMapEntity> eventPins = new ArrayList<>();
    ArrayList<NormalPinMapEntity> canteenPins = new ArrayList<>();
    ArrayList<NormalPinMapEntity> regisPins = new ArrayList<>();
    ArrayList<NormalPinMapEntity> infoPins = new ArrayList<>();
    ArrayList<NormalPinMapEntity> toiletPins = new ArrayList<>();
    ArrayList<NormalPinMapEntity> rallyPins = new ArrayList<>();
    ArrayList<NormalPinMapEntity> carParkPins = new ArrayList<>();
    ArrayList<NormalPinMapEntity> emerPins = new ArrayList<>();
    ArrayList<NormalPinMapEntity> prayerPins = new ArrayList<>();
    ArrayList<NormalPinMapEntity> popBusStationPins = new ArrayList<>();
    NormalPinMapEntity tempEventPin;

    private void initializeFaculties() {
        try {
            JSONArray facultiesJSON = new JSONArray(
                    getContext().getResources().getString(R.string.jsonFacultyMap)
            );
            for (int i = 0; i < facultiesJSON.length(); i++) {
                JSONObject facultyData = facultiesJSON.getJSONObject(i);
                faculties.put(facultyData.getInt("id"), new FacultyMapEntity(facultyData));
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void initializePopbusRoutes() {
        try {
            JSONArray routesJSON = new JSONArray(
                    getContext().getResources().getString(R.string.jsonPopbusRoutes)
            );
            for (int i = 0; i < routesJSON.length(); i++) {
                JSONObject routeData = routesJSON.getJSONObject(i);
                popbusRoutes.put(routeData.getString("name"), new PopbusRouteMapEntity(routeData));
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void initializePopBusStation() {
        try {
            JSONArray cuTourStationJSON = new JSONArray(
                    getContext().getResources().getString(R.string.jsonPopBusStation)
            );
            for (int i = 0; i < cuTourStationJSON.length(); i++) {
                JSONObject cuTourStationData = cuTourStationJSON.getJSONObject(i);
                cuexpo.cuexpo2017.dao.Location location = new cuexpo.cuexpo2017.dao.Location();
                location.setLatitude(cuTourStationData.getDouble("lat"));
                location.setLongitude(cuTourStationData.getDouble("lng"));
                popBusStationPins.add(new NormalPinMapEntity(cuTourStationData.getString("nameTh"), location, "Bus Stop"));
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void initializeFavoriteEvent() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("FavouritePlaces", getContext().MODE_PRIVATE);
        Map<String, ?> events = sharedPreferences.getAll();
        for (Map.Entry<String, ?> event: events.entrySet()) {
            String eventInfo = (String) event.getValue();
            Log.d("eventInfo", eventInfo);
            try {
                String[] info = eventInfo.split(",");
                cuexpo.cuexpo2017.dao.Location location = new cuexpo.cuexpo2017.dao.Location();
                location.setLatitude(Double.parseDouble(info[1]));
                location.setLongitude(Double.parseDouble(info[2]));
                eventPins.add(new NormalPinMapEntity(info[0], location, "Favorite Event"));
            } catch (Exception e) {
                Log.e("cannot parse eventInfo", e.toString());
            }
        }
    }

    private void initializeReservedEvent() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("ReservedPlaces", getContext().MODE_PRIVATE);
        Map<String, ?> events = sharedPreferences.getAll();
        for (Map.Entry<String, ?> event: events.entrySet()) {
            String eventInfo = (String) event.getValue();
            Log.d("eventInfo", eventInfo);
            try {
                String[] info = eventInfo.split(",");
                cuexpo.cuexpo2017.dao.Location location = new cuexpo.cuexpo2017.dao.Location();
                location.setLatitude(Double.parseDouble(info[1]));
                location.setLongitude(Double.parseDouble(info[2]));
                eventPins.add(new NormalPinMapEntity(info[0], location, "Reserved Event"));
            } catch (Exception e) {
                Log.e("cannot parse eventInfo", e.toString());
            }

        }
    }

    private void initializeMapStaticData() {
        initializeFaculties();
        initializePopbusRoutes();
        initializePopBusStation();
//        initializeFavoriteEvent();
//        initializeReservedEvent();
    }

    public void goToMap(int facultyId) {
        setAllFacultiesVisibility(true);
        FacultyMapEntity entity = faculties.get(facultyId);
        if (MapFragment.googleMap != null) {
            MapFragment.googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(entity.getMarker().getPosition(), 18.5f)
                    , 1500, null
            );
        }
        showInfoCard(entity.getMarkerIconDrawableResource(), entity.getType(), entity.getNameTh(), -1, entity.getColor());
    }

    public void goToMap(NormalPinMapEntity entity){
        if(tempEventPin != null) tempEventPin.clearMarker();
        tempEventPin = entity;
        tempEventPin.setMap(googleMap);
        if (MapFragment.googleMap != null) {
            MapFragment.googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(entity.getMarker().getPosition(), 18.5f)
                    , 1500, null
            );
        }
        showInfoCard(entity.getMarkerIconDrawableResource(), entity.getType(), entity.getName(), -1, entity.getColor());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        try {
            rootView = inflater.inflate(R.layout.fragment_map, container, false);
        } catch (InflateException e) {
            /* map is already there, just return view as it is */
        }

        new AsyncTask<Void,Void,Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                getActivity().runOnUiThread(new Thread(new Runnable() {
                    public void run() {
                        // Get View
                        pinList = (CardView) rootView.findViewById(R.id.pin_list);
                        infoCard = (RelativeLayout) rootView.findViewById(R.id.info_card);
                        closeInfoCard = (ImageView) rootView.findViewById(R.id.close_info);

                        showFaculty = (ImageView) rootView.findViewById(R.id.show_faculty_city);
                        showInterest = (ImageView) rootView.findViewById(R.id.show_interest);
                        showCanteen = (ImageView) rootView.findViewById(R.id.show_canteen);
                        showRegis = (ImageView) rootView.findViewById(R.id.show_regis);
                        showInfo = (ImageView) rootView.findViewById(R.id.show_info);
                        showToilet = (ImageView) rootView.findViewById(R.id.show_toilet);
                        showRally = (ImageView) rootView.findViewById(R.id.show_rally);
                        showCarPark = (ImageView) rootView.findViewById(R.id.show_car_park);
                        showEmer = (ImageView) rootView.findViewById(R.id.show_emer);
                        showPrayer = (ImageView) rootView.findViewById(R.id.show_prayer);
                        showBusStop = (ImageView) rootView.findViewById(R.id.show_bus_stop);

                        showBusLine1 = (ImageView) rootView.findViewById(R.id.show_bus_line_1);
                        showBusLine2 = (ImageView) rootView.findViewById(R.id.show_bus_line_2);
                        showBusLine3 = (ImageView) rootView.findViewById(R.id.show_bus_line_3);
                        showBusLine4 = (ImageView) rootView.findViewById(R.id.show_bus_line_4);

                        // Get Card Content
                        pinIcon = (ImageView) rootView.findViewById(R.id.pin_icon);
                        facility = (TextView) rootView.findViewById(R.id.facility);
                        description = (TextView) rootView.findViewById(R.id.description);

                        // Set OnClickListener
                        rootView.findViewById(R.id.show_hide_pin).setOnClickListener(showPinListOnClick);
                        rootView.findViewById(R.id.show_current_location).setOnClickListener(showCurrentLocation);
                        pinList.setOnClickListener(focusOCL);
                        rootView.findViewById(R.id.info_card).setOnClickListener(focusOCL);
                        closeInfoCard.setOnClickListener(closeOCL);

                        rootView.findViewById(R.id.faculty_city).setOnClickListener(showFacultyOCL);
                        rootView.findViewById(R.id.interest).setOnClickListener(showInterestOCL);
                        rootView.findViewById(R.id.canteen).setOnClickListener(showCanteenOCL);
                        rootView.findViewById(R.id.regis).setOnClickListener(showRegisOCL);
                        rootView.findViewById(R.id.info).setOnClickListener(showInfoOCL);
                        rootView.findViewById(R.id.toilet).setOnClickListener(showToiletOCL);
                        rootView.findViewById(R.id.rally).setOnClickListener(showRallyOCL);
                        rootView.findViewById(R.id.car_park).setOnClickListener(showCarParkOCL);
                        rootView.findViewById(R.id.emer).setOnClickListener(showEmerOCL);
                        rootView.findViewById(R.id.prayer).setOnClickListener(showPrayerOCL);
                        rootView.findViewById(R.id.bus_stop).setOnClickListener(showBusStopOCL);

                        showBusLine1.setOnClickListener(showBusLine1OCL);
                        showBusLine2.setOnClickListener(showBusLine2OCL);
                        showBusLine3.setOnClickListener(showBusLine3OCL);
                        showBusLine4.setOnClickListener(showBusLine4OCL);

                        // Set visibility
                        showFaculty.setSelected(true);
                        showBusLine1.setSelected(true);
                        showBusLine2.setSelected(true);
                        showBusLine3.setSelected(true);
                        showBusLine4.setSelected(true);

                        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.main_map);
                        mapFragment.getMapAsync(MapFragment.this);
                        initializeMapStaticData();

                        Call<FacilityDao> facilityCall = HttpManager.getInstance().getService().loadFacilityList();
                        facilityCall.enqueue(callbackFacility);
                        Call<ActivityItemCollectionDao> rallyCall = HttpManager.getInstance().getService().loadActivityByZone("58b1174858d522497ea7394d");
                        rallyCall.enqueue(callbackRally);
                    }
                }));
                return null;
            }
        }.execute();



        return rootView;
    }

    Callback<ActivityItemCollectionDao> callbackRally = new Callback<ActivityItemCollectionDao>() {
        @Override
        public void onResponse(Call<ActivityItemCollectionDao> call, Response<ActivityItemCollectionDao> response) {
            if (response.isSuccessful()) {
                List<ActivityItemResultDao> rallies = response.body().getResults();
                for (ActivityItemResultDao rally: rallies) {
                    String name = rally.getName().getTh();
                    ActivityItemLocationDao activityLocation = rally.getLocation();
                    cuexpo.cuexpo2017.dao.Location location = new cuexpo.cuexpo2017.dao.Location();
                    location.setLatitude(activityLocation.getLatitude());
                    location.setLongitude(activityLocation.getLongitude());
                    rallyPins.add(new NormalPinMapEntity(name, location, "Rally"));
                }
                initPins(rallyPins);
            } else {
                try {
                    Log.e("fetch error", response.errorBody().string());
                    Toast.makeText(Contextor.getInstance().getContext(), response.errorBody().string(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        @Override
        public void onFailure(Call<ActivityItemCollectionDao> call, Throwable t) {
            Toast.makeText(Contextor.getInstance().getContext(), t.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    Callback<FacilityDao> callbackFacility = new Callback<FacilityDao>() {
        @Override
        public void onResponse(Call<FacilityDao> call, Response<FacilityDao> response) {
            if (response.isSuccessful()) {
                List<FacilityResult> facilities = response.body().getResults();
                for (FacilityResult facility: facilities) {
                    String type = facility.getType();
                    String name = facility.getName().getTh();
                    cuexpo.cuexpo2017.dao.Location location = facility.getLocation();
                    if(type.equals("Canteen") || type.equals("Souvenir")) canteenPins.add(new NormalPinMapEntity(name, location, type));
                    else if(type.equals("Registration")) regisPins.add(new NormalPinMapEntity(name, location, type));
                    else if(type.equals("Information")) infoPins.add(new NormalPinMapEntity(name, location, type));
                    else if(type.equals("Toilet")) toiletPins.add(new NormalPinMapEntity(name, location, type));
                    else if(type.equals("Carpark")) carParkPins.add(new NormalPinMapEntity(name, location, "Car Park"));
                    else if(type.equals("Emergency")) emerPins.add(new NormalPinMapEntity(name, location, "First Aid"));
                    else if(type.equals("Prayer")) prayerPins.add(new NormalPinMapEntity(name, location, "Prayer Room"));
                }
                initPins(canteenPins);
                initPins(regisPins);
                initPins(infoPins);
                initPins(toiletPins);
                initPins(carParkPins);
                initPins(emerPins);
                initPins(prayerPins);
                initPins(popBusStationPins);
            } else {
                try {
                    Log.e("fetch error", response.errorBody().string());
                    Toast.makeText(Contextor.getInstance().getContext(), response.errorBody().string(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        @Override
        public void onFailure(Call<FacilityDao> call, Throwable t) {
            Toast.makeText(Contextor.getInstance().getContext(), t.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    public void initPins(List<NormalPinMapEntity> entries){
        for (NormalPinMapEntity entry : entries) {
            entry.setMap(googleMap);
            entry.setVisible(false);
        }
    }
    private View.OnClickListener showPinListOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideInfoCard();
            if (isShowingPinList) {
                isShowingPinList = false;
                ObjectAnimator.ofFloat(pinList, "x", dpToPx(12), dpToPx(-200)).start();
            } else {
                isShowingPinList = true;
                ObjectAnimator.ofFloat(pinList, "x", dpToPx(-200), dpToPx(12)).start();
            }
        }
    };

    private View.OnClickListener showCurrentLocation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LocationTask locationTask = new LocationTask();
            locationTask.sentLocationRequest();
            hidePinList();
            enableMyLocation();
            showInfoCard(-1, "Current Location", MainApplication.getCurrentLocationDetail(), R.color.header_background, -1);
        }
    };

    private void setAllFacultiesVisibility(boolean isVisible) {
        for (IMapEntity faculty : faculties.values()) {
            faculty.setVisible(isVisible);
        }
    }

    private void setAllNormalPinsVisibility(ArrayList<NormalPinMapEntity> entities, boolean visible) {
        for (NormalPinMapEntity entity : entities) {
            entity.setVisible(visible);
        }
    }

    private View.OnClickListener showFacultyOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showFaculty.isSelected()){
                showFaculty.setSelected(false);
                setAllFacultiesVisibility(false);
            } else {
                showFaculty.setSelected(true);
                setAllFacultiesVisibility(true);
            }
        }
    };
    private View.OnClickListener showInterestOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showInterest.isSelected()){
                showInterest.setSelected(false);
            } else {
                showInterest.setSelected(true);
            }
        }
    };
    private View.OnClickListener showCanteenOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showCanteen.isSelected()){
                showCanteen.setSelected(false);
                setAllNormalPinsVisibility(canteenPins, false);
            } else {
                showCanteen.setSelected(true);
                setAllNormalPinsVisibility(canteenPins, true);
            }
        }
    };
    private View.OnClickListener showRegisOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showRegis.isSelected()){
                showRegis.setSelected(false);
                setAllNormalPinsVisibility(regisPins, false);
            } else {
                showRegis.setSelected(true);
                setAllNormalPinsVisibility(regisPins, true);
            }
        }
    };
    private View.OnClickListener showInfoOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showInfo.isSelected()){
                showInfo.setSelected(false);
                setAllNormalPinsVisibility(infoPins, false);
            } else {
                showInfo.setSelected(true);
                setAllNormalPinsVisibility(infoPins, true);
            }
        }
    };
    private View.OnClickListener showToiletOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showToilet.isSelected()){
                showToilet.setSelected(false);
                setAllNormalPinsVisibility(toiletPins, false);
            } else {
                showToilet.setSelected(true);
                setAllNormalPinsVisibility(toiletPins, true);
            }
        }
    };
    private View.OnClickListener showRallyOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showRally.isSelected()){
                showRally.setSelected(false);
                setAllNormalPinsVisibility(rallyPins, false);
            } else {
                showRally.setSelected(true);
                setAllNormalPinsVisibility(rallyPins, true);
            }
        }
    };
    private View.OnClickListener showCarParkOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showCarPark.isSelected()){
                showCarPark.setSelected(false);
                setAllNormalPinsVisibility(carParkPins, false);
            } else {
                showCarPark.setSelected(true);
                setAllNormalPinsVisibility(carParkPins, true);
            }
        }
    };
    private View.OnClickListener showEmerOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showEmer.isSelected()){
                showEmer.setSelected(false);
                setAllNormalPinsVisibility(emerPins, false);
            } else {
                showEmer.setSelected(true);
                setAllNormalPinsVisibility(emerPins, true);
            }
        }
    };
    private View.OnClickListener showPrayerOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showPrayer.isSelected()){
                showPrayer.setSelected(false);
                setAllNormalPinsVisibility(prayerPins, false);
            } else {
                showPrayer.setSelected(true);
                setAllNormalPinsVisibility(prayerPins, true);
            }
        }
    };
    private View.OnClickListener showBusStopOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showBusStop.isSelected()){
                showBusStop.setSelected(false);
                setAllNormalPinsVisibility(popBusStationPins, false);
            } else {
                showBusStop.setSelected(true);
                setAllNormalPinsVisibility(popBusStationPins, true);
            }
        }
    };

    private View.OnClickListener showBusLine1OCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showBusLine1.isSelected()){
                showBusLine1.setSelected(false);
                popbusRoutes.get("1").setVisible(false);
            } else {
                showBusLine1.setSelected(true);
                popbusRoutes.get("1").setVisible(true);
            }
        }
    };
    private View.OnClickListener showBusLine2OCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showBusLine2.isSelected()){
                showBusLine2.setSelected(false);
                popbusRoutes.get("2").setVisible(false);
            } else {
                showBusLine2.setSelected(true);
                popbusRoutes.get("2").setVisible(true);
            }
        }
    };
    private View.OnClickListener showBusLine3OCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showBusLine3.isSelected()){
                showBusLine3.setSelected(false);
                popbusRoutes.get("3").setVisible(false);
            } else {
                showBusLine3.setSelected(true);
                popbusRoutes.get("3").setVisible(true);
            }
        }
    };
    private View.OnClickListener showBusLine4OCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (showBusLine4.isSelected()){
                showBusLine4.setSelected(false);
                popbusRoutes.get("4").setVisible(false);
            } else {
                showBusLine4.setSelected(true);
                popbusRoutes.get("4").setVisible(true);
            }
        }
    };

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission((AppCompatActivity) this.getActivity(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (googleMap != null) {
            try {
                googleMap.setMyLocationEnabled(true);
//                googleMap.getUiSettings().setMyLocationButtonEnabled(true);
//                Location location = googleMap.
//                Location location = MainApplication.getCurrentLocation();
                GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange (Location location) {
                        MainApplication.setCurrentLocation(location);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(location.getLatitude(), location.getLongitude()),
                                17
                        ), 1000, null);
                    }
                };
                googleMap.setOnMyLocationChangeListener(myLocationChangeListener);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            PermissionUtils.PermissionDeniedDialog
                    .newInstance(true).show(this.getChildFragmentManager(), "dialog");
        }
    }

    private void showInfoCard(int icon, String facilityString, String descriptionString, int colorId, int color) {
        hidePinList();

        // Set Content
        if (icon != -1) {
            pinIcon.setImageResource(icon);
            pinIcon.setVisibility(View.VISIBLE);
        } else {
            pinIcon.setVisibility(View.GONE);
        }
        facility.setText(facilityString);
        if (color == -1) facility.setTextColor(getResources().getColor(colorId));
        else facility.setTextColor(color);
        description.setText(descriptionString);

        // Animate
        if (isShowingInfoCard) return;
        isShowingInfoCard = true;
        infoCard.setVisibility(View.VISIBLE);
        closeInfoCard.setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(infoCard, "alpha", 0, 1).setDuration(300).start();
        ObjectAnimator.ofFloat(closeInfoCard, "alpha", 0, 1).setDuration(300).start();
    }

    private View.OnClickListener closeOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hideInfoCard();
        }
    };

    private void hidePinList() {
        if (isShowingPinList) {
            isShowingPinList = false;
            pinList.animate().translationX(dpToPx(0));
        }
    }

    private void hideInfoCard() {
        if (!isShowingInfoCard) return;
        isShowingInfoCard = false;

        ObjectAnimator.ofFloat(closeInfoCard, "alpha", 1, 0).setDuration(300).start();
        ObjectAnimator alpha = ObjectAnimator.ofFloat(infoCard, "alpha", 1, 0);
        alpha.setDuration(300);
        alpha.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                infoCard.setVisibility(View.GONE);
                closeInfoCard.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        alpha.start();
    }

    private View.OnClickListener focusOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getActivity().getApplicationContext());
        this.googleMap = googleMap;

        // Add Faculty
        for (IMapEntity facultyEntry : faculties.values()) {
            facultyEntry.setMap(googleMap);
        }
        // Add Popbus routes
        for (PopbusRouteMapEntity routeEntry : popbusRoutes.values()) {
            routeEntry.setMap(googleMap);
        }
        googleMap.setOnMarkerClickListener(markerOCL);
    }

    GoogleMap.OnMarkerClickListener markerOCL = new GoogleMap.OnMarkerClickListener() {
        public boolean onMarkerClick(Marker marker) {
            if (MapFragment.googleMap != null) {
                MapFragment.googleMap.animateCamera(
                        CameraUpdateFactory.newLatLng(marker.getPosition()),
                        500,
                        null
                );
            }

            for (FacultyMapEntity facultyEntry : faculties.values()) {
                if (facultyEntry.getMarker().equals(marker)) {
                    showInfoCard(facultyEntry.getMarkerIconDrawableResource(),
                            facultyEntry.getType(),
                            facultyEntry.getNameTh(),
                            -1,
                            facultyEntry.getColor());
                    return true;
                }
            }
            setPinOnClick(canteenPins, marker);
            setPinOnClick(regisPins, marker);
            setPinOnClick(infoPins, marker);
            setPinOnClick(toiletPins, marker);
            setPinOnClick(rallyPins, marker);
            setPinOnClick(carParkPins, marker);
            setPinOnClick(emerPins, marker);
            setPinOnClick(prayerPins, marker);
            setPinOnClick(tempEventPin, marker);
            setPinOnClick(popBusStationPins, marker);
            return true;
        }
    };

    public boolean setPinOnClick(List<NormalPinMapEntity> entries, Marker marker) {
        for (NormalPinMapEntity entry : entries) {
            if (entry.getMarker().equals(marker)) {
                showInfoCard(entry.getMarkerIconDrawableResource(),
                        entry.getType(),
                        entry.getName(),
                        -1,
                        entry.getColor());
                return true;
            }
        }
        return true;
    }

    public boolean setPinOnClick(NormalPinMapEntity entry, Marker marker) {
        if (entry == null) return false;
        if (entry.getMarker() == null) return false;
        if (entry.getMarker().equals(marker)) {
            showInfoCard(entry.getMarkerIconDrawableResource(),
                    entry.getType(),
                    entry.getName(),
                    -1,
                    entry.getColor());
            return true;
        }
        return true;
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        return Math.round(dp * ((float)displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public void onResume(){
        Log.d("mapOnResume", "resume");
        super.onResume();
    }

}
