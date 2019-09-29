package com.example.googlemapsresizecircle;


import com.google.android.gms.maps.model.LatLng;

public interface MapsContract {
    interface Presenter{
        void init();
        void buildLocation();

    }
    interface View{
        void setMarker(final LatLng latLng);
        void searchLocation();
        void setUpdateLocation();
        void buildGoogleApiClient();
        void createLocationRequest();
        void displayLocation();
    }
}
