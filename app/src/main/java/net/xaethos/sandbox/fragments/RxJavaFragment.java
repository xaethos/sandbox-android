package net.xaethos.sandbox.fragments;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import net.xaethos.sandbox.R;

import java.util.concurrent.TimeUnit;

public class RxJavaFragment extends Fragment {

    private final GoogleApiClient.ConnectionCallbacks mConnectionListener;
    private final LocationCallback mLocationCallback;

    GoogleApiClient mApiClient;

    TextView mCoordView;

    public RxJavaFragment() {
        mConnectionListener = new ConnectionCallback();
        mLocationCallback = new LocationCallback();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mApiClient = new GoogleApiClient.Builder(activity).addApi(LocationServices.API)
                .addConnectionCallbacks(mConnectionListener)
                .build();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_rx_java, container, false);
        mCoordView = (TextView) root.findViewById(R.id.coordinates);
        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        mApiClient.connect();
    }

    @Override
    public void onStop() {
        mApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onDetach() {
        mApiClient = null;
        super.onDetach();
    }

    private class ConnectionCallback implements GoogleApiClient.ConnectionCallbacks {
        @Override
        public void onConnected(Bundle bundle) {
            LocationRequest request = new LocationRequest();
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            request.setInterval(TimeUnit.SECONDS.toMillis(10));
            request.setFastestInterval(TimeUnit.SECONDS.toMillis(5));

            LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient,
                    request,
                    mLocationCallback,
                    Looper.getMainLooper());
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    }

    private class LocationCallback extends com.google.android.gms.location.LocationCallback {
        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            if (!locationAvailability.isLocationAvailable()) {
                mCoordView.setText(R.string.ellipsis);
            } else {
                updateCoordinates(LocationServices.FusedLocationApi.getLastLocation(mApiClient));
            }
        }

        @Override
        public void onLocationResult(LocationResult result) {
            updateCoordinates(result.getLastLocation());
        }

        private void updateCoordinates(Location location) {
            if (location == null) return;

            mCoordView.setText(String.format("%.3f, %.3f",
                    location.getLatitude(),
                    location.getLongitude()));
        }
    }

}
