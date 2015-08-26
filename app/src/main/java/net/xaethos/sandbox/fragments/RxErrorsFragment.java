package net.xaethos.sandbox.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.xaethos.sandbox.R;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class RxErrorsFragment extends Fragment {
    private static final int[] SONGS =
            {R.array.bottles_of_beer, R.array.little_ducks, R.array.monkeys_on_the_bed
            };

    final Random mRand;

    volatile ConnectableObservable<Integer> mCountObservable;
    volatile ConnectableObservable<Integer> mSongObservable;

    private CompositeSubscription mSubscriptions;

    public RxErrorsFragment() {
        mRand = new Random();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        createCountObservable.call();
        createSongObservable.call();
        mSubscriptions = new CompositeSubscription();

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

    private void setUpCountCard(View card, final CompositeSubscription subscriptions) {
        final TextView textView = (TextView) card.findViewById(R.id.text);
        final Button actionView = (Button) card.findViewById(R.id.action);
        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionView.setEnabled(false);
                subscribeCountCard(textView, actionView, subscriptions);
                subscriptions.add(countObservable().connect());
            }
        });

        subscribeCountCard(textView, actionView, subscriptions);
        subscriptions.add(countObservable().connect());
    }

    private void subscribeCountCard(
            TextView textView, Button actionView, CompositeSubscription subscriptions) {

        subscriptions.add(countObservable().map(new Func1<Integer, CharSequence>() {
            @Override
            public CharSequence call(Integer initialCount) {
                return String.format("%d!", initialCount);
            }
        }).subscribe(new RequestCardSubscriber(textView, actionView)));
    }

    private void setUpSongCard(View card, final CompositeSubscription subscriptions) {
        final TextView textView = (TextView) card.findViewById(R.id.text);
        final Button actionView = (Button) card.findViewById(R.id.action);
        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionView.setEnabled(false);
                subscribeSongCard(textView, actionView, subscriptions);
                subscriptions.add(songObservable().connect());
            }
        });

        subscribeSongCard(textView, actionView, subscriptions);
        subscriptions.add(songObservable().connect());
    }

    private void subscribeSongCard(
            TextView textView, Button actionView, CompositeSubscription subscriptions) {
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
        }).subscribe(new RequestCardSubscriber(textView, actionView)));
    }

    private void setUpLyricsCard(View card, CompositeSubscription subscriptions) {
        subscribeLyricsCard((TextView) card.findViewById(R.id.text), subscriptions);
    }

    private void subscribeLyricsCard(
            final TextView textView, final CompositeSubscription subscriptions) {
        subscriptions.add(createLyricsObservable(songObservable(),
                countObservable()).subscribe(new Subscriber<CharSequence>() {

            @Override
            public void onNext(CharSequence charSequence) {
                textView.setText(charSequence);
            }

            @Override
            public void onError(Throwable e) {
                textView.setText("please don't interrupt me");
                subscribeLyricsCard(textView, subscriptions);
            }

            @Override
            public void onCompleted() {
                textView.setText("That's all");
            }
        }));
    }

    private ConnectableObservable<Integer> countObservable() {
        return mCountObservable;
    }

    private ConnectableObservable<Integer> songObservable() {
        return mSongObservable;
    }

    private Observable<CharSequence> createLyricsObservable(
            Observable<Integer> songObservable, Observable<Integer> countObservable) {
        Observable<Observable<CharSequence>> songSequenceObservable = Observable.combineLatest(
                songObservable,
                countObservable,
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

        return Observable.switchOnNext(songSequenceObservable)
                .observeOn(AndroidSchedulers.mainThread());
    }

    private int rand(int lower, int upper) {
        return lower + mRand.nextInt(1 + upper - lower);
    }

    private final Action0 createCountObservable = new Action0() {
        @Override
        public void call() {
            Observable<Integer> observable =
                    Observable.create(new Observable.OnSubscribe<Integer>() {
                        @Override
                        public void call(Subscriber<? super Integer> subscriber) {
                            while (!subscriber.isUnsubscribed()) {
                                if (mRand.nextInt(6) == 0) {
                                    subscriber.onError(new Exception("BLARGH!"));
                                    return;
                                }

                                subscriber.onNext(rand(5, 99));

                                try {
                                    Thread.sleep(rand(8000, 16000));
                                } catch (InterruptedException e) {
                                    if (!subscriber.isUnsubscribed()) subscriber.onError(e);
                                    return;
                                }
                            }
                        }
                    });

            mCountObservable = observable.doOnTerminate(this)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .replay(1);
        }
    };

    private final Action0 createSongObservable = new Action0() {
        @Override
        public void call() {
            Observable<Integer> observable =
                    Observable.create(new Observable.OnSubscribe<Integer>() {
                        @Override
                        public void call(Subscriber<? super Integer> subscriber) {
                            while (!subscriber.isUnsubscribed()) {
                                if (mRand.nextInt(6) == 0) {
                                    subscriber.onError(new Exception("BARF!"));
                                    return;
                                }

                                subscriber.onNext(SONGS[mRand.nextInt(SONGS.length)]);

                                try {
                                    Thread.sleep(rand(10000, 20000));
                                } catch (InterruptedException e) {
                                    if (!subscriber.isUnsubscribed()) subscriber.onError(e);
                                    return;
                                }
                            }
                        }
                    });

            mSongObservable = observable.doOnTerminate(this)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .distinctUntilChanged()
                    .replay(1);
        }
    };

    private static class RequestCardSubscriber extends Subscriber<CharSequence> {
        final TextView textView;
        final Button actionView;

        private RequestCardSubscriber(TextView textView, Button actionView) {
            this.textView = textView;
            this.actionView = actionView;
        }

        @Override
        public void onNext(CharSequence text) {
            textView.setText(text);
        }

        @Override
        public void onError(Throwable e) {
            textView.setText(e.getMessage());
            actionView.setEnabled(true);
        }

        @Override
        public void onCompleted() {
            textView.setText("No more!");
        }
    }
}
