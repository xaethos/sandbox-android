package net.xaethos.sandbox.expand;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.xaethos.sandbox.R;

public class ExpandTextFragment extends Fragment {

    private View mExpandTextView;
    private View mExpandableContent;
    private View mExpandButton;
    private boolean mExpanded = true;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_expand_text, container, false);

        mExpandableContent = view.findViewById(R.id.content);
        mExpandTextView = view.findViewById(android.R.id.text1);
        mExpandButton = view.findViewById(android.R.id.button1);
        mExpandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View button) {
                ViewGroup.LayoutParams params = mExpandableContent.getLayoutParams();
                params.height = mExpanded ? 700 : ViewGroup.LayoutParams.WRAP_CONTENT;
                mExpanded = !mExpanded;

                mExpandableContent.setLayoutParams(params);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        int height = mExpandTextView.getMeasuredHeight();
        Log.d("XAE", "Height is " + height);

        if (height < 700) mExpandButton.setVisibility(View.GONE);
    }

}
