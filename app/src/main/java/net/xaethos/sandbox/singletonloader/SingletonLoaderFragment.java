package net.xaethos.sandbox.singletonloader;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.xaethos.sandbox.R;

public class SingletonLoaderFragment extends Fragment implements View.OnClickListener,
        LoaderManager.LoaderCallbacks<String> {

    private TextView mTextView;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_singleton_loader, container, false);
        view.findViewById(R.id.fragment_button).setOnClickListener(this);
        mTextView = (TextView) view.findViewById(R.id.activity_text);
        return view;
    }

    @Override
    public void onClick(View v) {
        getLoaderManager().initLoader(1, null, this);
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return SingletonLoader.getInstance(getActivity());
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
