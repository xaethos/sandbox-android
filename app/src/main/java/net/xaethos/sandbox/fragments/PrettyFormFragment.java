package net.xaethos.sandbox.fragments;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.xaethos.sandbox.R;
import net.xaethos.sandbox.rx.FormActions;

import rx.Observable;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

import static net.xaethos.sandbox.rx.EditTextObservables.afterTextChangedObservable;

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
                new FormController(afterTextChangedObservable(emailInput.getEditText()),
                        afterTextChangedObservable(thingamajigInput.getEditText()),
                        afterTextChangedObservable(fiddlesticksInput.getEditText()));

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
            Observable<? extends CharSequence> sharedEmailText = emailText.share();
            Observable<? extends CharSequence> sharedThingamajigText = thingamajigText.share();
            Observable<? extends Integer> sharedFiddlesticksCount =
                    fiddlesticksText.map(new Func1<CharSequence, Integer>() {
                        @Override
                        public Integer call(CharSequence countText) {
                            if (countText == null) return null;
                            try {
                                return Integer.parseInt(countText.toString());
                            } catch (NumberFormatException e) {
                                return null;
                            }
                        }
                    }).share();

            mEmailErrorsObservable = sharedEmailText.map(VALIDATE_NOT_EMPTY);
            mThingamajigErrorsObservable = sharedThingamajigText.map(VALIDATE_NOT_EMPTY);
            mFiddlesticksErrorsObservable =
                    sharedFiddlesticksCount.map(new Func1<Integer, CharSequence>() {
                        @Override
                        public CharSequence call(Integer integer) {
                            if (integer == null) return "invalid number";
                            return null;
                        }
                    });

            mSubmitEnabledObservable = mEmailErrorsObservable.map(NO_ERROR);
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

        private static boolean isEmpty(CharSequence text) {
            return text == null || text.length() == 0;
        }

        private static final Func1<CharSequence, Boolean> NO_ERROR =
                new Func1<CharSequence, Boolean>() {
                    @Override
                    public Boolean call(CharSequence errorText) {
                        return isEmpty(errorText);
                    }
                };

        private static final Func1<CharSequence, CharSequence> VALIDATE_NOT_EMPTY =
                new Func1<CharSequence, CharSequence>() {
                    @Override
                    public CharSequence call(CharSequence inputText) {
                        return isEmpty(inputText) ? "required" : null;
                    }
                };

    }

}
