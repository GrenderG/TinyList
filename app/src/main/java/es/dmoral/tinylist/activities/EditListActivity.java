package es.dmoral.tinylist.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.thebluealliance.spectrum.SpectrumDialog;

import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.dmoral.tinylist.R;
import es.dmoral.tinylist.adapters.ItemListAdapter;
import es.dmoral.tinylist.helpers.TinyListSQLHelper;
import es.dmoral.tinylist.models.Task;
import es.dmoral.tinylist.models.TaskList;

public class EditListActivity extends AppCompatActivity {

    /* Intent to handle edit list action. */
    public static final String INTENT_EDIT = "INTENT_EDIT";
    @Bind(R.id.main_layout)
    LinearLayout mainLayout;
    @Bind(R.id.et_task_list_title)
    EditText etTaskTitle;
    @Bind(R.id.list_item_recyclerview)
    RecyclerView listItemRecyclerview;
    @Bind(R.id.add_item)
    Button addItem;
    private int selectedColor;
    private TaskList editingTaskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_list);

        /* Getting the TaskList to edit */
        if (getIntent().getExtras() != null) {
            this.editingTaskList = (TaskList) getIntent().getSerializableExtra(INTENT_EDIT);
        }

        setupViews();
        setListeners();
    }

    /**
     * Method used to set up the entire view, here we check if the user is adding
     * a new TaskList or editing an existing one.
     */
    private void setupViews() {
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.new_shopping_list);

        this.listItemRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.Adapter mAdapter;
        if (this.editingTaskList == null) {
            mAdapter = new ItemListAdapter(this);
            this.listItemRecyclerview.setAdapter(mAdapter);
            this.selectedColor = Color.parseColor("#FFFFFF");
        } else {
            mAdapter = new ItemListAdapter(editingTaskList.getTasks(), this);
            this.listItemRecyclerview.setAdapter(mAdapter);
            this.selectedColor = this.editingTaskList.getBackgroundColor();
            this.etTaskTitle.setText(this.editingTaskList.getTitle());
            this.mainLayout.setBackgroundColor(this.selectedColor);
            getSupportActionBar().setTitle(this.etTaskTitle.getText().toString());
        }

    }

    private void setListeners() {
        this.etTaskTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /* Updating activity title as the user writes TaskList title. */
                getSupportActionBar().setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        this.addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Adding new task, scrolling and focusing it.*/
                if (editingTaskList == null)
                    ((ItemListAdapter) listItemRecyclerview.getAdapter()).addItem(new Task());
                else
                    ((ItemListAdapter) listItemRecyclerview.getAdapter()).addItem(new Task(editingTaskList.getTask_list_id()));
                listItemRecyclerview.scrollToPosition(listItemRecyclerview.getAdapter().getItemCount() - 1);
                ((ItemListAdapter) listItemRecyclerview.getAdapter()).setFocusedItem(listItemRecyclerview.getAdapter().getItemCount() - 1);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_palette:
                handlePaletteAction();
                return true;
            case R.id.action_done:
                saveTaskList();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * When the user touch palette menu icon in order to change background color of the
     * future CardView.
     */
    private void handlePaletteAction() {
        new SpectrumDialog.Builder(this)
                .setColors(R.array.card_colors)
                .setSelectedColor(this.selectedColor)
                .setDismissOnColorSelected(true)
                .setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                    @Override
                    public void onColorSelected(boolean positiveResult, @ColorInt int color) {
                        if (positiveResult) {
                            selectedColor = color;
                            mainLayout.setBackgroundColor(selectedColor);
                        }

                    }
                }).build().show(getSupportFragmentManager(), "SpectrumDialog");
    }

    /**
     * Method created to save a new TaskList or editing an existing one.
     */
    private void saveTaskList() {
        if (this.editingTaskList == null) {
            TinyListSQLHelper.getSqlHelper(this).addOrUpdateTaskList(
                    new TaskList(-1, this.etTaskTitle.getText().toString(), Calendar.getInstance().getTime(),
                            ((ItemListAdapter) listItemRecyclerview.getAdapter()).getTasks(), this.selectedColor, false)
            );
        } else {
            this.editingTaskList.setTasks(((ItemListAdapter) listItemRecyclerview.getAdapter()).getTasks());
            this.editingTaskList.setBackgroundColor(this.selectedColor);
            this.editingTaskList.setCreationDate(Calendar.getInstance().getTime());
            this.editingTaskList.setTitle(this.etTaskTitle.getText().toString());
            TinyListSQLHelper.getSqlHelper(this).addOrUpdateTaskList(this.editingTaskList);
        }

        onBackPressed();
    }

}
