package es.dmoral.tinylist.fragments;


import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.dmoral.tinylist.R;
import es.dmoral.tinylist.adapters.SavedListsAdapter;
import es.dmoral.tinylist.helpers.TinyListSQLHelper;
import es.dmoral.tinylist.models.TaskList;
import es.dmoral.tinylist.widgets.FABScrollBehavior;

public class SavedListsFragment extends Fragment {

    @Bind(R.id.saved_list_recycler_view) RecyclerView mRecyclerView;
    private static SavedListsFragment fragmentInstance;

    public SavedListsFragment() {
        // Required empty public constructor
    }

    /**
     * Using Singleton pattern (Android best practices)
     *
     * @return an instance of the fragment
     */
    public static SavedListsFragment getInstance() {
        if (fragmentInstance == null)
            fragmentInstance = new SavedListsFragment();
        return fragmentInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_saved_lists, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupView();
    }

    /**
     * Method used to set up the entire view, here we check if the user is adding
     * a new TaskList or editing an existing one.
     */
    private void setupView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        RecyclerView.Adapter mAdapter = new SavedListsAdapter(TinyListSQLHelper.getSqlHelper(getActivity()).getTaskLists(false), getActivity());
        mRecyclerView.setAdapter(mAdapter);
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                /* Archiving the swiped item and showing an Snackbar with "UNDO" option. */
                final TaskList taskListToArchive = ((SavedListsAdapter) mRecyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
                taskListToArchive.setIsArchived(true);
                TinyListSQLHelper.getSqlHelper(getActivity()).addOrUpdateTaskList(taskListToArchive);
                ((SavedListsAdapter) mRecyclerView.getAdapter()).removeItem(viewHolder.getAdapterPosition());
                undoSnackbar();
            }
        };
        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(mRecyclerView);
    }

    /**
     * Method used to redraw the items inside the main RecyclerView
     */
    public void redrawItems() {
        ((SavedListsAdapter) mRecyclerView.getAdapter()).replaceWith(TinyListSQLHelper.getSqlHelper(getActivity()).getTaskLists(false));
    }

    /**
     * This method shows an Snackbar informing the item has been archived and giving the user the
     * option to revert that.
     */
    public void undoSnackbar() {
        Snackbar undoSnackbar = Snackbar
                .make(this.mRecyclerView, R.string.list_archived, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (((SavedListsAdapter) mRecyclerView.getAdapter()).getCachedItem() != null) {
                            final TaskList taskListToArchive = ((SavedListsAdapter) mRecyclerView.getAdapter()).getCachedItem();
                            taskListToArchive.setIsArchived(false);
                            TinyListSQLHelper.getSqlHelper(getActivity()).addOrUpdateTaskList(taskListToArchive);
                            redrawItems();
                            ArchivedListsFragment.getInstance().redrawItems();
                        }
                    }
                })
                /* Controlling if the FAB can hide or not to avoid weird behaviours
                * with its final position after hiding while the Snackbar is still
                * showing. */
                .setCallback(new Snackbar.Callback() {
                    public void onShown(Snackbar snackbar) {
                        super.onShown(snackbar);
                        FABScrollBehavior.setCanHideChild(false);
                    }

                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        super.onDismissed(snackbar, event);
                        FABScrollBehavior.setCanHideChild(true);
                    }
                });

        undoSnackbar.show();
    }

    /**
     * Clears the cached item, this method is called when the user deletes an archived item.
     * (to prevent the UNDO of that item).
     */
    public void clearCachedItem() {
        ((SavedListsAdapter) this.mRecyclerView.getAdapter()).setCachedItem(null);
    }

    @Override
    public void onStart() {
        super.onStart();
        /* Redraw items when this fragment is first viewed by the user. */
        redrawItems();
    }

}
