package erina.task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * A task that must be finished by a stated date, shown as
 * {@code [D][ ] return book (by: Oct 15 2019)}.
 */
public class Deadline extends Task {
    /** How the due date is shown to the user, for example {@code Oct 15 2019}. */
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM d yyyy");

    /**
     * When the task is due.
     *
     * <p>Kept as a real date rather than the text the user typed, so it can
     * be reformatted for display and, later, compared and sorted.
     */
    private final LocalDate by;

    /**
     * Creates a deadline that is not yet done.
     *
     * @param description what the user wants to do
     * @param by          the date it is due
     */
    public Deadline(String description, LocalDate by) {
        super(description);
        this.by = by;
    }

    @Override
    public String toSaveString() {
        // LocalDate.toString gives the ISO form (2019-10-15), the same form
        // the user types, so the file stays easy to read and to parse back.
        return "D | " + (isDone() ? "1" : "0") + " | " + getDescription()
                + " | " + by;
    }

    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by.format(DISPLAY_FORMAT) + ")";
    }
}
