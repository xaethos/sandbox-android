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
import net.xaethos.sandbox.rx.LocationStream;
import net.xaethos.sandbox.rx.TextViewSetTextAction;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class RxJavaFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks {

    GoogleApiClient mApiClient;

    LocationStream mLocationStream;
    CompositeSubscription mSubscriptions;

    SupportMapFragment mMapFragment;
    TextView mCoordinatesView;
    TextView mCounterView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApiClient = new GoogleApiClient.Builder(getActivity()).addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        mLocationStream = new LocationStream(mApiClient);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_rx_java, container, false);

        mCoordinatesView = (TextView) root.findViewById(R.id.coordinates);
        mCounterView = (TextView) root.findViewById(R.id.time);
        mMapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        mApiClient.connect();

        mSubscriptions = new CompositeSubscription();
        subscribeCounter(mSubscriptions, mCounterView);
        subscribeCoordinates(mSubscriptions, mCoordinatesView);
        subscribeMap(mSubscriptions, mMapFragment);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSubscriptions.unsubscribe();
        mApiClient.disconnect();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMapFragment = null;
        mCounterView = null;
        mCoordinatesView = null;
    }

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

    private void subscribeCounter(
            final CompositeSubscription subscriptions, final TextView counterView) {
        if (subscriptions == null || counterView == null) return;

        subscriptions.add(newCounterObservable().map(new Func1<Integer, CharSequence>() {
            @Override
            public CharSequence call(Integer value) {
                return "Count " + value;
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new TextViewSetTextAction(counterView)));
    }

    private void subscribeCoordinates(
            final CompositeSubscription subscriptions, final TextView coordView) {
        if (subscriptions == null || coordView == null) return;

        subscriptions.add(mLocationStream.getLocationObservable()
                .map(new Func1<Location, CharSequence>() {
                    @Override
                    public CharSequence call(Location location) {
                        if (location == null) {
                            return getText(R.string.ellipsis);
                        } else {
                            return String.format("%.4f, %.4f",
                                    location.getLatitude(),
                                    location.getLongitude());
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new TextViewSetTextAction(coordView)));
    }

    private void subscribeMap(
            final CompositeSubscription subscriptions, final SupportMapFragment mapFragment) {
        if (subscriptions == null || mapFragment == null) return;

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                googleMap.getUiSettings().setAllGesturesEnabled(false);

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
                                googleMap.animateCamera(update);
                            }
                        }));
            }
        });
    }

}
