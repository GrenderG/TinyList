package es.dmoral.tinylist.activities;

import android.content.Intent;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.dmoral.tinylist.R;
import es.dmoral.tinylist.fragments.ArchivedListsFragment;
import es.dmoral.tinylist.fragments.SavedListsFragment;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.container) ViewPager mViewPager;
    @Bind(R.id.fab) FloatingActionButton fab;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.tabs) TabLayout tabLayout;
    @Bind(R.id.appbar) AppBarLayout appBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* Changing to main theme (avoiding cold start).
        Thanks to "https://github.com/DreaminginCodeZH/MaterialColdStart" */
        setTheme(R.style.AppTheme_NoActionBar);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupView();
        setListeners();

    }

    /**
     * Method used to set up the entire view, here we check if the user is adding
     * a new TaskList or editing an existing one.
     */
    private void setupView() {
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);

        mViewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));

        tabLayout.setupWithViewPager(mViewPager);

    }

    private void setListeners() {
        /* FAB handling the main action, adding a new TaskList */
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditListActivity.class);
                startActivity(intent);
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                appBarLayout.setExpanded(true, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                /* If the user starts scrolling or dragging the ViewPager, the adapters will redraw
                * themselves in order to show recent changes. */
                if (state != ViewPager.SCROLL_STATE_IDLE) {
                    SavedListsFragment.getInstance().redrawItems();
                    ArchivedListsFragment.getInstance().redrawItems();
                    fab.show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_about:
                showAboutDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Just info about the app :-)
     */
    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setPositiveButton(R.string.accept, null)
                .setTitle(getString(R.string.app_name) + " " + getString(R.string.app_version))
                .setMessage(getString(R.string.about_msg))
                .create().show();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            /* getItem is called to instantiate the fragment for the given page. */
            switch (position){
                case 0:
                    return SavedListsFragment.getInstance();
                case 1:
                    return ArchivedListsFragment.getInstance();
                default:
                    return new Fragment();
            }

        }

        @Override
        public int getCount() {
            // Show 2 total pages (Saved lists and Archived lists).
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.saved_lists);
                case 1:
                    return getString(R.string.archived_lists);
            }
            return null;
        }

    }
}
