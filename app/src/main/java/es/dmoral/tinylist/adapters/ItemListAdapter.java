package es.dmoral.tinylist.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.dmoral.tinylist.R;
import es.dmoral.tinylist.helpers.TinyListSQLHelper;
import es.dmoral.tinylist.models.Task;

/**
 * Created by grend on 14/01/2016.
 */
public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ViewHolder> {

    private final ArrayList<Task> tasks;
    /* This is the position of the last added item, which needs to be focused. */
    private int focusedItem;
    private Context context;

    public ItemListAdapter(Context context) {
        this.context = context;
        this.tasks = new ArrayList<>();
    }

    public ItemListAdapter(ArrayList<Task> tasks, Context context) {
        this.context = context;
        this.tasks = tasks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemListAdapter.ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_layout, parent, false), new CustomEditTextListener(),
                new CustomCheckboxListener());
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.customEditTextListener.updatePosition(holder.getAdapterPosition());
        holder.customCheckboxListener.updatePosition(holder.getAdapterPosition());
        holder.customCheckboxListener.updatePaintFlags(holder.itemDesc);
        holder.cbItem.setChecked(this.tasks.get(position).isChecked());
        holder.itemDesc.setText(this.tasks.get(position).getTask());
        holder.removeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem(holder.getAdapterPosition());
            }
        });
        if (this.focusedItem == position)
            holder.itemDesc.requestFocus();
    }

    @Override
    public int getItemCount() {
        return this.tasks.size();
    }

    /**
     * This method sets the focusedItem (see its description to know more)
     *
     * @param position position of the focused item.
     */
    public void setFocusedItem(int position) {
        this.focusedItem = position;
    }

    /**
     * Adds a new item to the list.
     *
     * @param task item to add
     */
    public void addItem(Task task) {
        this.tasks.add(task);
        notifyItemInserted(tasks.size() - 1);
    }

    /**
     * This method removes an item and notifies their removal and notifies the change of
     * the views below the removed item.
     *
     * @param position position of the item to remove
     */
    public void removeItem(int position) {
        TinyListSQLHelper.getSqlHelper(context).deleteTask(this.tasks.get(position).getTask_id());
        this.tasks.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount() - position);
    }

    /**
     * This method returns all tasks.
     *
     * @return tasks.
     */
    public ArrayList<Task> getTasks() {
        return this.tasks;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public CustomEditTextListener customEditTextListener;
        public CustomCheckboxListener customCheckboxListener;
        @Bind(R.id.cb_item)
        CheckBox cbItem;
        @Bind(R.id.item_desc)
        EditText itemDesc;
        @Bind(R.id.remove_item)
        ImageView removeItem;

        public ViewHolder(View itemView, CustomEditTextListener customEditTextListener,
                          CustomCheckboxListener customCheckboxListener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.customEditTextListener = customEditTextListener;
            this.itemDesc.addTextChangedListener(customEditTextListener);
            this.customCheckboxListener = customCheckboxListener;
            this.cbItem.setOnCheckedChangeListener(customCheckboxListener);
        }
    }

    /**
     * This class is used to maintain updated the Task's ArrayList as the users changes the information.
     */
    private class CustomEditTextListener implements TextWatcher {

        private int position;

        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            tasks.get(position).setTask(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    /**
     * This class is used to maintain updated the Task's ArrayList as the users changes the information.
     */
    private class CustomCheckboxListener implements CompoundButton.OnCheckedChangeListener {

        private int position;
        private EditText currentEditText;

        public void updatePosition(int position) {
            this.position = position;
        }

        public void updatePaintFlags(EditText currentEditText) {
            this.currentEditText = currentEditText;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            tasks.get(position).setIsChecked(isChecked);

            if (isChecked)
                this.currentEditText.setPaintFlags(this.currentEditText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            else
                this.currentEditText.setPaintFlags(this.currentEditText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

}
