package net.xaethos.sandbox.rx;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class EditTextObservables {

    private EditTextObservables() {
    }

    public static Observable<Editable> afterTextChangedObservable(EditText editText) {
        return Observable.create(new OnEditableSubscribe(editText)).publish().refCount();
    }

    private static class OnEditableSubscribe implements Observable.OnSubscribe<Editable> {
        private final TextView input;

        public OnEditableSubscribe(final TextView input) {
            this.input = input;
        }

        @Override
        public void call(final Subscriber<? super Editable> subscriber) {
            final TextWatcher watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    //noop
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //noop
                }

                @Override
                public void afterTextChanged(final Editable editable) {
                    subscriber.onNext(editable);
                }
            };

            input.addTextChangedListener(watcher);
            subscriber.add(Subscriptions.create(new Action0() {
                @Override
                public void call() {
                    input.removeTextChangedListener(watcher);
                }
            }));
        }
    }

}
