package erina;

import erina.task.Deadline;
import erina.task.Event;
import erina.task.Task;
import erina.task.Todo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Reads and writes the task list on the hard disk, so tasks survive between
 * runs of the program.
 *
 * <p>Each task is one line in the file, in the form produced by
 * {@link Task#toSaveString()}, for example:
 * <pre>
 * T | 1 | read book
 * D | 0 | return book | 2019-10-15
 * E | 0 | project meeting | Mon 2pm | 4pm
 * </pre>
 */
public class Storage {
    /** Separator between the fields of one saved task. */
    private static final String FIELD_SEPARATOR = " | ";

    /** Where the task list is kept between runs. */
    private final Path filePath;

    /**
     * Creates a storage that reads and writes the given file.
     *
     * @param filePath where to keep the task list between runs
     */
    public Storage(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * Reads the task list saved by a previous run.
     *
     * @return the saved tasks, or an empty list if nothing has been saved yet
     * @throws ErinaException if the file exists but cannot be read or does
     *                        not make sense
     */
    public List<Task> load() throws ErinaException {
        List<Task> tasks = new ArrayList<>();

        // No file simply means nothing has been saved yet, so it is a normal
        // first run rather than an error.
        if (!Files.exists(filePath)) {
            return tasks;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException e) {
            throw new ErinaException("OOPS!!! I could not read the save file "
                    + filePath + ".");
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            try {
                tasks.add(parseTask(line));
            } catch (ErinaException | ArrayIndexOutOfBoundsException
                    | DateTimeParseException e) {
                // Report the line number as people count them, from 1.
                throw new ErinaException("OOPS!!! Line " + (i + 1)
                        + " of the save file " + filePath + " is not a task I understand:"
                        + " \"" + line + "\"");
            }
        }
        return tasks;
    }

    /**
     * Writes the whole task list, replacing whatever was saved before.
     *
     * <p>Called after every change, so the file always matches the list and
     * nothing is lost if the program is closed abruptly.
     *
     * @param tasks the tasks to save
     * @throws ErinaException if the file cannot be written
     */
    public void save(List<Task> tasks) throws ErinaException {
        List<String> lines = new ArrayList<>();
        for (Task task : tasks) {
            lines.add(task.toSaveString());
        }

        try {
            // The folder does not exist until the first save on a new machine.
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, lines);
        } catch (IOException e) {
            throw new ErinaException("OOPS!!! I could not write the save file "
                    + filePath + ".");
        }
    }

    /**
     * Rebuilds one task from one saved line.
     *
     * @param line the line as written by {@link Task#toSaveString()}
     * @return the task that line describes
     * @throws ErinaException if the line does not describe a task
     */
    private Task parseTask(String line) throws ErinaException {
        // The separator contains |, which is a special character in regular
        // expressions, so split on its literal quoted form.
        String[] fields = line.split(Pattern.quote(FIELD_SEPARATOR));

        Task task;
        switch (fields[0]) {
        case "T":
            task = new Todo(fields[2]);
            break;
        case "D":
            task = new Deadline(fields[2], LocalDate.parse(fields[3]));
            break;
        case "E":
            task = new Event(fields[2], fields[3], fields[4]);
            break;
        default:
            throw new ErinaException("Unknown task type: " + fields[0]);
        }

        if (fields[1].equals("1")) {
            task.markAsDone();
        } else if (!fields[1].equals("0")) {
            throw new ErinaException("Unknown done flag: " + fields[1]);
        }
        return task;
    }
}
