package es.dmoral.tinylist.adapters;

import android.graphics.Paint;
import android.os.SystemClock;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.dmoral.tinylist.R;
import es.dmoral.tinylist.models.Task;

/**
 * Created by grend on 14/01/2016.
 */
public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ViewHolder> {

    private final ArrayList<Task> tasks;

    public ItemListAdapter() {
        this.tasks = new ArrayList<>();
    }

    public ItemListAdapter(ArrayList<Task> tasks) {
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
        holder.customEditTextListener.updatePosition(position);
        holder.customCheckboxListener.updatePosition(position);
        holder.customCheckboxListener.updatePaintFlags(holder.itemDesc);
        holder.cbItem.setChecked(this.tasks.get(position).isChecked());
        holder.itemDesc.setText(this.tasks.get(position).getTask());
    }

    @Override
    public int getItemCount() {
        return this.tasks.size();
    }

    public void addItem(Task task) {
        this.tasks.add(task);
        notifyItemInserted(tasks.size() - 1);
    }

    public void addItem(Task task, int position) {
        this.tasks.add(position, task);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        this.tasks.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount() - position);
    }

    public void replaceWith(ArrayList<Task> newTasks) {
        this.tasks.clear();
        this.tasks.addAll(newTasks);
        notifyDataSetChanged();
    }

    public ArrayList<Task> getTasks() {
        return this.tasks;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.cb_item) CheckBox cbItem;
        @Bind(R.id.item_desc) EditText itemDesc;
        public CustomEditTextListener customEditTextListener;
        public CustomCheckboxListener customCheckboxListener;

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
            if (position == tasks.size())
                notifyDataSetChanged();
            tasks.get(position).setTask(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

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
            /*if (position == tasks.size())
                notifyDataSetChanged();*/
            tasks.get(position).setIsChecked(isChecked);

            if (isChecked)
                this.currentEditText.setPaintFlags(this.currentEditText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            else
                this.currentEditText.setPaintFlags(this.currentEditText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

}
