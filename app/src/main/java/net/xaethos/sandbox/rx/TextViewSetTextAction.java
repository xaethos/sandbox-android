package net.xaethos.sandbox.rx;

import android.widget.TextView;

import rx.functions.Action1;

public class TextViewSetTextAction implements Action1<CharSequence> {
    private final TextView mTextView;

    public TextViewSetTextAction(TextView textView) {
        mTextView = textView;
    }

    @Override
    public void call(CharSequence text) {
        mTextView.setText(text);
    }
}
