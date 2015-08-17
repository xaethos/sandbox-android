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

    public static Observable<CharSequence> onTextChangedObservable(EditText editText) {
        return Observable.create(new OnTextChangeSubscribe(editText));
    }

    private static class OnTextChangeSubscribe implements Observable.OnSubscribe<CharSequence> {
        private final TextView input;

        public OnTextChangeSubscribe(final TextView input) {
            this.input = input;
        }

        @Override
        public void call(final Subscriber<? super CharSequence> subscriber) {
            final TextWatcher watcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    //noop
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    subscriber.onNext(s);
                }

                @Override
                public void afterTextChanged(final Editable editable) {
                    //noop
                }
            };

            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(input.getText());
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

}
