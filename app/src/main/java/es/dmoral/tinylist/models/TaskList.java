package es.dmoral.tinylist.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by grend on 13/01/2016.
 *
 * This class represents a Task (or shopping...) List, containing all items/tasks.
 */
public class TaskList implements Serializable {

    private int task_list_id;
    private String title;
    private Date creationDate;
    private ArrayList<Task> tasks;
    private int backgroundColor;
    private boolean isArchived;

    public TaskList() {

    }

    public TaskList(int task_list_id, String title, Date creationDate, ArrayList<Task> tasks, int backgroundColor, boolean isArchived) {
        this.task_list_id = task_list_id;
        this.title = title;
        this.creationDate = creationDate;
        this.tasks = tasks;
        this.backgroundColor = backgroundColor;
        this.isArchived = isArchived;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    public int getTask_list_id() {
        return task_list_id;
    }

    public void setTask_list_id(int task_list_id) {
        this.task_list_id = task_list_id;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setIsArchived(boolean isArchived) {
        this.isArchived = isArchived;
    }
}
