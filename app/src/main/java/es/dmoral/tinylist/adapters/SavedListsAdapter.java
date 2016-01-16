package es.dmoral.tinylist.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
    private ActionMode actionMode;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
        holder.cardView.setCardBackgroundColor(taskLists.get(position).getBackgroundColor());
        holder.taskListTitle.setText(taskLists.get(position).getTitle());

        holder.taskContainer.removeAllViews();
        for (Task task : taskLists.get(position).getTasks()) {
            TextView taskDescription = new TextView(context);
            taskDescription.setText(task.getTask());
            taskDescription.setTextSize(18f);
            if (task.isChecked())
                taskDescription.setPaintFlags(taskDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.taskContainer.addView(taskDescription);
        }
        holder.taskListDate.setText(dateFormat.format(taskLists.get(position).getCreationDate()));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditListActivity.class);
                intent.putExtra(EditListActivity.INTENT_EDIT, taskLists.get(position));
                context.startActivity(intent);
            }
        });
        holder.archiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TaskList taskListToArchive = taskLists.get(position);
                taskListToArchive.setIsArchived(true);
                TinyListSQLHelper.getSqlHelper(context).addOrUpdateTaskList(taskListToArchive);
                removeItem(position);
                SavedListsFragment.getInstance().undoSnackbar();
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.taskLists.size();
    }

    public void addItem(TaskList taskList) {
        this.taskLists.add(taskList);
        notifyItemInserted(taskLists.size() - 1);
    }

    public void addItem(TaskList taskList, int position) {
        this.taskLists.add(position, taskList);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        this.cachedItem = this.taskLists.get(position);
        this.taskLists.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount() - position);
    }

    public void replaceWith(ArrayList<TaskList> newTaskLists) {
        this.taskLists.clear();
        this.taskLists.addAll(newTaskLists);
        notifyDataSetChanged();
    }

    public TaskList getItem(int position) {
        return this.taskLists.get(position);
    }

    public TaskList getCachedItem() {
        return this.cachedItem;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.task_list_title) TextView taskListTitle;
        @Bind(R.id.task_container) LinearLayout taskContainer;
        @Bind(R.id.task_list_date) TextView taskListDate;
        @Bind(R.id.card_view_saved_lists) CardView cardView;
        @Bind(R.id.archive) ImageView archiveButton;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
