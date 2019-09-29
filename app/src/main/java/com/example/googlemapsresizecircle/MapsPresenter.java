package com.example.googlemapsresizecircle;

public class MapsPresenter implements MapsContract.Presenter {

    private static final String TAG = "MapActivity";
    MapsContract.View mView;

    public MapsPresenter(MapsContract.View mView) {
        this.mView = mView;
    }



    @Override
    public void init() {
        mView.searchLocation();
        mView.setUpdateLocation();
    }

    @Override
    public void buildLocation() {
        mView.buildGoogleApiClient();
        mView.createLocationRequest();
        mView.displayLocation();
    }
}
