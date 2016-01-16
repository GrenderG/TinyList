package es.dmoral.tinylist.activities;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.larswerkman.lobsterpicker.adapters.BitmapColorAdapter;
import com.larswerkman.lobsterpicker.sliders.LobsterShadeSlider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import es.dmoral.tinylist.R;
import es.dmoral.tinylist.adapters.ItemListAdapter;
import es.dmoral.tinylist.adapters.SavedListsAdapter;
import es.dmoral.tinylist.helpers.TinyListSQLHelper;
import es.dmoral.tinylist.models.Task;
import es.dmoral.tinylist.models.TaskList;

public class EditListActivity extends AppCompatActivity {

    @Bind(R.id.main_layout) LinearLayout mainLayout;
    @Bind(R.id.et_task_list_title) EditText etTaskTitle;
    @Bind(R.id.list_item_recyclerview) RecyclerView listItemRecyclerview;
    @Bind(R.id.add_item) Button addItem;
    @Bind(R.id.main_container) LinearLayout mainContainer;

    public static final String INTENT_EDIT = "INTENT_EDIT";
    private int selectedColor;
    private RecyclerView.Adapter mAdapter;
    private TaskList editingTaskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_list);

        if (getIntent().getExtras() != null) {
            this.editingTaskList = (TaskList) getIntent().getSerializableExtra(INTENT_EDIT);
        }

        setupViews();
        setListeners();
    }

    private void setupViews() {
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.new_shopping_list);

        this.listItemRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        if (this.editingTaskList == null) {
            this.mAdapter = new ItemListAdapter();
            this.listItemRecyclerview.setAdapter(this.mAdapter);
            this.selectedColor = Color.parseColor("#FFFFFF");
        } else {
            this.mAdapter = new ItemListAdapter(editingTaskList.getTasks());
            this.listItemRecyclerview.setAdapter(this.mAdapter);
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
                getSupportActionBar().setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        this.addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ItemListAdapter) listItemRecyclerview.getAdapter())
                        .addItem(new Task());
                listItemRecyclerview.scrollToPosition(listItemRecyclerview.getAdapter().getItemCount() - 1);
            }
        });

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                ((ItemListAdapter) listItemRecyclerview.getAdapter()).removeItem(viewHolder.getAdapterPosition());
            }
        };
        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(listItemRecyclerview);
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

    private void handlePaletteAction() {
        final Dialog dialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.color_picker_layout, null);
        dialog.setContentView(view);
        dialog.setTitle(R.string.choose_color);
        dialog.setCancelable(false);

        final LobsterShadeSlider shadeSlider = (LobsterShadeSlider) view.findViewById(R.id.shadeslider);
        shadeSlider.setColorAdapter(new BitmapColorAdapter(this, R.drawable.color_palette));

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        view.findViewById(R.id.accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor = shadeSlider.getColor();
                mainLayout.setBackgroundColor(selectedColor);
                dialog.cancel();
            }
        });

        dialog.show();
    }

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
