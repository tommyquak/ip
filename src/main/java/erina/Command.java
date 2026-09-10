package erina;

import java.util.Arrays;
import java.util.Optional;

/**
 * The set of instructions Erina understands.
 *
 * <p>Using an enum rather than loose String constants means the valid
 * commands are stated in exactly one place, the compiler checks every use
 * against that list instead of trusting a quoted string, and a switch over
 * this type can be checked for completeness.
 */
public enum Command {
    /** Shows every task added so far. */
    LIST("list"),

    /** Marks a task as completed. */
    MARK("mark"),

    /** Marks a task as not yet completed. */
    UNMARK("unmark"),

    /** Adds a task with only a description. */
    TODO("todo"),

    /** Adds a task due by a stated time. */
    DEADLINE("deadline"),

    /** Adds a task spanning a period of time. */
    EVENT("event"),

    /** Removes a task from the list. */
    DELETE("delete"),

    /** Shows the tasks whose descriptions contain a keyword. */
    FIND("find"),

    /** Ends the conversation. */
    BYE("bye");

    /** The word the user types to invoke this command. */
    private final String keyword;

    Command(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Returns the command the user asked for.
     *
     * <p>This is the single point at which typed text becomes a known
     * command, so it is also the single place that can decide the text is
     * not one.
     *
     * @param keyword the first word the user typed
     * @return the matching command
     * @throws ErinaException if no command uses that keyword
     */
    public static Command fromKeyword(String keyword) throws ErinaException {
        // Optional makes the "not found" case explicit instead of falling out
        // of a loop; the checked exception is thrown outside the stream, since
        // lambdas cannot throw checked exceptions.
        Optional<Command> match = Arrays.stream(values())
                .filter(command -> command.keyword.equals(keyword))
                .findFirst();
        if (match.isEmpty()) {
            throw new ErinaException(
                    "OOPS!!! I'm sorry, but I don't know what that means :-(");
        }
        return match.get();
    }

    /**
     * Returns the word the user types to invoke this command.
     *
     * @return the keyword, for example {@code "mark"}
     */
    public String getKeyword() {
        return keyword;
    }
}
