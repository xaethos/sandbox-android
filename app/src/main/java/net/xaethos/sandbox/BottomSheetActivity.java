package net.xaethos.sandbox;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import net.xaethos.sandbox.views.BottomSheetView;

public class BottomSheetActivity extends FragmentActivity implements View.OnClickListener {

    private static final int[] COUNTS = {1, 2, 6, 10};
    private int countIndex = 0;

    private ViewGroup mLipsumContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bottom_sheet);

        final BottomSheetView bottomSheetView = (BottomSheetView) findViewById(R.id.bottomsheet);
        bottomSheetView.setOnSheetStateChangeListener(new BottomSheetView
                .OnSheetStateChangeListener() {
            @Override
            public void onSheetStateChanged(BottomSheetView.State state) {
                Toast.makeText(BottomSheetActivity.this, state.toString(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
        bottomSheetView.setOnDismissedListener(new BottomSheetView.OnDismissedListener() {
            @Override
            public void onDismissed(BottomSheetView bottomSheetView) {
                finish();
            }
        });
        bottomSheetView.show();

        mLipsumContainer =
                (ViewGroup) bottomSheetView.getContentView().findViewById(R.id.container_lipsum);

        findViewById(android.R.id.button1).setOnClickListener(this);
        findViewById(android.R.id.button2).setOnClickListener(this);

        changeCount();
    }

    @Override
    public void onClick(View v) {
        changeCount();
    }

    private void changeCount() {
        final int count = COUNTS[countIndex++ % COUNTS.length];

        mLipsumContainer.removeAllViews();
        for (int i = 0; i < count; ++i) {
            TextView lipsum = new TextView(this);
            lipsum.setText(R.string.lipsum);
            mLipsumContainer.addView(lipsum);
        }
    }
}
