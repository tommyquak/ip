import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point of the Erina chatbot.
 *
 * <p>Erina stores to-dos, deadlines and events, lists them with their
 * completion status, marks them done or not done, deletes them, explains
 * what went wrong when a command cannot be carried out, and stops on the
 * {@code bye} command. The task list is saved to the hard disk after
 * every change and loaded back on the next start, so tasks survive
 * between runs.
 *
 * <p>The instructions Erina accepts are listed in {@link Command}.
 */
public class Erina {
    /** Where the task list is kept between runs. */
    private static final Path SAVE_FILE = Path.of("data", "erina.txt");

    /** Horizontal rule used to visually separate each of Erina's replies. */
    private static final String DIVIDER =
            "    ____________________________________________________________";

    public static void main(String[] args) {
        String banner = " _____      _             \n"
                + "| ____|_ __(_)_ __   __ _ \n"
                + "|  _| | '__| | '_ \\ / _` |\n"
                + "| |___| |  | | | | | (_| |\n"
                + "|_____|_|  |_|_| |_|\\__,_|\n";
        System.out.println(banner);

        greet();

        Storage storage = new Storage(SAVE_FILE);

        // A save file that cannot be understood should not end the program:
        // report it and carry on with an empty list. The unreadable file is
        // only replaced once the user changes something.
        List<Task> tasks;
        try {
            tasks = storage.load();
        } catch (ErinaException e) {
            reply(e.getMessage(), "I'll start with an empty list instead.");
            tasks = new ArrayList<>();
        }

        // Read one command per line until the user asks to exit.
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();

            // A blank line is not worth an error message; just read the next one.
            if (input.isEmpty()) {
                continue;
            }

            // Split into the command word and everything after it, so that
            // commands taking an argument (mark 2) can be told apart from
            // commands that do not (list).
            String[] parts = input.split(" ", 2);
            String keyword = parts[0];
            String argument = parts.length > 1 ? parts[1].trim() : "";

            // Looking the command up and carrying it out may both reject what
            // the user typed. Catching here, at the top of the loop, means one
            // place decides what a failed command looks like, and a bad
            // command never ends the conversation.
            try {
                Command command = Command.fromKeyword(keyword);
                if (command == Command.BYE) {
                    break;
                }
                handleCommand(tasks, command, argument);

                // Saving after every successful command, in one place, keeps
                // the file in step with the list without each command having
                // to remember to save.
                storage.save(tasks);
            } catch (ErinaException e) {
                reply(e.getMessage());
            }
        }

