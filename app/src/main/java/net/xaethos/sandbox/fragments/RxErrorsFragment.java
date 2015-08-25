package net.xaethos.sandbox.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.xaethos.sandbox.R;
import net.xaethos.sandbox.rx.TextViewSetTextAction;

import java.util.Random;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class RxErrorsFragment extends Fragment {

    private final Random mRand;

    private CompositeSubscription mSubscriptions;

    public RxErrorsFragment() {
        mRand = new Random();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mSubscriptions = new CompositeSubscription();
        View root = inflater.inflate(R.layout.fragment_rx_errors, container, false);

        setUpCountCard(root.findViewById(R.id.count), mSubscriptions);

        return root;
    }

    @Override
    public void onDestroyView() {
        mSubscriptions.unsubscribe();
        mSubscriptions = null;
        super.onDestroyView();
    }

    private void setUpCountCard(View card, CompositeSubscription subscriptions) {
        TextView textView = (TextView) card.findViewById(R.id.text);

        Observable<CharSequence> observable =
                Observable.create(new Observable.OnSubscribe<Integer>() {
                    @Override
                    public void call(Subscriber<? super Integer> subscriber) {
                        while (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(rand(5, 99));
                            try {
                                Thread.sleep(rand(3000, 10000));
                            } catch (InterruptedException e) {
                                if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
                                return;
                            }
                        }
                    }
                }).map(new Func1<Integer, CharSequence>() {
                    @Override
                    public CharSequence call(Integer integer) {
                        return String.format("%d!", integer);
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

        subscriptions.add(observable.subscribe(new TextViewSetTextAction(textView)));
    }

    private int rand(int lower, int upper) {
        return lower + mRand.nextInt(1 + upper - lower);
    }
}
