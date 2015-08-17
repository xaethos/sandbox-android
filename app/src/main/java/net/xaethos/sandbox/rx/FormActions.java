package net.xaethos.sandbox.rx;

import android.support.design.widget.TextInputLayout;
import android.view.View;

import rx.functions.Action1;

public class FormActions {

    private FormActions() {
    }

    public static Action1<CharSequence> setTextInputError(final TextInputLayout textInputLayout) {
        return new Action1<CharSequence>() {
            @Override
            public void call(CharSequence errorText) {
                textInputLayout.setError(errorText);
            }
        };
    }

    public static Action1<Boolean> setViewEnabled(final View view) {
        return new Action1<Boolean>() {
            @Override
            public void call(Boolean enabled) {
                view.setEnabled(enabled);
            }
        };
    }

}
