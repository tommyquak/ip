/**
 * A task that must be finished by a stated time, shown as
 * {@code [D][ ] return book (by: Sunday)}.
 */
public class Deadline extends Task {
    /**
     * When the task is due, kept as the text the user typed.
     *
     * <p>Storing it as a String keeps this increment simple; a later
     * increment replaces it with a real date type so that dates can be
     * compared and reformatted.
     */
    private final String by;

    /**
     * Creates a deadline that is not yet done.
     *
     * @param description what the user wants to do
     * @param by          when it is due, as typed by the user
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    @Override
    public String toSaveString() {
        return "D | " + (isDone() ? "1" : "0") + " | " + getDescription()
                + " | " + by;
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}
