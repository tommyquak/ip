package erina;

import java.util.ArrayList;
import java.util.List;

import erina.task.Task;

/**
 * The list of tasks the user is keeping.
 *
 * <p>Wraps the underlying {@link List} so that the rest of the program works
 * with tasks in the user's terms, and so that list-wide behaviour added later
 * (searching, sorting) has one natural home.
 */
public class TaskList {
    /** The tasks, in the order the user added them. */
    private final List<Task> tasks;

    /** Creates an empty task list. */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a task list holding the given tasks, typically loaded from the
     * save file.
     *
     * @param tasks the tasks to start with
     */
    public TaskList(List<Task> tasks) {
        this.tasks = tasks;
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task the task to add
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Removes and returns the task at the given position.
     *
     * @param index 0-based position of the task to remove
     * @return the task that was removed
     */
    public Task remove(int index) {
        return tasks.remove(index);
    }

    /**
     * Returns the task at the given position.
     *
     * @param index 0-based position of the task
     * @return the task at that position
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Returns how many tasks the list holds.
     *
     * @return the number of tasks
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns whether the list holds no tasks.
     *
     * @return {@code true} if there are no tasks
     */
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    /**
     * Returns the tasks whose descriptions contain the given keyword.
     *
     * <p>Matching ignores case, so {@code find book} also finds
     * {@code Book club}: when searching, the user is after the task, not the
     * capitalisation they happened to type it with.
     *
     * @param keyword the text to look for inside task descriptions
     * @return the matching tasks, in the order they appear in the list
     */
    public List<Task> find(String keyword) {
        List<Task> matches = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getDescription().toLowerCase().contains(keyword.toLowerCase())) {
                matches.add(task);
            }
        }
        return matches;
    }

    /**
     * Returns the tasks as a plain list, for code that needs to iterate or
     * save them.
     *
     * @return the underlying list of tasks
     */
    public List<Task> asList() {
        return tasks;
    }
}
