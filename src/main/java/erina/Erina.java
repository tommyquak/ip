package erina;

import java.nio.file.Path;
import java.util.List;

import erina.task.Task;

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
 *
 * <p>Commands are carried out by {@link #getResponse(String)}, which returns
 * what Erina wants to say rather than printing it. Both front ends call it:
 * the console loop in {@link #run()} prints the result, and the GUI shows it
 * in a dialog box. Behaviour therefore cannot drift between the two.
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

    /** Whether the user has asked to leave. */
    private boolean isExit;

    /** What went wrong while loading, or {@code null} if loading succeeded. */
    private String loadError;

    /**
     * Creates an Erina that saves its tasks to the given file.
     *
     * @param saveFile where to keep the task list between runs
     */
    public Erina(Path saveFile) {
        this.storage = new Storage(saveFile);
        this.ui = new Ui();

        // A save file that cannot be understood should not end the program:
        // remember the complaint, and carry on with an empty list. The
        // unreadable file is only replaced once the user changes something.
        try {
            this.tasks = new TaskList(storage.load());
        } catch (ErinaException e) {
            this.loadError = e.getMessage() + "\nI'll start with an empty list instead.";
            this.tasks = new TaskList();
        }
    }

    /**
     * Returns the complaint about the save file, if there was one.
     *
     * @return the message to show at startup, or {@code null} if all was well
     */
    public String getLoadError() {
        return loadError;
    }

    /**
     * Returns whether the user has asked to leave.
     *
     * @return {@code true} once a {@code bye} command has been handled
     */
    public boolean isExit() {
        return isExit;
    }

    /**
     * Starts Erina with the standard save file location.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        new Erina(SAVE_FILE).run();
    }

    /** Greets the user, serves commands until they leave, then says goodbye. */
    public void run() {
        ui.showWelcome();
        if (loadError != null) {
            ui.reply(loadError.split("\n"));
        }

        // Read one command per line until the user asks to exit.
        while (true) {
            String input = ui.readCommand();
            if (input == null) {
                // Input ended without a bye command, so say goodbye anyway.
                ui.showGoodbye();
                break;
            }

            // A blank line is not worth an error message; just read the next one.
            if (input.trim().isEmpty()) {
                continue;
            }

            ui.reply(getResponse(input).split("\n"));
            if (isExit) {
                break;
            }
        }
    }

    /**
     * Carries out one command and returns what Erina wants to say about it.
     *
     * <p>Returning the reply instead of printing it is what lets the console
     * and the GUI share this method: each decides for itself how to show the
     * result. Failures are returned as their message rather than thrown, so a
     * bad command never ends the conversation.
     *
     * @param input one line as typed by the user
     * @return the reply, whose lines are separated by newlines
     */
    public String getResponse(String input) {
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        // Split into the command word and everything after it, so that
        // commands taking an argument (mark 2) can be told apart from
        // commands that do not (list).
        String[] parts = trimmed.split(" ", 2);
        String keyword = parts[0];
        String argument = parts.length > 1 ? parts[1].trim() : "";

        try {
            Command command = Command.fromKeyword(keyword);
            if (command == Command.BYE) {
                isExit = true;
                return Ui.FAREWELL;
            }

            String response = handleCommand(command, argument);

            // Saving after every successful command, in one place, keeps
            // the file in step with the list without each command having
            // to remember to save.
            storage.save(tasks.asList());
            return response;
        } catch (ErinaException e) {
            return e.getMessage();
        }
    }

    /**
     * Carries out one command from the user.
     *
     * @param command  the command the user asked for
     * @param argument everything after the command word, possibly empty
     * @return what Erina wants to say about it
     * @throws ErinaException if the argument is missing or does not make sense
     */
    private String handleCommand(Command command, String argument) throws ErinaException {
        switch (command) {
            case LIST:
                return listTasks();
            case MARK:
                return setDone(argument, true);
            case UNMARK:
                return setDone(argument, false);
            case TODO:
                return addTask(Parser.parseTodo(argument));
            case DEADLINE:
                return addTask(Parser.parseDeadline(argument));
            case EVENT:
                return addTask(Parser.parseEvent(argument));
            case DELETE:
                return deleteTask(argument);
            case FIND:
                return findTasks(argument);
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
     * @return the confirmation to show
     */
    private String addTask(Task task) {
        tasks.add(task);
        return String.join("\n",
                "Got it. I've added this task:",
                "  " + task,
                "Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Removes the task at the given position and confirms it to the user.
     *
     * @param argument the task number as typed by the user, counting from 1
     * @return the confirmation to show
     * @throws ErinaException if the number is missing, not a number, or does
     *                        not refer to an existing task
     */
    private String deleteTask(String argument) throws ErinaException {
        // Remove returns the task it took out, so it can be shown to the user
        // after it is no longer in the list.
        Task removed = tasks.remove(Parser.parseIndex(argument, tasks.size()));
        return String.join("\n",
                "Noted. I've removed this task:",
                "  " + removed,
                "Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Marks the task at the given position as done or not done.
     *
     * @param argument the task number as typed by the user, counting from 1
     * @param isDone   {@code true} to mark done, {@code false} to mark not done
     * @return the confirmation to show
     * @throws ErinaException if the number is missing, not a number, or does
     *                        not refer to an existing task
     */
    private String setDone(String argument, boolean isDone) throws ErinaException {
        Task task = tasks.get(Parser.parseIndex(argument, tasks.size()));

        if (isDone) {
            task.markAsDone();
            return "Nice! I've marked this task as done:\n  " + task;
        }
        task.markAsNotDone();
        return "OK, I've marked this task as not done yet:\n  " + task;
    }

    /**
     * Shows the tasks whose descriptions contain the given keyword.
     *
     * <p>Matches are numbered from 1 by their position among the matches,
     * not their position in the full list, mirroring how the course sample
     * output presents them.
     *
     * @param argument the text to search for
     * @return the matching tasks, or a message saying there were none
     * @throws ErinaException if no text to search for was given
     */
    private String findTasks(String argument) throws ErinaException {
        if (argument.isEmpty()) {
            throw new ErinaException("OOPS!!! Please tell me what to look for, "
                    + "like: find book");
        }

        List<Task> matches = tasks.find(argument);
        if (matches.isEmpty()) {
            return "No tasks match \"" + argument + "\".";
        }

        String[] lines = new String[matches.size() + 1];
        lines[0] = "Here are the matching tasks in your list:";
        for (int i = 0; i < matches.size(); i++) {
            lines[i + 1] = (i + 1) + "." + matches.get(i);
        }
        return String.join("\n", lines);
    }

    /**
     * Shows every task added so far, numbered from 1.
     *
     * <p>Shows a prompt instead of an empty block when nothing has been added.
     *
     * @return the numbered task list, or a prompt if there is nothing to show
     */
    private String listTasks() {
        if (tasks.isEmpty()) {
            return "Your list is empty. Add something to get started!";
        }

        // One heading line, then one line per task.
        String[] lines = new String[tasks.size() + 1];
        lines[0] = "Here are the tasks in your list:";
        for (int i = 0; i < tasks.size(); i++) {
            // Users count from 1, so display position i as i + 1.
            lines[i + 1] = (i + 1) + "." + tasks.get(i);
        }
        return String.join("\n", lines);
    }
}
