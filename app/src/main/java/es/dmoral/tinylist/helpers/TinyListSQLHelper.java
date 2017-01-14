package es.dmoral.tinylist.helpers;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import es.dmoral.tinylist.models.Task;
import es.dmoral.tinylist.models.TaskList;

/**
 * Created by grend on 13/01/2016.
 * <p>
 * This class is a set of useful database actions, threads or AsyncTask are not used
 * because of the small size of the database and small complexity of the operations.
 */
public class TinyListSQLHelper extends SQLiteOpenHelper {

    private static final String TABLE_NAME_TASK_LISTS = "task_lists";
    private static final String TABLE_NAME_TASKS = "tasks";
    private static final String CREATE_TABLE_TASK_LISTS = "CREATE TABLE task_lists " +
            "(task_list_id INTEGER PRIMARY KEY, " +
            "title VARCHAR, " +
            "background_color INTEGER, " +
            "is_archived, " + // 0 or 1
            "creation_date DATE)";
    private static final String CREATE_TABLE_TASKS = "CREATE TABLE tasks " +
            "(task_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "task VARCHAR, " +
            "is_checked INTEGER," + // 0 or 1
            "task_list_id INTEGER," +
            "FOREIGN KEY(task_list_id) REFERENCES task_lists(task_list_id))";
    private static TinyListSQLHelper sqlHelper = null;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    /*Avoiding instantiation of this class*/
    private TinyListSQLHelper(Context context) {
        super(context, "tinylistdb.sqlite", null, 1);
    }

