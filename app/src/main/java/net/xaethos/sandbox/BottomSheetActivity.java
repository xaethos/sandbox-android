package net.xaethos.sandbox;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;

import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.OnSheetDismissedListener;

public class BottomSheetActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bottom_sheet_2);

//        BottomSheetLayout bottomSheetLayout = new BottomSheetLayout(this);
        final BottomSheetLayout bottomSheetLayout =
                (BottomSheetLayout) findViewById(R.id.bottomsheet);

//        setContentView(bottomSheetLayout);
        bottomSheetLayout.showWithSheetView(LayoutInflater.from(this)
                        .inflate(R.layout.bottom_sheet, bottomSheetLayout, false),
                null,
                new OnSheetDismissedListener() {

                    @Override
                    public void onDismissed(BottomSheetLayout bottomSheetLayout) {
                        finish();
                    }
                });

//        setContentView(R.layout.activity_bottom_sheet);

//        final BottomSheetView sheetView = (BottomSheetView) findViewById(R.id.modal_background);
//        sheetView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });

        findViewById(R.id.btn_fill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetLayout.expandSheet();
            }
        });

        findViewById(R.id.btn_wrap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetLayout.peekSheet();
            }
        });

        findViewById(R.id.btn_dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetLayout.dismissSheet();
            }
        });
    }

}
