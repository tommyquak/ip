import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point of the Erina chatbot.
 *
 * <p>At this stage (Level-3) Erina stores tasks, lists them with their
 * completion status, marks them done or not done, and stops on the
 * {@value #EXIT_COMMAND} command.
 */
public class Erina {
    /** Horizontal rule used to visually separate each of Erina's replies. */
    private static final String DIVIDER =
            "    ____________________________________________________________";

    /** Command that ends the conversation. */
    private static final String EXIT_COMMAND = "bye";

    /** Command that shows every task added so far. */
    private static final String LIST_COMMAND = "list";

    /** Command that marks a task as completed. */
    private static final String MARK_COMMAND = "mark";

    /** Command that marks a task as not yet completed. */
    private static final String UNMARK_COMMAND = "unmark";

    public static void main(String[] args) {
        String banner = " _____      _             \n"
                + "| ____|_ __(_)_ __   __ _ \n"
                + "|  _| | '__| | '_ \\ / _` |\n"
                + "| |___| |  | | | | | (_| |\n"
                + "|_____|_|  |_|_| |_|\\__,_|\n";
        System.out.println(banner);

        greet();

        // Tasks are held in memory only; they are not saved between runs yet.
        List<Task> tasks = new ArrayList<>();

        // Read one command per line until the user asks to exit.
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();

            // Split into the command word and everything after it, so that
            // commands taking an argument (mark 2) can be told apart from
            // commands that do not (list).
            String[] parts = input.split(" ", 2);
            String command = parts[0];
            String argument = parts.length > 1 ? parts[1] : "";

            if (command.equals(EXIT_COMMAND)) {
                break;
            } else if (command.equals(LIST_COMMAND)) {
                listTasks(tasks);
            } else if (command.equals(MARK_COMMAND)) {
                setDone(tasks, argument, true);
            } else if (command.equals(UNMARK_COMMAND)) {
                setDone(tasks, argument, false);
            } else {
                addTask(tasks, input);
            }
        }

        exit();
    }

    /**
     * Adds a task to the list and confirms it to the user.
     *
     * @param tasks       the list to add to
     * @param description the task description as typed by the user
     */
    private static void addTask(List<Task> tasks, String description) {
        tasks.add(new Task(description));
        reply("added: " + description);
    }

    /**
     * Marks the task at the given position as done or not done.
     *
     * @param tasks    the list holding the task
     * @param argument the task number as typed by the user, counting from 1
     * @param isDone   {@code true} to mark done, {@code false} to mark not done
     */
    private static void setDone(List<Task> tasks, String argument, boolean isDone) {
        // The user counts from 1 but the list is indexed from 0.
        int index = Integer.parseInt(argument.trim()) - 1;
        Task task = tasks.get(index);

        if (isDone) {
            task.markAsDone();
            reply("Nice! I've marked this task as done:", "  " + task);
        } else {
            task.markAsNotDone();
            reply("OK, I've marked this task as not done yet:", "  " + task);
        }
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
