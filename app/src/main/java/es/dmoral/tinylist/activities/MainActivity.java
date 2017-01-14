package es.dmoral.tinylist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.dmoral.tinylist.R;
import es.dmoral.tinylist.fragments.ArchivedListsFragment;
import es.dmoral.tinylist.fragments.BaseFragment;
import es.dmoral.tinylist.fragments.SavedListsFragment;
import es.dmoral.tinylist.helpers.TinyListSQLHelper;
import es.dmoral.tinylist.models.Task;
import es.dmoral.tinylist.models.TaskList;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.container)
    ViewPager viewPagerContainer;
    @Bind(R.id.fab)
    FloatingActionButton fab;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.tabs)
    TabLayout tabLayout;
    @Bind(R.id.appbar)
    AppBarLayout appBarLayout;

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

        viewPagerContainer.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));

        tabLayout.setupWithViewPager(viewPagerContainer);

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
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset == 0) { //expanded
                    fab.show();
                } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) { //collapsed
                    fab.hide();
                }
            }
        });
        viewPagerContainer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
                    try {
                        ((SectionsPagerAdapter) viewPagerContainer.getAdapter())
                                .getRegisteredFragment(viewPagerContainer.getCurrentItem()).redrawItems();
                    } catch (NullPointerException ignored) {
                    }
                    fab.show();
                }
            }
        });
    }

    public BaseFragment getCurrentVisibleFragment() {
        return (((SectionsPagerAdapter)
                viewPagerContainer.getAdapter()).getRegisteredFragment(viewPagerContainer.getCurrentItem()));
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
            case R.id.action_import:
                showImportListDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Just info about the app :-)
     */
    private void showAboutDialog() {
        final SpannableString spannableString = new SpannableString(getString(R.string.about_msg));
        Linkify.addLinks(spannableString, Linkify.ALL);

        new MaterialDialog.Builder(this)
                .positiveText(android.R.string.ok)
                .title(getString(R.string.app_name) + " " + getString(R.string.app_version))
                .content(spannableString)
                .show();
    }

    /**
     * Prompt to import a list.
     */
    private void showImportListDialog() {
        final MaterialDialog importDialog = new MaterialDialog.Builder(this)
                .title(getString(R.string.action_import))
                .content(R.string.import_list_msg)
                .negativeText(android.R.string.cancel)
                .inputRange(1, -1)
                .input(getString(R.string.import_list_hint), null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        importList(input.toString());
                        if (viewPagerContainer.getCurrentItem() == 0)
                            ((SectionsPagerAdapter)
                                    viewPagerContainer.getAdapter()).getRegisteredFragment(0).redrawItems();
                    }
                }).build();
        importDialog.getInputEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
        importDialog.getInputEditText().setSingleLine(false);
        importDialog.show();
    }

    private void importList(String listToImport) {
        TaskList taskListToImport = new TaskList();
        ArrayList<Task> tasksToImport = new ArrayList<>();
        final String[] lines = listToImport.split("[\r\n]+");
        for (int i = 0; i < lines.length; i++) {
            if (i == 0)
                if (!lines[0].contains(Task.DONE_TASK_MARK)
                        && !lines[0].contains(Task.UNDONE_TASK_MARK)) {
                    taskListToImport.setTitle(lines[0].trim());
                    continue;
                }
            Task taskToImport = new Task();
            taskToImport.setIsChecked(lines[i].contains(Task.DONE_TASK_MARK));
            lines[i] = lines[i].replace(Task.DONE_TASK_MARK, "");
            lines[i] = lines[i].replace(Task.UNDONE_TASK_MARK, "");
            taskToImport.setTask(lines[i].trim());
            tasksToImport.add(taskToImport);

        }
        taskListToImport.setTasks(tasksToImport);
        TinyListSQLHelper.getSqlHelper(this).addOrUpdateTaskList(taskListToImport);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private SparseArray<BaseFragment> registeredFragments = new SparseArray<>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            /* getItem is called to instantiate the fragment for the given page. */
            switch (position) {
                case 0:
                    SavedListsFragment savedListsFragment = SavedListsFragment.getInstance();
                    registeredFragments.put(0, savedListsFragment);
                    return savedListsFragment;
                case 1:
                    ArchivedListsFragment archivedListsFragment = ArchivedListsFragment.getInstance();
                    registeredFragments.put(1, archivedListsFragment);
                    return archivedListsFragment;
                default:
                    return new Fragment();
            }

        }

        @Override
        public int getCount() {
            // Show 2 total pages (Saved lists and Archived lists).
            return 2;
        }

        public BaseFragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
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
