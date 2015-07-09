package net.xaethos.sandbox;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import net.xaethos.sandbox.expand.ExpandTextActivity;
import net.xaethos.sandbox.singletonloader.SingletonLoaderActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_loaderadapter).setOnClickListener(this);
        findViewById(R.id.btn_singleton_loader).setOnClickListener(this);
        findViewById(R.id.btn_expand_text).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_loaderadapter:
                startActivity(new Intent(this, LoaderAdapterActivity.class));
                break;
            case R.id.btn_expand_text:
                startActivity(new Intent(this, ExpandTextActivity.class));
                break;
            case R.id.btn_singleton_loader:
                startActivity(new Intent(this, SingletonLoaderActivity.class));
                break;
        }
    }
}
