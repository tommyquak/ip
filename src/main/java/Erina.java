import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point of the Erina chatbot.
 *
 * <p>At this stage (Level-2) Erina stores whatever the user types as a task,
 * lists the stored tasks on request, and stops on the
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

    public static void main(String[] args) {
        String banner = " _____      _             \n"
                + "| ____|_ __(_)_ __   __ _ \n"
                + "|  _| | '__| | '_ \\ / _` |\n"
                + "| |___| |  | | | | | (_| |\n"
                + "|_____|_|  |_|_| |_|\\__,_|\n";
        System.out.println(banner);

        greet();

        // Tasks are held in memory only; they are not saved between runs yet.
        List<String> tasks = new ArrayList<>();

        // Read one command per line until the user asks to exit.
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            if (input.equals(EXIT_COMMAND)) {
                break;
            } else if (input.equals(LIST_COMMAND)) {
                listTasks(tasks);
            } else {
                addTask(tasks, input);
            }
        }

        exit();
    }

    /**
     * Adds a task to the list and confirms it to the user.
     *
     * @param tasks the list to add to
     * @param task  the task description as typed by the user
     */
    private static void addTask(List<String> tasks, String task) {
        tasks.add(task);
        reply("added: " + task);
    }

    /**
     * Shows every task added so far, numbered from 1.
     *
     * <p>Shows a prompt instead of an empty block when nothing has been added.
     *
     * @param tasks the tasks to show
     */
    private static void listTasks(List<String> tasks) {
        if (tasks.isEmpty()) {
            reply("Your list is empty. Add something to get started!");
            return;
        }

        String[] lines = new String[tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
            // Users count from 1, so display position i as i + 1.
            lines[i] = (i + 1) + ". " + tasks.get(i);
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
