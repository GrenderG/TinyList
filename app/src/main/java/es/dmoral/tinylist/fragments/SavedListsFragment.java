package es.dmoral.tinylist.fragments;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.dmoral.tinylist.R;
import es.dmoral.tinylist.adapters.ItemListAdapter;
import es.dmoral.tinylist.adapters.SavedListsAdapter;
import es.dmoral.tinylist.helpers.TinyListSQLHelper;
import es.dmoral.tinylist.models.Task;
import es.dmoral.tinylist.models.TaskList;

public class SavedListsFragment extends Fragment {

    @Bind(R.id.saved_list_recycler_view) RecyclerView mRecyclerView;
    private static SavedListsFragment fragmentInstance;
    private RecyclerView.Adapter mAdapter;

    public SavedListsFragment() {
        // Required empty public constructor
    }

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

    private void setupView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new SavedListsAdapter(TinyListSQLHelper.getSqlHelper(getActivity()).getTaskLists(false), getActivity());
        mRecyclerView.setAdapter(mAdapter);
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final TaskList taskListToArchive = ((SavedListsAdapter) mRecyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
                taskListToArchive.setIsArchived(true);
                TinyListSQLHelper.getSqlHelper(getActivity()).addOrUpdateTaskList(taskListToArchive);
                ((SavedListsAdapter) mRecyclerView.getAdapter()).removeItem(viewHolder.getAdapterPosition());
                undoSnackbar();
            }
        };
        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(mRecyclerView);
    }

    public void redrawItems() {
        ((SavedListsAdapter) mRecyclerView.getAdapter()).replaceWith(TinyListSQLHelper.getSqlHelper(getActivity()).getTaskLists(false));
    }

    public void undoSnackbar() {
        Snackbar snackbar = Snackbar
                .make(this.mRecyclerView, R.string.list_archived, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final TaskList taskListToArchive = ((SavedListsAdapter) mRecyclerView.getAdapter()).getCachedItem();
                        taskListToArchive.setIsArchived(false);
                        TinyListSQLHelper.getSqlHelper(getActivity()).addOrUpdateTaskList(taskListToArchive);
                        redrawItems();
                        ArchivedListsFragment.getInstance().redrawItems();
                    }
                });

        snackbar.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        redrawItems();
    }

}
