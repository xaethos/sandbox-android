package net.xaethos.sandbox;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import net.xaethos.sandbox.fragments.ExpandTextFragment;
import net.xaethos.sandbox.fragments.LoaderAdapterFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout mDrawerLayout;
    Toolbar mToolbar;
    NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        setSupportActionBar(mToolbar);

        mNavigationView = (NavigationView) findViewById(R.id.navigation);
        mNavigationView.setNavigationItemSelectedListener(this);

        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentById(R.id.navigation_content) == null) {
            mToolbar.setTitle(R.string.sandbox_expand_text);
            manager.beginTransaction()
                    .add(R.id.navigation_content, new ExpandTextFragment())
                    .commit();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
        case R.id.nav_expand_text:
        case R.id.nav_loader_adapter:
            return navigateToFragment(menuItem);
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private boolean navigateToFragment(MenuItem menuItem) {
        Fragment fragment;

        switch (menuItem.getItemId()) {
        case R.id.nav_expand_text:
            fragment = new ExpandTextFragment();
            break;

        case R.id.nav_loader_adapter:
            fragment = new LoaderAdapterFragment();
            break;

        default:
            return false;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.navigation_content, fragment)
                .commit();

        menuItem.setChecked(true);
        mToolbar.setTitle(menuItem.getTitle());
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

}
