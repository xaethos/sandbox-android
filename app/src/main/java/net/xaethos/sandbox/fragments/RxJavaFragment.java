package net.xaethos.sandbox.fragments;

import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import net.xaethos.sandbox.R;
import net.xaethos.sandbox.observables.LocationStream;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class RxJavaFragment extends Fragment {

    private final GoogleApiClient.ConnectionCallbacks mConnectionListener;

    GoogleApiClient mApiClient;

    LocationStream mLocationStream;

    CompositeSubscription mSubscriptions;
    Action1<Location> mCoordinateSubscriber;
    Action1<Integer> mCounterSubscriber;

    public RxJavaFragment() {
        mConnectionListener = new ConnectionCallback();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApiClient = new GoogleApiClient.Builder(getActivity()).addApi(LocationServices.API)
                .addConnectionCallbacks(mConnectionListener)
                .build();
        mLocationStream = new LocationStream(mApiClient);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_rx_java, container, false);

        final TextView coordView = (TextView) root.findViewById(R.id.coordinates);
        mCoordinateSubscriber = new Action1<Location>() {
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
        };

        final TextView timeView = (TextView) root.findViewById(R.id.time);
        mCounterSubscriber = new Action1<Integer>() {
            @Override
            public void call(Integer value) {
                timeView.setText("Count " + value);
            }
        };

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        final CompositeSubscription subscriptions = new CompositeSubscription();
        mSubscriptions = subscriptions;

        mSubscriptions.add(mLocationStream.getLocationObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mCoordinateSubscriber));

        mSubscriptions.add(newCounterObservable().subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mCounterSubscriber));

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                subscriptions.add(mLocationStream.getLocationObservable()
                        .filter(new Func1<Location, Boolean>() {
                            @Override
                            public Boolean call(Location location) {
                                return location != null;
                            }
                        })
                        .map(new Func1<Location, CameraUpdate>() {
                            @Override
                            public CameraUpdate call(Location location) {
                                return CameraUpdateFactory.newLatLngZoom(new LatLng(location
                                        .getLatitude(),
                                        location.getLongitude()), 15f);
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<CameraUpdate>() {
                            @Override
                            public void call(CameraUpdate update) {
                                googleMap.getUiSettings().setAllGesturesEnabled(false);
                                googleMap.animateCamera(update);
                            }
                        }));
            }
        });

        mApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSubscriptions.unsubscribe();
        mSubscriptions = null;

        mApiClient.disconnect();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCoordinateSubscriber = null;
        mCounterSubscriber = null;
    }

    private Observable<Integer> newCounterObservable() {
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

    private class ConnectionCallback implements GoogleApiClient.ConnectionCallbacks {
        @Override
        public void onConnected(Bundle bundle) {
            LocationRequest request = new LocationRequest();
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            request.setInterval(TimeUnit.SECONDS.toMillis(10));
            request.setFastestInterval(TimeUnit.SECONDS.toMillis(5));

            LocationServices.FusedLocationApi.requestLocationUpdates(mApiClient,
                    request,
                    mLocationStream.getLocationCallback(),
                    Looper.getMainLooper());
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    }

}
