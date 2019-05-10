package com.mauritues.brume.bmtraffic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.mauritues.brume.bmtraffic.model.LocationRecording;
import com.mauritues.brume.bmtraffic.serviceproviders.LocationRecordingServiceProvider;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainMapActivity extends AppCompatActivity implements OnMapReadyCallback, LanguageCallback {

    private MapView bmMapView;
    private GoogleMap bmGoogleMap;
    private boolean isMapReady = false;
    private static int LOCATION_REQUEST = 1023;
    private static int REQUEST_CHECK_SETTINGS = 1024;
    private LocationRecordingServiceProvider provider;

    private MapAutoCompleteTextView sourceLocation;
    private MapAutoCompleteTextView destinationLocation;

    private MapAutoCompleteTextView selectedBox;
    private FusedLocationProviderClient client;

    PlacesTask placesTask;
    ParserTask parserTask;

    private Location currentLocation;

    ArrayList<LatLng> mapPoints;

    @Override
    protected void attachBaseContext(Context newBase) {
        String locale = Utils.getLang(newBase);
        Log.e(this.getClass().getSimpleName(), "Locale is " + locale);
        Context ctx = Utils.changeLocale(newBase, locale);
        super.attachBaseContext(ctx);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);
        try {
            if (getSupportActionBar() != null)
                getSupportActionBar().hide();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        client = LocationServices.getFusedLocationProviderClient(this);
        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        provider = new LocationRecordingServiceProvider(mFirestore);
        findViewById(R.id.menuButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(MainMapActivity.this, v);
                menu.getMenuInflater().inflate(R.menu.drawer_view, menu.getMenu());
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.logOut) {
                            SharedPreferences prefs = getSharedPreferences("traffic", Context.MODE_PRIVATE);
                            SharedPreferences.Editor edit = prefs.edit();
                            edit.putBoolean("login", false);
                            edit.apply();
                            startActivity(new Intent(MainMapActivity.this, MainActivity.class));
                            finish();
                            return true;
                        } else if (item.getItemId() == R.id.chnageLang) {
                            Utils.showLanguageDialog(MainMapActivity.this, MainMapActivity.this);
                            return true;
                        } else if (item.getItemId() == R.id.nav_history) {
                            startActivity(new Intent(MainMapActivity.this, RecordHistory.class));
                            return true;
                        } else
                            return false;
                    }
                });
                menu.show();
            }
        });

        String MAP_VIEW_KEY = getResources().getString(R.string.google_maps_key);

        SessionVariables.MAP_VIEW_KEY = getResources().getString(R.string.google_maps_key);


        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_KEY);
        }

        bmMapView = findViewById(R.id.bmMapView);
        bmMapView.onCreate(mapViewBundle);
        bmMapView.getMapAsync(this);

        sourceLocation = findViewById(R.id.sourceLocation);
        destinationLocation = findViewById(R.id.destinationLocation);

        mapPoints = new ArrayList<>();
        ((TextView) findViewById(R.id.dateView)).setText(Utils.getDate());
        ((TextView) findViewById(R.id.timeView)).setText(Utils.getIme());
        ImageView recordButton = findViewById(R.id.recordButton);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar snackbar = Snackbar
                        .make(view, "Recording Traffic", Snackbar.LENGTH_LONG);
                snackbar.show();

                if (mapPoints.size() == 2) {
                    persistData(mapPoints.get(0), ((EditText) findViewById(R.id.sourceLocation)).getText().toString(), mapPoints.get(1)
                            , ((EditText) findViewById(R.id.destinationLocation)).getText().toString());
                }

            }
        });
        findViewById(R.id.mapType).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMapReady) {
                    if ((bmGoogleMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) || (bmGoogleMap.getMapType() == GoogleMap.MAP_TYPE_TERRAIN)) {
                        bmGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        ((ImageView) findViewById(R.id.mapType)).setColorFilter(Color.GREEN);
                    } else {
                        bmGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        ((ImageView) findViewById(R.id.mapType)).setColorFilter(Color.GRAY);
                    }
                }
            }
        });

        findViewById(R.id.shareLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.app_name_long));
                    intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
                    startActivity(intent);
            }
        });