    /* Using Singleton pattern*/
    public static TinyListSQLHelper getSqlHelper(Context context) {
        if (sqlHelper == null)
            sqlHelper = new TinyListSQLHelper(context);
        return sqlHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_TASK_LISTS);
        db.execSQL(CREATE_TABLE_TASKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * This method adds or updates a TaskList (depending of this id, a new TaskList will
     * always have -1 as it's id.
     *
     * @param taskList TaskList to add or update.
     */
    public void addOrUpdateTaskList(TaskList taskList) {
        SQLiteDatabase db = sqlHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", taskList.getTitle());
        values.put("background_color", taskList.getBackgroundColor());
        values.put("is_archived", taskList.isArchived() ? 1 : 0);
        values.put("creation_date", dateFormat.format(taskList.getCreationDate()));

        for (Task task : taskList.getTasks()) {
            addOrUpdateTask(task, db);
        }

        if (taskList.getTask_list_id() == -1) {
            values.put("task_list_id", getNextUniqueId(db));
            db.insert(TABLE_NAME_TASK_LISTS, null, values);
        } else {
            db.update(TABLE_NAME_TASK_LISTS, values, "task_list_id = ?",
                    new String[]{String.valueOf(taskList.getTask_list_id())});
        }

        db.close();

    }

    /**
     * This method adds or updates a TaskList (depending of this id, a new TaskList will
     * always have -1 as it's id.
     *
     * @param task Task to add or update
     * @param db   db to use
     */
    private void addOrUpdateTask(Task task, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put("task", task.getTask());
        values.put("is_checked", task.isChecked() ? 1 : 0);

        if (task.getTask_id() == -1) {
            values.put("task_list_id",
                    task.getTask_list_id() == -1 ? getNextUniqueId(db) : task.getTask_list_id());
            db.insert(TABLE_NAME_TASKS, null, values);
        } else {
            db.update(TABLE_NAME_TASKS, values, "task_id = ?",
                    new String[]{String.valueOf(task.getTask_id())});
        }

    }

    /**
     * Deletes the desired TaskList based on the given id
     *
     * @param id id of the TaskList to remove
     */
    public void deleteTaskList(int id) {
        SQLiteDatabase db = sqlHelper.getWritableDatabase();
        TaskList taskList = getTaskList(db, id);

        deleteTask(taskList.getTask_list_id(), db);

        db.delete(TABLE_NAME_TASK_LISTS, "task_list_id = ?",
                new String[]{String.valueOf(id)});
        db.close();

    }

    /**
     * Deletes the desired Task based on the given task_list_id
     *
     * @param task_list_id id of the Task to remove
     * @param db           database to use
     */
    private void deleteTask(int task_list_id, SQLiteDatabase db) {
        db.delete(TABLE_NAME_TASKS, "task_list_id = ?",
                new String[]{String.valueOf(task_list_id)});
    }

    /**
     * Deletes the desired Task based on the given task_id
     * but without an existing db
     *
     * @param task_id id of the Task to remove
     */
    public void deleteTask(int task_id) {
        SQLiteDatabase db = sqlHelper.getWritableDatabase();
        db.delete(TABLE_NAME_TASKS, "task_id = ?",
                new String[]{String.valueOf(task_id)});
        db.close();
    }

    /**
     * Returns all of the TaskLists based on if they are archived or not
     *
     * @param isArchived to know what kind of TaskLists are requested
     * @return the desired TaskLists
     */
    public ArrayList<TaskList> getTaskLists(boolean isArchived) {
        ArrayList<TaskList> taskLists = new ArrayList<>();
        SQLiteDatabase db = sqlHelper.getReadableDatabase();
        /* Getting the TaskLists ordered by date (in descendant order) */
        Cursor c = db.query(TABLE_NAME_TASK_LISTS, null, "is_archived = " + (isArchived ? "1" : "0"), null, null, null, "datetime(creation_date) DESC");

        while (c.moveToNext()) {
            TaskList taskList = new TaskList();
            taskList.setTask_list_id(c.getInt(c.getColumnIndex("task_list_id")));
            taskList.setTitle(c.getString(c.getColumnIndex("title")));
            taskList.setBackgroundColor(c.getInt(c.getColumnIndex("background_color")));
            taskList.setIsArchived(c.getInt(c.getColumnIndex("is_archived")) == 1);
            try {
                taskList.setCreationDate(dateFormat.parse(c.getString(c.getColumnIndex("creation_date"))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            taskList.setTasks(getTasks(taskList.getTask_list_id()));
            taskLists.add(taskList);
        }

        c.close();
        db.close();
        return taskLists;
    }

    /**
     * This method is used to retrieve all tasks of a given TaskList id.
     *
     * @param task_list_id the id of the TaskList to retrieve Tasks
     * @return the desired Tasks
     */
    private ArrayList<Task> getTasks(int task_list_id) {
        ArrayList<Task> tasks = new ArrayList<>();
        SQLiteDatabase db = sqlHelper.getReadableDatabase();
        Cursor c = db.query(TABLE_NAME_TASKS, null, "task_list_id = ?", new String[]{String.valueOf(task_list_id)}, null, null, null);

        while (c.moveToNext()) {
            Task task = new Task();
            task.setTask(c.getString(c.getColumnIndex("task")));
            task.setIsChecked(c.getInt(c.getColumnIndex("is_checked")) == 1);
            task.setTask_list_id(c.getInt(c.getColumnIndex("task_list_id")));
            task.setTask_id(c.getInt(c.getColumnIndex("task_id")));
            tasks.add(task);
        }

        c.close();
        return tasks;
    }

    /**
     * Returns the desired TaskList
     *
     * @param db           db to use
     * @param task_list_id desired TaskList id
     * @return the desired TaskList
     */
    private TaskList getTaskList(SQLiteDatabase db, int task_list_id) {
        Cursor c = db.query(TABLE_NAME_TASK_LISTS, null, "task_list_id = ?", new String[]{String.valueOf(task_list_id)}, null, null, null);
        c.moveToFirst();

        TaskList taskList = new TaskList();
        taskList.setTask_list_id(c.getInt(c.getColumnIndex("task_list_id")));
        taskList.setTitle(c.getString(c.getColumnIndex("title")));
        taskList.setBackgroundColor(c.getInt(c.getColumnIndex("background_color")));
        try {
            taskList.setCreationDate(dateFormat.parse(c.getString(c.getColumnIndex("creation_date"))));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        taskList.setTasks(getTasks(taskList.getTask_list_id()));

        c.close();
        return taskList;
    }

    /**
     * This method calculates the UNIQUE id the next TaskList would have when it's created, used
     * to manage correct behaviour of the added Tasks
     *
     * @param db db to use
     * @return the next UNIQUE id
     */
    private int getNextUniqueId(SQLiteDatabase db) {
        int nextUniqueId;
        Cursor c = db.rawQuery("SELECT MAX(task_list_id) FROM " + TABLE_NAME_TASK_LISTS, null);
        c.moveToFirst();
        nextUniqueId = c.getInt(0) + 1;
        c.close();

        return nextUniqueId;
    }

}
