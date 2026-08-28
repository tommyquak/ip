import java.nio.file.Path;

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
 * <p>The instructions Erina accepts are listed in {@link Command}. This class
 * only coordinates: {@link Ui} talks to the user, {@link Parser} makes sense
 * of what they typed, {@link TaskList} holds the tasks and {@link Storage}
 * keeps them on disk.
 */
public class Erina {
    /** Where the task list is kept between runs. */
    private static final Path SAVE_FILE = Path.of("data", "erina.txt");

    /** Keeps the task list on disk between runs. */
    private final Storage storage;

    /** Talks to the user. */
    private final Ui ui;

    /** The tasks the user is keeping. */
    private TaskList tasks;

    /**
     * Creates an Erina that saves its tasks to the given file.
     *
     * @param saveFile where to keep the task list between runs
     */
    public Erina(Path saveFile) {
        this.storage = new Storage(saveFile);
        this.ui = new Ui();
    }

    public static void main(String[] args) {
        new Erina(SAVE_FILE).run();
    }

    /** Greets the user, serves commands until they leave, then says goodbye. */
    public void run() {
        ui.showWelcome();

        // A save file that cannot be understood should not end the program:
        // report it and carry on with an empty list. The unreadable file is
        // only replaced once the user changes something.
        try {
            tasks = new TaskList(storage.load());
        } catch (ErinaException e) {
            ui.reply(e.getMessage(), "I'll start with an empty list instead.");
            tasks = new TaskList();
        }

        // Read one command per line until the user asks to exit.
        while (true) {
            String input = ui.readCommand();
            if (input == null) {
                break;
            }
            input = input.trim();

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
                handleCommand(command, argument);

                // Saving after every successful command, in one place, keeps
                // the file in step with the list without each command having
                // to remember to save.
                storage.save(tasks.asList());
            } catch (ErinaException e) {
                ui.reply(e.getMessage());
            }
        }

        ui.showGoodbye();
    }

    /**
     * Carries out one command from the user.
     *
     * @param command  the command the user asked for
     * @param argument everything after the command word, possibly empty
     * @throws ErinaException if the argument is missing or does not make sense
     */
    private void handleCommand(Command command, String argument) throws ErinaException {
        switch (command) {
        case LIST:
            listTasks();
            break;
        case MARK:
            setDone(argument, true);
            break;
        case UNMARK:
            setDone(argument, false);
            break;
        case TODO:
            addTask(Parser.parseTodo(argument));
            break;
        case DEADLINE:
            addTask(Parser.parseDeadline(argument));
            break;
        case EVENT:
            addTask(Parser.parseEvent(argument));
            break;
        case DELETE:
            deleteTask(argument);
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
     * @param task the task to add
     */
    private void addTask(Task task) {
        tasks.add(task);
        ui.reply("Got it. I've added this task:",
                "  " + task,
                "Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Removes the task at the given position and confirms it to the user.
     *
     * @param argument the task number as typed by the user, counting from 1
     * @throws ErinaException if the number is missing, not a number, or does
     *                        not refer to an existing task
     */
    private void deleteTask(String argument) throws ErinaException {
        // Remove returns the task it took out, so it can be shown to the user
        // after it is no longer in the list.
        Task removed = tasks.remove(Parser.parseIndex(argument, tasks.size()));
        ui.reply("Noted. I've removed this task:",
                "  " + removed,
                "Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Marks the task at the given position as done or not done.
     *
     * @param argument the task number as typed by the user, counting from 1
     * @param isDone   {@code true} to mark done, {@code false} to mark not done
     * @throws ErinaException if the number is missing, not a number, or does
     *                        not refer to an existing task
     */
    private void setDone(String argument, boolean isDone) throws ErinaException {
        Task task = tasks.get(Parser.parseIndex(argument, tasks.size()));

        if (isDone) {
            task.markAsDone();
            ui.reply("Nice! I've marked this task as done:", "  " + task);
        } else {
            task.markAsNotDone();
            ui.reply("OK, I've marked this task as not done yet:", "  " + task);
        }
    }

    /**
     * Shows every task added so far, numbered from 1.
     *
     * <p>Shows a prompt instead of an empty block when nothing has been added.
     */
    private void listTasks() {
        if (tasks.isEmpty()) {
            ui.reply("Your list is empty. Add something to get started!");
            return;
        }

        // One heading line, then one line per task.
        String[] lines = new String[tasks.size() + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int i = 0; i < tasks.size(); i++) {
            // Users count from 1, so display position i as i + 1.
            lines[i + 1] = (i + 1) + "." + tasks.get(i);
        }
        ui.reply(lines);
    }
}
