package net.xaethos.sandbox;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

public class BottomSheetActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_sheet);

        findViewById(R.id.modal_background).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
