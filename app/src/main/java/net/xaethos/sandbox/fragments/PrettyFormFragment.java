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
import rx.subscriptions.CompositeSubscription;

public class PrettyFormFragment extends Fragment {

    TextInputLayout mEmailInput;
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

        mEmailInput = setUpTextInputLayout(root, R.id.input_email);
        mThingamajigInput = setUpTextInputLayout(root, R.id.input_thingamajig);
        mFiddlesticksInput = setUpTextInputLayout(root, R.id.input_fiddlesticks);

        return root;
    }

    @Override
    public void onDestroyView() {
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

        private CharSequence mEmailText;
        private CharSequence mEmailError;

        public CharSequence getEmailText() {
            return mEmailText;
        }

        public void setEmailText(CharSequence emailText) {
            mEmailError = validateNotEmpty(emailText);
            mEmailText = emailText;
        }

        public CharSequence getEmailError() {
            return mEmailError;
        }

        public void setEmailError(CharSequence emailError) {
            mEmailError = nullIfEmpty(emailError);
        }

        public boolean isEmailValid() {
            return mEmailError == null;
        }

        private CharSequence nullIfEmpty(CharSequence text) {
            if (text == null) return null;
            return text.length() == 0 ? null : text;
        }

        private CharSequence validateNotEmpty(CharSequence text) {
            if (text != null && text.length() > 0) return null;
            return "cannot be empty";
        }

    }

}
