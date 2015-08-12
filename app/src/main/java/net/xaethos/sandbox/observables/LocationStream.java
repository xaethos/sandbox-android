package net.xaethos.sandbox.observables;

import android.location.Location;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import rx.Observable;
import rx.Subscriber;

public class LocationStream {

    final GoogleApiClient mApiClient;

    final private SubscribableCallback mLocationCallback;
    final private Observable<Location> mLocationObservable;

    public LocationStream(GoogleApiClient apiClient) {
        mApiClient = apiClient;
        mLocationCallback = new SubscribableCallback();
        mLocationObservable = Observable.create(mLocationCallback).replay(1).autoConnect();
    }

    public LocationCallback getLocationCallback() {
        return mLocationCallback;
    }

    public Observable<Location> getLocationObservable() {
        return mLocationObservable;
    }

    private class SubscribableCallback extends LocationCallback
            implements Observable.OnSubscribe<Location> {

        private Subscriber<? super Location> mSubscriber;

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
}
