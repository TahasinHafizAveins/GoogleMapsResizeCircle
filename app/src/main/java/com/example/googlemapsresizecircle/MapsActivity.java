package com.example.googlemapsresizecircle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, MapDialog.DialogListener, MapsContract.View, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener {

    MapsContract.Presenter mPresenter;
    private GoogleMap mMap;
    private static final String TAG = "MapActivity";
    private static final int PERMISSION_REQUEST_CODE = 7001;
    private static final int PLAY_SERVICE_REQUEST = 7002;
    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;
    private static final int DISPLACEMENT = 10;
    private LocationRequest mLocationRequest;
    private Location mLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final float Default_Zoom = 15f;
    private GoogleApiClient mGoogleApiClient;
    Marker marker;
    private Circle mCircle;
    private LatLng cLatLng = null;
    Double radius = null;
    boolean markerClicked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mPresenter = new MapsPresenter(this);
        mPresenter.init();


    }

    void getLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            Task location = mFusedLocationClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful()) {
                        Log.d(TAG, "OnCompleteListener: Location found");
                        Location currentLocation = (Location) task.getResult();
                        assert currentLocation != null;
                        moveCamera(currentLocation, Default_Zoom, "My Location");

                    } else {
                        Log.d(TAG, "OnCompleteListener: Location is null");
                        Toast.makeText(MapsActivity.this, "OnCompleteListener: Location is null", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException:" + e.getMessage());
        }

    }

    @Override
    public void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void displayLocation() {
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, PERMISSION_REQUEST_CODE);
        }
        getLocation();
    }

    @Override
    public void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        mPresenter.buildLocation();
                    }
                }
            }
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICE_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    private void drawCircle() {
        mMap.addCircle(new CircleOptions()
                .center(cLatLng)
                .radius(500.0)
                .strokeWidth(5f)
                .clickable(true)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(70, 150, 50, 50))
        );
        cLatLng = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                mMap.clear();
                if (cLatLng == null) {
                    Log.d(TAG, "Latlong null");
                    cLatLng = point;
                    drawCircle();

                }
                setMarker(point);
            }
        });

        mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {
                showCustomDialog(circle);
            }
        });
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerDragListener(this);
        markerClicked = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        displayLocation();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    private void moveCamera(Location location, float zoom, String title) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        setMarker(latLng);
    }

    public void setMarker(final LatLng latLng) {
        if (marker != null) {
            marker.remove();
        }

        Log.d(TAG, "moveCamera: moving camera to : latitude: " + latLng.latitude + ", longitude: " + latLng.longitude);
        marker = mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F));
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
        marker.setAnchor(0.5f,0.5f);
        marker.setDraggable(true);

    }

    @Override
    public void searchLocation() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyC99rc4wXzJvqiBjQryKt18cdTQYxxBAG4");
        }

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS));

        autocompleteFragment.setCountry("ID");
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                final LatLng latLngPlace = place.getLatLng();
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + ", " + place.getAddress());
                setMarker(latLngPlace);

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.d(TAG, "An error occurred: " + status);
            }
        });
    }

    @Override
    public void setUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    private void showCustomDialog(Circle circle) {
        mCircle = circle;

        Log.d(TAG, "Open Dialog");
        Bundle bundle = new Bundle();
        bundle.putDouble("radius", circle.getRadius());

        MapDialog mapDialog = new MapDialog();
        mapDialog.setCancelable(false);
        mapDialog.setArguments(bundle);

        mapDialog.show(getSupportFragmentManager(), TAG);


    }

    @Override
    public void onFinishEditDialog(Double inputText) {
        mCircle.setRadius(inputText);
        Log.d(TAG, "gettingRadius: " + radius);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Log.d("onMapLongClick",""+latLng);
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true));

        mMap.clear();
        if (cLatLng == null) {
            Log.d(TAG, "Latlong null");
            cLatLng = latLng;
            drawCircle();
        }
        markerClicked = false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        Log.d("onMarkerDragStart","Marker " + marker.getId() + " DragStart");
    }

    @Override
    public void onMarkerDrag(Marker marker) {

        Log.d("onMarkerDrag","Marker " + marker.getId() + " Drag@" + marker.getPosition());
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng point = marker.getPosition();
        mMap.clear();
        if (cLatLng == null) {
            Log.d(TAG, "Latlong null");
            cLatLng = point;
            drawCircle();
        }
        setMarker(point);
        Log.d("onMarkerDragEnd","Marker " + marker.getId() + " DragEnd"+point);
    }
}