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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import net.xaethos.sandbox.R;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class RxJavaFragment extends Fragment {

    private final GoogleApiClient.ConnectionCallbacks mConnectionListener;

    GoogleApiClient mApiClient;
    LocationStream mLocationStream;

    public RxJavaFragment() {
        mConnectionListener = new ConnectionCallback();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mApiClient = new GoogleApiClient.Builder(activity).addApi(LocationServices.API)
                .addConnectionCallbacks(mConnectionListener)
                .build();
        mLocationStream = new LocationStream(mApiClient);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_rx_java, container, false);

        final TextView coordView = (TextView) root.findViewById(R.id.coordinates);
        Observable.create(mLocationStream)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Location>() {
                    @Override
                    public void call(Location location) {
                        if (location == null) {
                            coordView.setText(R.string.ellipsis);
                        } else {
                            coordView.setText(String.format("%.4f, %.4f",
                                    location.getLatitude(),
                                    location.getLongitude()));
                        }
                    }
                });

        final TextView timeView = (TextView) root.findViewById(R.id.time);
        Integer[] digits = new Integer[20];
        for (int i = 0; i < digits.length; ++i) digits[i] = i;
        counterObservable().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer value) {
                        timeView.setText("Count " + value);
                    }
                });

        return root;
    }

    private Observable<Integer> counterObservable() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            private int mCounter;

            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                while (true) {
                    if (subscriber.isUnsubscribed()) return;
                    subscriber.onNext(mCounter++);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        if (!subscriber.isUnsubscribed()) subscriber.onError(e);
                    }
                }
            }
        });
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

    private static class LocationStream extends LocationCallback
            implements Observable.OnSubscribe<Location> {

        private final GoogleApiClient mApiClient;
        Subscriber<? super Location> mSubscriber;

        public LocationStream(GoogleApiClient apiClient) {
            mApiClient = apiClient;
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            if (!locationAvailability.isLocationAvailable()) {
                emit(null);
            } else if (mApiClient.isConnected()) {
                emit(LocationServices.FusedLocationApi.getLastLocation(mApiClient));
            }
        }

        @Override
        public void onLocationResult(LocationResult result) {
            emit(result.getLastLocation());
        }

        @Override
        public void call(Subscriber<? super Location> subscriber) {
            mSubscriber = subscriber;
        }

        private void emit(Location location) {
            if (mSubscriber == null) return;

            if (mSubscriber.isUnsubscribed()) {
                mSubscriber = null;
                return;
            }
            mSubscriber.onNext(location);
        }
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
                    mLocationStream,
                    Looper.getMainLooper());
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    }

}
