package net.xaethos.sandbox.expand;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import net.xaethos.sandbox.R;

public class ExpandTextActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expand_text);
    }

    @Override
    protected void onResume() {
        super.onResume();

        View text = findViewById(android.R.id.text1);
        int height = text.getMeasuredHeight();
        Log.d("XAE", "Height is " + height);

        if (height < 700) findViewById(android.R.id.button1).setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_expand_text, menu);
        return true;
    }

    private boolean expanded = true;

    public void toggle(View button) {
        View content = findViewById(R.id.content);
        ViewGroup.LayoutParams params = content.getLayoutParams();
        params.height = expanded ? 700 : ViewGroup.LayoutParams.WRAP_CONTENT;
        expanded = !expanded;

        content.setLayoutParams(params);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
