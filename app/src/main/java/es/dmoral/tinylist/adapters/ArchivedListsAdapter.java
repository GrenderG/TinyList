package es.dmoral.tinylist.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.dmoral.tinylist.R;
import es.dmoral.tinylist.fragments.SavedListsFragment;
import es.dmoral.tinylist.helpers.TinyListSQLHelper;
import es.dmoral.tinylist.models.Task;
import es.dmoral.tinylist.models.TaskList;

/**
 * Created by grend on 13/01/2016.
 */
public class ArchivedListsAdapter extends RecyclerView.Adapter<ArchivedListsAdapter.ViewHolder> {

    private final ArrayList<TaskList> taskLists;
    private final Context context;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ArchivedListsAdapter(ArrayList<TaskList> taskLists, Context context) {
        this.taskLists = taskLists;
        this.context = context;
    }

    @Override
    public ArchivedListsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ArchivedListsAdapter.ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.archived_lists_cardview, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.cardView.setCardBackgroundColor(taskLists.get(position).getBackgroundColor());
        holder.taskListTitle.setText(taskLists.get(position).getTitle());

        /* Removing existing views (to prevent weird behaviours as the RecyclerView recycle views,
        * and adding Tasks (List items) to the CardView. */
        holder.taskContainer.removeAllViews();
        for (Task task : taskLists.get(position).getTasks()) {
            TextView taskDescription = new TextView(context);
            taskDescription.setText(task.getTask());
            taskDescription.setTextSize(18f);
            if (task.isChecked()) {
                taskDescription.setPaintFlags(taskDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                taskDescription.setAlpha(0.3f);
            }
            holder.taskContainer.addView(taskDescription);
        }

        holder.taskListDate.setText(dateFormat.format(taskLists.get(position).getCreationDate()));
        holder.unarchiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TaskList taskListToArchive = taskLists.get(position);
                taskListToArchive.setIsArchived(false);
                TinyListSQLHelper.getSqlHelper(context).addOrUpdateTaskList(taskListToArchive);
                removeItem(position);
            }
        });

        /* Handling delete action and ensuring the user really wants to delete that archived list. */
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SavedListsFragment.getInstance().clearCachedItem();
                                TinyListSQLHelper.getSqlHelper(context).deleteTaskList(taskLists.get(position).getTask_list_id());
                                removeItem(position);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .setTitle(context.getString(R.string.delete_title) + taskLists.get(position).getTitle())
                        .setMessage(context.getString(R.string.delete_msg_1) + taskLists.get(position).getTitle() + context.getString(R.string.delete_msg_2))
                        .create().show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.taskLists.size();
    }

    /**
     * This method removes an item and notifies their removal and notifies the change of
     * the views below the removed item.
     *
     * @param position position of the item to remove
     */
    public void removeItem(int position) {
        this.taskLists.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount() - position);
    }

    /**
     * Replaces all items with new ones.
     *
     * @param newTaskLists items to replace.
     */
    public void replaceWith(ArrayList<TaskList> newTaskLists) {
        this.taskLists.clear();
        this.taskLists.addAll(newTaskLists);
        notifyDataSetChanged();
    }

    /**
     * This method returns the item at the desired position.
     *
     * @param position position of the item to retrieve
     * @return retrieved item
     */
    public TaskList getItem(int position) {
        return this.taskLists.get(position);
    }

    /**
     * Using ViewHolder pattern (combined with ButterKnife library
     * as in the rest of the app (http://jakewharton.github.io/butterknife/))
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.task_archived_list_title) TextView taskListTitle;
        @Bind(R.id.task_archived_container) LinearLayout taskContainer;
        @Bind(R.id.task_archived_list_date) TextView taskListDate;
        @Bind(R.id.card_view_archived_lists) CardView cardView;
        @Bind(R.id.unarchive) ImageView unarchiveButton;
        @Bind(R.id.delete) ImageView deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
