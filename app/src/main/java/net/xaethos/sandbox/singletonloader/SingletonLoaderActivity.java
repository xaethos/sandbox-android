package net.xaethos.sandbox.singletonloader;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.xaethos.sandbox.R;

public class SingletonLoaderActivity extends ActionBarActivity
        implements LoaderManager.LoaderCallbacks<String> {

    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singleton_loader);
        mTextView = (TextView) findViewById(R.id.activity_text);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_singleton_loader, menu);
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

    public void onInitLoader(View view) {
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return SingletonLoader.getInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        mTextView.setText(data);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        mTextView.setText("reset!");
    }
}
