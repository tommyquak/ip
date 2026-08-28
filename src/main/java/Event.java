/**
 * A task that spans a period of time, shown as
 * {@code [E][ ] project meeting (from: Mon 2pm to: 4pm)}.
 */
public class Event extends Task {
    /** When the event starts, kept as the text the user typed. */
    private final String from;

    /** When the event ends, kept as the text the user typed. */
    private final String to;

    /**
     * Creates an event that is not yet done.
     *
     * @param description what the event is
     * @param from        when it starts, as typed by the user
     * @param to          when it ends, as typed by the user
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    @Override
    public String toSaveString() {
        return "E | " + (isDone() ? "1" : "0") + " | " + getDescription()
                + " | " + from + " | " + to;
    }

    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
