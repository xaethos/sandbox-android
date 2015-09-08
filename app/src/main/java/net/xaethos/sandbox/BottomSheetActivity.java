package net.xaethos.sandbox;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import net.xaethos.sandbox.views.BottomSheetView;

public class BottomSheetActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_sheet);

        final BottomSheetView sheetView = (BottomSheetView) findViewById(R.id.modal_background);
        sheetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.btn_fill).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sheetView.fill();
            }
        });

        findViewById(R.id.btn_wrap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sheetView.wrap();
            }
        });

        findViewById(R.id.btn_dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sheetView.hide();
            }
        });
    }

}