        exit();
    }

    /**
     * Carries out one command from the user.
     *
     * @param tasks    the list the command acts on
     * @param command  the command the user asked for
     * @param argument everything after the command word, possibly empty
     * @throws ErinaException if the argument is missing or does not make sense
     */
    private static void handleCommand(List<Task> tasks, Command command, String argument)
            throws ErinaException {
        switch (command) {
        case LIST:
            listTasks(tasks);
            break;
        case MARK:
            setDone(tasks, argument, true);
            break;
        case UNMARK:
            setDone(tasks, argument, false);
            break;
        case TODO:
            addTask(tasks, parseTodo(argument));
            break;
        case DEADLINE:
            addTask(tasks, parseDeadline(argument));
            break;
        case EVENT:
            addTask(tasks, parseEvent(argument));
            break;
        case DELETE:
            deleteTask(tasks, argument);
            break;
        default:
            // BYE is handled by the main loop, which has to stop reading.
            // Unknown words never reach here: Command.fromKeyword rejects them.
            throw new ErinaException(
                    "OOPS!!! I'm sorry, but I don't know what that means :-(");
        }
    }

    /**
     * Adds an already-built task to the list and confirms it to the user.
     *
     * @param tasks the list to add to
     * @param task  the task to add
     */
    private static void addTask(List<Task> tasks, Task task) {
        tasks.add(task);
        reply("Got it. I've added this task:",
                "  " + task,
                "Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Builds a to-do from the text after the {@code todo} command.
     *
     * @param argument the text after the command word
     * @return the to-do described by that text
     * @throws ErinaException if no description was given
     */
    private static Todo parseTodo(String argument) throws ErinaException {
        if (argument.isEmpty()) {
            throw new ErinaException(
                    "OOPS!!! The description of a todo cannot be empty.");
        }
        return new Todo(argument);
    }

    /**
     * Builds a deadline from the text after the {@code deadline}
     * command, which has the form {@code <description> /by <when>}.
     *
     * @param argument the text after the command word
     * @return the deadline described by that text
     * @throws ErinaException if the description or the /by part is missing
     */
    private static Deadline parseDeadline(String argument) throws ErinaException {
        if (argument.isEmpty()) {
            throw new ErinaException(
                    "OOPS!!! The description of a deadline cannot be empty.");
        }

        // Limit of 2 so that a description containing "/by" is left intact.
        String[] parts = argument.split(" /by ", 2);
        if (parts.length < 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new ErinaException("OOPS!!! A deadline needs a description and a "
                    + "/by time, like: deadline return book /by Sunday");
        }
        return new Deadline(parts[0].trim(), parts[1].trim());
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
    private static Event parseEvent(String argument) throws ErinaException {
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
     * Removes the task at the given position and confirms it to the user.
     *
     * @param tasks    the list to remove from
     * @param argument the task number as typed by the user, counting from 1
     * @throws ErinaException if the number is missing, not a number, or does
     *                        not refer to an existing task
     */
    private static void deleteTask(List<Task> tasks, String argument) throws ErinaException {
        // Remove returns the task it took out, so it can be shown to the user
        // after it is no longer in the list.
        Task removed = tasks.remove(parseIndex(tasks, argument));
        reply("Noted. I've removed this task:",
                "  " + removed,
                "Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Marks the task at the given position as done or not done.
     *
     * @param tasks    the list holding the task
     * @param argument the task number as typed by the user, counting from 1
     * @param isDone   {@code true} to mark done, {@code false} to mark not done
     * @throws ErinaException if the number is missing, not a number, or does
     *                        not refer to an existing task
     */
    private static void setDone(List<Task> tasks, String argument, boolean isDone)
            throws ErinaException {
        Task task = tasks.get(parseIndex(tasks, argument));

        if (isDone) {
            task.markAsDone();
            reply("Nice! I've marked this task as done:", "  " + task);
        } else {
            task.markAsNotDone();
            reply("OK, I've marked this task as not done yet:", "  " + task);
        }
    }

    /**
     * Turns the task number the user typed into a position in the list.
     *
     * <p>Every command taking a task number validates it here, so the checks
     * and their wording stay in one place.
     *
     * @param tasks    the list the number refers to
     * @param argument the task number as typed by the user, counting from 1
     * @return the matching 0-based index into {@code tasks}
     * @throws ErinaException if the number is missing, not a number, or does
     *                        not refer to an existing task
     */
    private static int parseIndex(List<Task> tasks, String argument) throws ErinaException {
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

        if (tasks.isEmpty()) {
            throw new ErinaException("OOPS!!! There are no tasks yet, so there is "
                    + "no task " + number + ".");
        }
        if (number < 1 || number > tasks.size()) {
            throw new ErinaException("OOPS!!! There is no task " + number
                    + ". You have " + tasks.size() + " tasks.");
        }

        // The user counts from 1 but the list is indexed from 0.
        return number - 1;
    }

    /**
     * Shows every task added so far, numbered from 1.
     *
     * <p>Shows a prompt instead of an empty block when nothing has been added.
     *
     * @param tasks the tasks to show
     */
    private static void listTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            reply("Your list is empty. Add something to get started!");
            return;
        }

        // One heading line, then one line per task.
        String[] lines = new String[tasks.size() + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int i = 0; i < tasks.size(); i++) {
            // Users count from 1, so display position i as i + 1.
            lines[i + 1] = (i + 1) + "." + tasks.get(i);
        }
        reply(lines);
    }

    /** Prints Erina's opening message. */
    private static void greet() {
        reply("Hello! I'm Erina", "What can I do for you?");
    }

    /** Prints Erina's farewell message, shown just before the program ends. */
    private static void exit() {
        reply("Bye. Hope to see you again soon!");
    }

    /**
     * Prints one reply, wrapped in horizontal rules so it stands out from
     * the text the user typed.
     *
     * @param lines the lines of the reply, printed one per output line
     */
    private static void reply(String... lines) {
        System.out.println(DIVIDER);
        for (String line : lines) {
            System.out.println("     " + line);
        }
        System.out.println(DIVIDER);
    }
}
