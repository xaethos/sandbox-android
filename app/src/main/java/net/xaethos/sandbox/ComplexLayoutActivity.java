package net.xaethos.sandbox;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

public class ComplexLayoutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complex_layout);

        CollapsingToolbarLayout collapsingBar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_bar);
        collapsingBar.setTitle(getTitle());

        Toolbar actionBar = (Toolbar) findViewById(R.id.action_bar);
        actionBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(actionBar);

        SectionsPagerAdapter pagerAdapter =
                new SectionsPagerAdapter(this, getSupportFragmentManager());

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabBar = (TabLayout) findViewById(R.id.tab_bar);
        tabBar.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_complex_layout, menu);
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

    static class SectionsPagerAdapter extends FragmentPagerAdapter {

        final Resources mResources;

        public SectionsPagerAdapter(Context context, FragmentManager fm) {
            super(fm);
            mResources = context.getResources();
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a ScrollingFragment (defined as a static inner class below).
            return ScrollingFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
            case 0:
                return mResources.getString(R.string.title_section1).toUpperCase(l);
            case 1:
                return mResources.getString(R.string.title_section2).toUpperCase(l);
            case 2:
                return mResources.getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    public static class ScrollingFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static ScrollingFragment newInstance(int sectionNumber) {
            ScrollingFragment fragment = new ScrollingFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(
                LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Context context = container.getContext();

            RecyclerView recyclerView = new RecyclerView(context);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new ListAdapter(inflater,
                    getArguments().getInt(ARG_SECTION_NUMBER)));

            return recyclerView;
        }
    }

    static class ListAdapter extends RecyclerView.Adapter<ViewHolder> {
        final LayoutInflater mInflater;
        final int mSectionNumber;

        ListAdapter(LayoutInflater inflater, int sectionNumber) {
            mInflater = inflater;
            mSectionNumber = sectionNumber;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(mInflater.inflate(android.R.layout.simple_list_item_1,
                    parent,
                    false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.textView.setText(String.format("item %d.%d", mSectionNumber, position));
        }

        @Override
        public int getItemCount() {
            return 30;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

}
