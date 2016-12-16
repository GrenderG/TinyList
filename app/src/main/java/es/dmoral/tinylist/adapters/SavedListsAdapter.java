package es.dmoral.tinylist.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
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
import es.dmoral.tinylist.activities.EditListActivity;
import es.dmoral.tinylist.activities.MainActivity;
import es.dmoral.tinylist.fragments.SavedListsFragment;
import es.dmoral.tinylist.helpers.TinyListSQLHelper;
import es.dmoral.tinylist.models.Task;
import es.dmoral.tinylist.models.TaskList;

/**
 * Created by grend on 13/01/2016.
 */
public class SavedListsAdapter extends RecyclerView.Adapter<SavedListsAdapter.ViewHolder> {

    private final ArrayList<TaskList> taskLists;
    private final Context context;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    /* Cached item to restore (if user wants to), used when an item is removed. */
    private TaskList cachedItem;

    public SavedListsAdapter(ArrayList<TaskList> taskLists, Context context) {
        this.taskLists = taskLists;
        this.context = context;
    }

    @Override
    public SavedListsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SavedListsAdapter.ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.saved_list_cardview, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.cardView.setCardBackgroundColor(taskLists.get(holder.getAdapterPosition()).getBackgroundColor());
        holder.taskListTitle.setVisibility(View.VISIBLE);
        if (taskLists.get(holder.getAdapterPosition()).getTitle().isEmpty())
            holder.taskListTitle.setVisibility(View.GONE);
        else
            holder.taskListTitle.setText(taskLists.get(holder.getAdapterPosition()).getTitle());

        holder.taskContainer.removeAllViews();
        for (Task task : taskLists.get(holder.getAdapterPosition()).getTasks()) {
            TextView taskDescription = new TextView(context);
            taskDescription.setText(task.getTask());
            taskDescription.setTextSize(18f);
            if (task.isChecked()) {
                taskDescription.setPaintFlags(taskDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                taskDescription.setAlpha(0.3f);
            }
            holder.taskContainer.addView(taskDescription);
        }
        holder.taskListDate.setText(dateFormat.format(taskLists.get(holder.getAdapterPosition()).getCreationDate()));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditListActivity.class);
                intent.putExtra(EditListActivity.INTENT_EDIT, taskLists.get(holder.getAdapterPosition()));
                context.startActivity(intent);
            }
        });
        holder.archiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TaskList taskListToArchive = taskLists.get(holder.getAdapterPosition());
                taskListToArchive.setIsArchived(true);
                TinyListSQLHelper.getSqlHelper(context).addOrUpdateTaskList(taskListToArchive);
                removeItem(holder.getAdapterPosition());
                ((SavedListsFragment) ((MainActivity) context).getCurrentVisibleFragment()).undoSnackbar();
            }
        });
        holder.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String shareIntentText = "";
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntentText += taskLists.get(holder.getAdapterPosition()).getTitle() + "\n\n";
                for (Task task : taskLists.get(holder.getAdapterPosition()).getTasks()) {
                    shareIntentText += (task.isChecked() ? Task.DONE_TASK_MARK : Task.UNDONE_TASK_MARK)
                            + " " + task.getTask() + "\n";
                }
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareIntentText.trim());
                context.startActivity(shareIntent);
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
        this.cachedItem = this.taskLists.get(position);
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
     * Get item at the desired position.
     *
     * @param position desired position
     * @return item at the desired position.
     */
    public TaskList getItem(int position) {
        return this.taskLists.get(position);
    }

    /**
     * This method returns the cached item (see its description to know more)
     *
     * @return cached item.
     */
    public TaskList getCachedItem() {
        return this.cachedItem;
    }

    /**
     * Sets the cached item (see its description to know more)
     *
     * @param item cached item.
     */
    public void setCachedItem(TaskList item) {
        this.cachedItem = item;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.task_list_title)
        TextView taskListTitle;
        @Bind(R.id.task_container)
        LinearLayout taskContainer;
        @Bind(R.id.task_list_date)
        TextView taskListDate;
        @Bind(R.id.card_view_saved_lists)
        CardView cardView;
        @Bind(R.id.archive)
        ImageView archiveButton;
        @Bind(R.id.share)
        ImageView shareButton;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
