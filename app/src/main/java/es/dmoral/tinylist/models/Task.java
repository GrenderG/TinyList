package es.dmoral.tinylist.models;

import java.io.Serializable;

/**
 * Created by grend on 13/01/2016.
 * <p>
 * This class represents every single item/task of a TaskList.
 */
public class Task implements Serializable {

    public static final String DONE_TASK_MARK = "[✔]";
    public static final String UNDONE_TASK_MARK = "[❌]";
    private int task_id;
    private int task_list_id;
    private String task;
    private boolean isChecked;

    public Task() {
        this.task_id = -1;
        this.isChecked = false;
        this.task_list_id = -1;
        this.task = "";
    }

    public Task(int task_list_id) {
        this.task_list_id = task_list_id;
        this.task_id = -1;
        this.isChecked = false;
        this.task = "";
    }

    public Task(int task_id, int task_list_id, String task, boolean isChecked) {
        this.task_id = task_id;
        this.task_list_id = task_list_id;
        this.task = task;
        this.isChecked = isChecked;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public int getTask_id() {
        return task_id;
    }

    public void setTask_id(int task_id) {
        this.task_id = task_id;
    }

    public int getTask_list_id() {
        return task_list_id;
    }

    public void setTask_list_id(int task_list_id) {
        this.task_list_id = task_list_id;
    }
}
