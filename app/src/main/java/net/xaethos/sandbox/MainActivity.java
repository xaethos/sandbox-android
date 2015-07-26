package net.xaethos.sandbox;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new ActivitiesPreferenceFragment())
                .commit();
    }

    public static List<Preference> getChildIntentPreferences(Context context) {
        ArrayList<Preference> preferences = new ArrayList<>();

        PackageManager packageManager = context.getPackageManager();
        Intent packageIntent = new Intent(Intent.ACTION_MAIN);
        packageIntent.addCategory("net.xaethos.sandbox.LAUNCHER");

        List<ResolveInfo> resolution = packageManager.queryIntentActivities(packageIntent, 0);
        for (ResolveInfo info : resolution) {
            Intent childIntent = new Intent();
            childIntent.setClassName(context, info.activityInfo.name);

            Preference preference = new Preference(context);
            preference.setTitle(info.activityInfo.labelRes);
            preference.setIntent(childIntent);
            preferences.add(preference);
        }

        return preferences;
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class ActivitiesPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());
            setPreferenceScreen(screen);
            for (Preference preference : getChildIntentPreferences(getActivity())) {
                screen.addPreference(preference);
            }
        }
    }

}
