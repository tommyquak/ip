package erina;

import erina.task.Deadline;
import erina.task.Event;
import erina.task.Todo;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Turns the text the user types into the objects the program works with.
 *
 * <p>All knowledge of command syntax, such as {@code /by} and {@code /from}
 * markers, date formats and task numbering, lives here. The rest of the
 * program never inspects raw input, so a change to the syntax touches only
 * this class.
 */
public class Parser {
    /** This class is not meant to be instantiated; its methods are static. */
    private Parser() {
    }

    /**
     * Builds a to-do from the text after the {@code todo} command.
     *
     * @param argument the text after the command word
     * @return the to-do described by that text
     * @throws ErinaException if no description was given
     */
    public static Todo parseTodo(String argument) throws ErinaException {
        if (argument.isEmpty()) {
            throw new ErinaException(
                    "OOPS!!! The description of a todo cannot be empty.");
        }
        return new Todo(argument);
    }

    /**
     * Builds a deadline from the text after the {@code deadline}
     * command, which has the form {@code <description> /by <yyyy-mm-dd>}.
     *
     * @param argument the text after the command word
     * @return the deadline described by that text
     * @throws ErinaException if the description or the /by part is missing,
     *                        or the /by part is not a date
     */
    public static Deadline parseDeadline(String argument) throws ErinaException {
        if (argument.isEmpty()) {
            throw new ErinaException(
                    "OOPS!!! The description of a deadline cannot be empty.");
        }

        // Limit of 2 so that a description containing "/by" is left intact.
        String[] parts = argument.split(" /by ", 2);
        if (parts.length < 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new ErinaException("OOPS!!! A deadline needs a description and a "
                    + "/by date, like: deadline return book /by 2019-10-15");
        }
        return new Deadline(parts[0].trim(), parseDate(parts[1].trim()));
    }

    /**
     * Builds an event from the text after the {@code event} command,
     * which has the form {@code <description> /from <start> /to <end>}.
     *
     * @param argument the text after the command word
     * @return the event described by that text
     * @throws ErinaException if the description, the /from part or the /to
     *                        part is missing
     */
    public static Event parseEvent(String argument) throws ErinaException {
        if (argument.isEmpty()) {
            throw new ErinaException(
                    "OOPS!!! The description of an event cannot be empty.");
        }

        String[] fromParts = argument.split(" /from ", 2);
        if (fromParts.length < 2 || fromParts[0].isBlank()) {
            throw new ErinaException("OOPS!!! An event needs a description, a /from "
                    + "time and a /to time, like: event project meeting /from Mon 2pm /to 4pm");
        }

        String[] toParts = fromParts[1].split(" /to ", 2);
        if (toParts.length < 2 || toParts[0].isBlank() || toParts[1].isBlank()) {
            throw new ErinaException("OOPS!!! An event needs a description, a /from "
                    + "time and a /to time, like: event project meeting /from Mon 2pm /to 4pm");
        }
        return new Event(fromParts[0].trim(), toParts[0].trim(), toParts[1].trim());
    }

    /**
     * Turns the task number the user typed into a position in the list.
     *
     * <p>Every command taking a task number validates it here, so the checks
     * and their wording stay in one place.
     *
     * @param argument  the task number as typed by the user, counting from 1
     * @param taskCount how many tasks there are to refer to
     * @return the matching 0-based index
     * @throws ErinaException if the number is missing, not a number, or does
     *                        not refer to an existing task
     */
    public static int parseIndex(String argument, int taskCount) throws ErinaException {
        if (argument.isEmpty()) {
            throw new ErinaException("OOPS!!! Please tell me which task number, "
                    + "like: mark 2");
        }

        int number;
        try {
            number = Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            // Rethrown as an ErinaException so the main loop deals with one
            // kind of failure, phrased for the user rather than the compiler.
            throw new ErinaException("OOPS!!! \"" + argument
                    + "\" is not a task number.");
        }

        if (taskCount == 0) {
            throw new ErinaException("OOPS!!! There are no tasks yet, so there is "
                    + "no task " + number + ".");
        }
        if (number < 1 || number > taskCount) {
            throw new ErinaException("OOPS!!! There is no task " + number
                    + ". You have " + taskCount + " tasks.");
        }

        // The user counts from 1 but the list is indexed from 0.
        return number - 1;
    }

    /**
     * Turns a date the user typed into a real date.
     *
     * @param text the date as typed, expected as {@code yyyy-mm-dd}
     * @return the date it names
     * @throws ErinaException if the text is not a date in that form
     */
    public static LocalDate parseDate(String text) throws ErinaException {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new ErinaException("OOPS!!! \"" + text + "\" is not a date I "
                    + "understand. Please use yyyy-mm-dd, like 2019-10-15.");
        }
    }
}
