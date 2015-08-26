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
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class RxErrorsFragment extends Fragment {
    private static final int[] SONGS =
            {R.array.bottles_of_beer, R.array.little_ducks, R.array.monkeys_on_the_bed
            };

    private final Random mRand;

    private ConnectableObservable<Integer> mCountObservable;
    private ConnectableObservable<Integer> mSongObservable;

    private CompositeSubscription mSubscriptions;

    public RxErrorsFragment() {
        mRand = new Random();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mCountObservable = createCountObservable();
        mSongObservable = createSongObservable();
        mSubscriptions =
                new CompositeSubscription(mCountObservable.connect(), mSongObservable.connect());

        View root = inflater.inflate(R.layout.fragment_rx_errors, container, false);

        setUpCountCard(root.findViewById(R.id.count), mSubscriptions);
        setUpSongCard(root.findViewById(R.id.song), mSubscriptions);
        setUpLyricsCard(root.findViewById(R.id.lyrics), mSubscriptions);

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

        subscriptions.add(countObservable().map(new Func1<Integer, CharSequence>() {
            @Override
            public CharSequence call(Integer integer) {
                return String.format("%d!", integer);
            }
        }).subscribe(new TextViewSetTextAction(textView)));
    }

    private void setUpSongCard(View card, CompositeSubscription subscriptions) {
        TextView textView = (TextView) card.findViewById(R.id.text);

        subscriptions.add(songObservable().map(new Func1<Integer, CharSequence>() {
            @Override
            public CharSequence call(Integer songRes) {
                switch (songRes) {
                case R.array.monkeys_on_the_bed:
                    return "Monkeys!";
                case R.array.little_ducks:
                    return "Ducks!";
                case R.array.bottles_of_beer:
                    return "Beer!";
                }
                return "Uhh...";
            }
        }).subscribe(new TextViewSetTextAction(textView)));
    }

    private void setUpLyricsCard(View card, CompositeSubscription subscriptions) {
        TextView textView = (TextView) card.findViewById(R.id.text);

        Observable<Observable<CharSequence>> songSequenceObservable = Observable.combineLatest(
                songObservable(),
                countObservable(),
                new Func2<Integer, Integer, Observable<CharSequence>>() {
                    @Override
                    public Observable<CharSequence> call(
                            final Integer songRes, final Integer initalCount) {
                        final String[] lines = getResources().getStringArray(songRes);

                        return Observable.create(new Observable.OnSubscribe<CharSequence>() {
                            int line = 0;
                            int count = initalCount;

                            @Override
                            public void call(Subscriber<? super CharSequence> subscriber) {
                                while (!subscriber.isUnsubscribed()) {
                                    if (line == lines.length - 1) count--;
                                    subscriber.onNext(String.format(lines[line], count));
                                    line = (line + 1) % lines.length;
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
                                        return;
                                    }
                                }
                            }
                        }).subscribeOn(Schedulers.io()).delay(1200, TimeUnit.MILLISECONDS);
                    }
                });

        subscriptions.add(Observable.switchOnNext(songSequenceObservable)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new TextViewSetTextAction(textView)));
    }

    private ConnectableObservable<Integer> countObservable() {
        return mCountObservable;
    }

    public ConnectableObservable<Integer> songObservable() {
        return mSongObservable;
    }

    private ConnectableObservable<Integer> createCountObservable() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                while (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(rand(5, 99));
                    try {
                        Thread.sleep(rand(8000, 16000));
                    } catch (InterruptedException e) {
                        if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
                        return;
                    }
                }
            }
        }).compose(this.<Integer>ioBound()).replay(1);
    }

    private ConnectableObservable<Integer> createSongObservable() {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                while (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(SONGS[mRand.nextInt(SONGS.length)]);
                    try {
                        Thread.sleep(rand(10000, 20000));
                    } catch (InterruptedException e) {
                        if (!subscriber.isUnsubscribed()) subscriber.onCompleted();
                        return;
                    }
                }
            }
        }).compose(this.<Integer>ioBound()).distinctUntilChanged().replay(1);
    }

    private int rand(int lower, int upper) {
        return lower + mRand.nextInt(1 + upper - lower);
    }

    @SuppressWarnings("unchecked")
    <T> Observable.Transformer<T, T> ioBound() {
        return (Observable.Transformer<T, T>) IO_WORK_TRANSFORMER;
    }

    private static final Observable.Transformer<Object, Object> IO_WORK_TRANSFORMER =
            new Observable.Transformer<Object, Object>() {
                @Override
                public Observable<Object> call(Observable<Object> observable) {
                    return observable.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread());
                }
            };
}
