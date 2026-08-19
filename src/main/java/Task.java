/**
 * A single item on the user's list.
 *
 * <p>A task is a description together with whether it has been completed.
 * Keeping both in one object, rather than parallel lists of strings and
 * booleans, means the two can never fall out of step.
 *
 * <p>This class is abstract because every task the user can create is one of
 * the specific kinds below it ({@link Todo}, {@link Deadline}, {@link Event}).
 * Each kind decides how it is shown by overriding {@link #toString()}, while
 * the description and completion flag are handled once, here.
 */
public abstract class Task {
    /** What the user wants to do. Fixed once the task is created. */
    private final String description;

    /** Whether the user has marked this task as completed. */
    private boolean isDone;

    /**
     * Creates a task that is not yet done.
     *
     * @param description what the user wants to do
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /** Marks this task as completed. */
    public void markAsDone() {
        this.isDone = true;
    }

    /** Marks this task as not yet completed. */
    public void markAsNotDone() {
        this.isDone = false;
    }

    /**
     * Returns the symbol shown inside the status box.
     *
     * @return {@code "X"} if this task is done, a single space otherwise
     */
    public String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    /**
     * Returns the status box and description shared by every kind of task,
     * for example {@code [X] read book}.
     *
     * <p>Subclasses prepend their own type box and append any extra detail.
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