//        mDrawerLayout = findViewById(R.id.drawer_layout);
//
//        NavigationView navigationView = findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(
//                new NavigationView.OnNavigationItemSelectedListener() {
//                    @Override
//                    public boolean onNavigationItemSelected(MenuItem menuItem) {
//                        // set item as selected to persist highlight
//                        menuItem.setChecked(true);
//                        // close drawer when item is tapped
//                        mDrawerLayout.closeDrawers();
//
//                        // Add code here to update the UI based on the item selected
//                        // For example, swap UI fragments here
//                        if(menuItem.getItemId() == R.id.nav_history){
//                            Intent intent = new Intent(getApplicationContext(), RecordHistory.class);
//                            startActivity(intent);
//                        }
//
//                        return true;
//                    }
//                });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        isMapReady = true;
        bmGoogleMap = googleMap;
        bmGoogleMap.setMinZoomPreference(9);
        final LatLng ne = new LatLng(-19.861232, 57.934147);
        final LatLng sw = new LatLng(-20.930999, 57.107669);
        bmGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                bmGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(sw, ne), 30));/*
                LatLng focus = new LatLng(-20.1608072, 57.4662028);*/
                // TODO: 10/03/2019 Replace this with user current location
                bmGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLngBounds(sw, ne).getCenter(), 10f));
                checkLocationSetting();
                plotAllTrafficGraph(bmGoogleMap);
            }
        });
        bmGoogleMap.setTrafficEnabled(true);

        //sourceLocation.setThreshold(1);
        sourceLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                selectedBox = findViewById(R.id.sourceLocation);

                if (selectedBox.isPerformingCompletion()) {
                    // An item has been selected from the list. Ignore.
                    selectedBox.dismissDropDown();
                    LatLng selectedLocation = SessionVariables.Locations.get(selectedBox.getText().toString());
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(selectedLocation).title(selectedBox.getText().toString())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                            .draggable(false).visible(true);

                    if (mapPoints.size() > 2) {
                        mapPoints = new ArrayList<>();
                    }

                    mapPoints.add(selectedLocation);

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (LatLng position : mapPoints) {
                        builder.include(position);
                    }

                    LatLngBounds bounds = builder.build();
                    int padding = 0;
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                    bmGoogleMap.addMarker(markerOptions);

                    bmGoogleMap.moveCamera(cu);


                    //Execute Directions API request

                    if (mapPoints.size() == 2) {
                        PolylineOptions polylineOptions = plotDirection(mapPoints.get(0), mapPoints.get(1));
                        bmGoogleMap.addPolyline(polylineOptions);

                    }


                } else {
                    placesTask = new PlacesTask();
                    placesTask.execute(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        destinationLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                selectedBox = findViewById(R.id.destinationLocation);
                if (selectedBox.isPerformingCompletion()) {
                    // An item has been selected from the list. Ignore.
                    // An item has been selected from the list. Ignore.
                    selectedBox.dismissDropDown();
                    LatLng selectedLocation = SessionVariables.Locations.get(selectedBox.getText().toString());
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(selectedLocation).title(selectedBox.getText().toString())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .draggable(false).visible(true);

                    if (mapPoints.size() > 2) {
                        mapPoints = new ArrayList<>();
                    }

                    mapPoints.add(selectedLocation);

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (LatLng position : mapPoints) {
                        builder.include(position);
                    }

                    LatLngBounds bounds = builder.build();
                    int padding = 0;
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                    bmGoogleMap.addMarker(markerOptions);

                    bmGoogleMap.moveCamera(cu);


                    if (mapPoints.size() == 2) {
                        PolylineOptions polylineOptions = plotDirection(mapPoints.get(0), mapPoints.get(1));
                        if (polylineOptions != null) {
                            bmGoogleMap.addPolyline(polylineOptions);
                        }

                    }

                } else {
                    placesTask = new PlacesTask();
                    placesTask.execute(charSequence.toString());
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        bmMapView.onResume();

    }


    public PolylineOptions plotDirection(LatLng sourceLocation, LatLng destinationLocation) {
        System.out.println("Yes i was called to draw the line");
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(getResources().getString(R.string.google_maps_key))
                .build();

        String sourceLatitude = String.valueOf(sourceLocation.latitude);
        String sourceLongitude = String.valueOf(sourceLocation.longitude);
        String sourceStringLocation = sourceLatitude + "," + sourceLongitude;

        String destinationLatitude = String.valueOf(destinationLocation.latitude);
        String desitinationLongitude = String.valueOf(destinationLocation.longitude);
        String destinationStringLocation = destinationLatitude + "," + desitinationLongitude;

        ArrayList<LatLng> path = new ArrayList<>();

        DirectionsApiRequest req = DirectionsApi.getDirections(context, sourceStringLocation, destinationStringLocation);
        try {
            DirectionsResult res = req.await();

            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.length > 0) {
                System.out.println("Yes we are in line" + Arrays.toString(res.routes));
                DirectionsRoute route = res.routes[0];

                if (route.legs != null) {
                    for (int i = 0; i < route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j = 0; j < leg.steps.length; j++) {
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length > 0) {
                                    for (int k = 0; k < step.steps.length; k++) {
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null) {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {
                                        //Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coords = points.decodePath();
                                        for (com.google.maps.model.LatLng coord : coords) {
                                            path.add(new LatLng(coord.lat, coord.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("FragmentActivity", ex.getLocalizedMessage());
        }

        //Draw the polyline
        if (path.size() > 0) {
            return new PolylineOptions().addAll(path).color(Color.RED).width(8);
//            mMap.addPolyline(opts);
        }

        return null;
    }

    @Override
    public void onLanguageChanged() {
        this.recreate();
    }

    //sub classes
    // Fetches all places from GooglePlaces AutoComplete Web Service
    @SuppressLint("StaticFieldLeak")
    private class PlacesTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... place) {
            // For storing data from web service
            String data = "";

            // Obtain browser key from https://code.google.com/apis/console
            String key = "key=" + getResources().getString(R.string.google_maps_key);

            String input = "";

            try {
                input = "input=" + URLEncoder.encode(place[0], "utf-8");
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }

            // place type to be searched
            String types = "types=geocode";

            // Sensor enabled
            String sensor = "sensor=false";

            // Building the parameters to the web service
            String parameters = input + "&" + types + "&" + sensor + "&" + key + "&components=country:mu";

            // Output format
            String output = "json";

            // Building the url to the web service
            String url = "https://maps.googleapis.com/maps/api/place/autocomplete/" + output + "?" + parameters;

            try {
                // Fetching the data from we service
                Log.i("ABOUT TO FETCH", "about to fetch data");
                data = downloadUrl(url);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // Creating ParserTask
            parserTask = new ParserTask();
            // Starting Parsing the JSON string returned by Web Service
            parserTask.execute(result);
        }
    }

    private void getLastLocation() {
        if (checkPermission()) {
            client.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null && isMapReady) {
                                Log.e("Loc", location.toString());
                                if (bmGoogleMap
                                        .getProjection()
                                        .getVisibleRegion()
                                        .latLngBounds
                                        .contains(
                                                new LatLng(
                                                        location.getLatitude(),
                                                        location.getLongitude()
                                                )
                                        )
                                ) {
                                    bmGoogleMap.addMarker(new MarkerOptions().position(
                                            new LatLng(
                                                    location.getLatitude(),
                                                    location.getLongitude()))
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                                            .draggable(false)
                                            .visible(true)
                                    );
                                    sourceLocation.setVisibility(View.GONE);
                                    Log.e("Loc", location.toString() + " Is in range");
                                } else {
                                    Snackbar.make(
                                            sourceLocation,
                                            "You re currently not in Mauritius, please enter an address",
                                            Snackbar.LENGTH_LONG
                                    ).show();
                                    Log.e("Loc", location.toString() + " Is out of range");
                                    sourceLocation.setVisibility(View.VISIBLE);
                                }
                            } else {
                                Snackbar.make(
                                        sourceLocation,
                                        "You re currently not in Mauritius, please enter an address",
                                        Snackbar.LENGTH_LONG
                                ).show();
                                Log.e("Loc", location.toString() + " Is out of range");
                                sourceLocation.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    1023
            );
        }
    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED);
    }

    private void checkLocationSetting() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(request);
        SettingsClient settingClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingClient.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getLastLocation();
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainMapActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLastLocation();
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    @SuppressLint("StaticFieldLeak")
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;

            PlaceJsonParser placeJsonParser = new PlaceJsonParser();

            try {
                jObject = new JSONObject(jsonData[0]);

                // Getting the parsed data as a List construct
                places = placeJsonParser.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {
            selectedBox.setAdapter(null);
            String[] from = new String[]{"description"};
            int[] to = new int[]{android.R.id.text1};

            // Creating a SimpleAdapter for the AutoCompleteTextView
            if (result != null) {
                SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);

                // Setting the adapter
                selectedBox.setAdapter(adapter);
                selectedBox.showDropDown();
            }

        }
    }

    /**
     * A method to download json data from url
     */
    @SuppressLint("LongLogTag")
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception while downloading url", e.toString());
        } finally {
            if (iStream != null) {
                iStream.close();
                urlConnection.disconnect();
            }
        }
        return data;
    }


    public void persistData(LatLng sourceLatLng, String sourceDescription,
                            LatLng destinationLatLng, String destinationDescription) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            LocationRecording locationRecording = new LocationRecording(
                    sourceLatLng.latitude,
                    sourceLatLng.longitude,
                    sourceDescription,
                    destinationLatLng.latitude,
                    destinationLatLng.longitude,
                    destinationDescription,
                    FirebaseAuth.getInstance().getCurrentUser().getUid(),
                    Utils.getToken(this)
            );

            provider.recordTraffic(locationRecording);
        }
    }


    public void plotAllTrafficGraph(GoogleMap googleMap) {
        provider.retrieveAllRecordings();
        List<LocationRecording> locationRecordings = SessionVariables.storedRecordings;

        for (LocationRecording locationRecording : locationRecordings) {

            LatLng sourceLatLng = new LatLng(locationRecording.getSourceLatitude(), locationRecording.getSourceLongitude());
            LatLng destinationLatLng = new LatLng(locationRecording.getDestinationLatitude(), locationRecording.getDestinationLongitude());

//            LatLngBounds.Builder builder = new LatLngBounds.Builder();
//            builder.include(sourceLatLng);
//            builder.include(destinationLatLng);
//
//            LatLngBounds bounds = builder.build();
//            int padding=0;
//            CameraUpdate cu = Camer
            MarkerOptions sourceMarkerOptions = new MarkerOptions()
                    .position(sourceLatLng).title(locationRecording.getSourceLocationAddress())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                    .draggable(false).visible(true);

            MarkerOptions destinationMarkerOptions = new MarkerOptions()
                    .position(destinationLatLng).title(locationRecording.getSourceLocationAddress())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .draggable(false).visible(true);

            googleMap.addMarker(sourceMarkerOptions);
            googleMap.addMarker(destinationMarkerOptions);


        }

    }


}
