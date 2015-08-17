package net.xaethos.sandbox.fragments;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.xaethos.sandbox.R;
import net.xaethos.sandbox.rx.FormActions;

import java.util.Arrays;
import java.util.regex.Pattern;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.FuncN;
import rx.subscriptions.CompositeSubscription;

import static net.xaethos.sandbox.rx.EditTextObservables.onTextChangedObservable;

public class PrettyFormFragment extends Fragment {

    CompositeSubscription mSubscriptions;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pretty_form, container, false);

        final TextInputLayout emailInput = setUpTextInputLayout(root, R.id.input_email);
        final TextInputLayout thingamajigInput = setUpTextInputLayout(root, R.id.input_thingamajig);
        final TextInputLayout fiddlesticksInput =
                setUpTextInputLayout(root, R.id.input_fiddlesticks);
        final View submitButton = root.findViewById(R.id.btn_submit);

        FormController controller =
                new FormController(onTextChangedObservable(emailInput.getEditText()),
                        onTextChangedObservable(thingamajigInput.getEditText()),
                        onTextChangedObservable(fiddlesticksInput.getEditText()));

        mSubscriptions = new CompositeSubscription();
        mSubscriptions.add(controller.emailErrors()
                .subscribe(FormActions.setTextInputError(emailInput)));
        mSubscriptions.add(controller.thingamajigErrors()
                .subscribe(FormActions.setTextInputError(thingamajigInput)));
        mSubscriptions.add(controller.fiddlesticksErrors()
                .subscribe(FormActions.setTextInputError(fiddlesticksInput)));
        mSubscriptions.add(controller.submitEnabledControl()
                .subscribe(FormActions.setViewEnabled(submitButton)));

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

    public static class FormController {

        private final Observable<CharSequence> mEmailErrorsObservable;
        private final Observable<CharSequence> mThingamajigErrorsObservable;
        private final Observable<CharSequence> mFiddlesticksErrorsObservable;

        private final Observable<Boolean> mSubmitEnabledObservable;

        public FormController(
                Observable<? extends CharSequence> emailText,
                Observable<? extends CharSequence> thingamajigText,
                Observable<? extends CharSequence> fiddlesticksText) {

            mEmailErrorsObservable = createEmailValidation(emailText);
            mThingamajigErrorsObservable = createThingmajigValidation(thingamajigText);
            mFiddlesticksErrorsObservable = createFiddlesticksValidation(fiddlesticksText);

            mSubmitEnabledObservable =
                    Observable.combineLatest(Arrays.asList(mEmailErrorsObservable,
                            mThingamajigErrorsObservable,
                            mFiddlesticksErrorsObservable), new FuncN<Boolean>() {
                        @Override
                        public Boolean call(Object... errors) {
                            for (Object error : errors) if (error != null) return false;
                            return true;
                        }
                    }).distinctUntilChanged();
        }

        // Outputs

        public Observable<CharSequence> emailErrors() {
            return mEmailErrorsObservable;
        }

        public Observable<CharSequence> thingamajigErrors() {
            return mThingamajigErrorsObservable;
        }

        public Observable<CharSequence> fiddlesticksErrors() {
            return mFiddlesticksErrorsObservable;
        }

        public Observable<Boolean> submitEnabledControl() {
            return mSubmitEnabledObservable;
        }

        // Private

        private static final Pattern PATTERN_EMAIL =
                Pattern.compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\" +
                        ".[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\" +
                        ".)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?");

        private static Observable<CharSequence> createEmailValidation(
                Observable<? extends
                        CharSequence> input) {
            return input.map(new Func1<CharSequence, CharSequence>() {
                @Override
                public CharSequence call(CharSequence inputText) {
                    if (inputText.length() == 0) return "required";
                    if (!PATTERN_EMAIL.matcher(inputText).matches()) return "invalid email";
                    return null;
                }
            }).distinctUntilChanged().replay(1).refCount();
        }

        private static Observable<CharSequence> createThingmajigValidation(
                Observable<? extends
                        CharSequence> input) {
            return input.map(new Func1<CharSequence, CharSequence>() {
                @Override
                public CharSequence call(CharSequence charSequence) {
                    return charSequence.length() > 10 ? "max 10 characters" : null;
                }
            }).distinctUntilChanged().replay(1).refCount();
        }

        private static Observable<CharSequence> createFiddlesticksValidation(
                Observable<? extends CharSequence> textInput) {
            return textInput.map(new Func1<CharSequence, CharSequence>() {
                public CharSequence call(CharSequence countText) {
                    if (countText.length() == 0) return null;
                    int count;
                    try {
                        count = Integer.parseInt(countText.toString());
                    } catch (NumberFormatException e) {
                        return "invalid number";
                    }
                    if (count < 0) return "must be positive";
                    return null;
                }
            }).distinctUntilChanged().replay(1).refCount();
        }

    }

}
