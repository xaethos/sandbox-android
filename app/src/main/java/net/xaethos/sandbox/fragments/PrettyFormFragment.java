package net.xaethos.sandbox.fragments;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.xaethos.sandbox.R;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

public class PrettyFormFragment extends Fragment {

    TextInputLayout mThingamajigInput;
    TextInputLayout mFiddlesticksInput;

    FormController mController;
    CompositeSubscription mSubscriptions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController = new FormController();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pretty_form, container, false);
        mSubscriptions = new CompositeSubscription();

        final TextInputLayout emailInput = setUpTextInputLayout(root, R.id.input_email);
        TextChangeEventStream stream = new TextChangeEventStream();
        emailInput.getEditText().addTextChangedListener(stream.getTextWatcher());
        mController.setEmailTextChangeObservable(stream.getObservable());
        mController.getEmailErrorsObservable().subscribe(new Action1<CharSequence>() {
            @Override
            public void call(CharSequence errorText) {
                emailInput.setError(errorText);
            }
        });

        mThingamajigInput = setUpTextInputLayout(root, R.id.input_thingamajig);
        mFiddlesticksInput = setUpTextInputLayout(root, R.id.input_fiddlesticks);

        return root;
    }

    @Override
    public void onDestroyView() {
        mThingamajigInput = null;
        mFiddlesticksInput = null;

        mSubscriptions.unsubscribe();
        mSubscriptions = null;
        super.onDestroyView();
    }

    private TextInputLayout setUpTextInputLayout(View container, int viewId) {
        TextInputLayout input = (TextInputLayout) container.findViewById(viewId);
        input.setErrorEnabled(true);
        return input;
    }

    public static class TextChangeEventStream {

        final private SubscribableWatcher mTextWatcher;
        final private Observable<Editable> mEditableObservable;

        public TextChangeEventStream() {
            mTextWatcher = new SubscribableWatcher();
            mEditableObservable = Observable.create(mTextWatcher).publish().autoConnect();
        }

        public TextWatcher getTextWatcher() {
            return mTextWatcher;
        }

        public Observable<Editable> getObservable() {
            return mEditableObservable;
        }

        private class SubscribableWatcher implements TextWatcher, Observable.OnSubscribe<Editable> {

            private Subscriber<? super Editable> mSubscriber;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //noop
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //noop
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mSubscriber.isUnsubscribed()) {
                    mSubscriber = null;
                    return;
                }
                mSubscriber.onNext(s);
            }

            @Override
            public void call(Subscriber<? super Editable> subscriber) {
                mSubscriber = subscriber;
            }
        }

    }

    public static class FormController {

        private Observable<CharSequence> mEmailErrorsObservable;

        public void setEmailTextChangeObservable(
                Observable<? extends CharSequence> observable) {
            mEmailErrorsObservable = observable.map(VALIDATE_NOT_EMPTY);
        }

        public Observable<CharSequence> getEmailErrorsObservable() {
            return mEmailErrorsObservable;
        }

        private static final Func1<CharSequence, CharSequence> VALIDATE_NOT_EMPTY =
                new Func1<CharSequence, CharSequence>() {
                    @Override
                    public CharSequence call(CharSequence inputText) {
                        if (inputText != null && inputText.length() > 0) return null;
                        return "required";
                    }
                };

    }

}
