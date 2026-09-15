package erina;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link Erina}'s replies, driven through {@link Erina#getResponse}.
 *
 * <p>Each test gets its own temporary save file, so the tests neither read
 * nor overwrite the real task list.
 */
public class ErinaTest {
    @TempDir
    Path tempDir;

    private Path saveFile() {
        return tempDir.resolve("erina.txt");
    }

    private Erina newErina() {
        return new Erina(saveFile());
    }

    @Test
    public void getResponse_help_mentionsEveryCommand() {
        String help = newErina().getResponse("help");

        // The help text is generated from the enum, so every command's usage
        // line must appear, including help itself.
        for (Command command : Command.values()) {
            assertTrue(help.contains(command.getUsage()),
                    "help text is missing: " + command.getUsage());
        }
    }

    @Test
    public void getResponse_unknownCommand_pointsToHelp() {
        String reply = newErina().getResponse("blah");
        assertTrue(reply.contains("help"));
    }

    @Test
    public void getResponse_sameTodoTwice_addsItOnlyOnce() {
        Erina erina = newErina();
        erina.getResponse("todo read book");

        // Different capitalisation is still the same task.
        String reply = erina.getResponse("todo Read Book");

        assertTrue(reply.contains("already"), reply);
        assertFalse(erina.getResponse("list").contains("2."));
    }

    @Test
    public void getResponse_markTaskAlreadyDone_saysSo() {
        Erina erina = newErina();
        erina.getResponse("todo read book");
        erina.getResponse("mark 1");

        assertTrue(erina.getResponse("mark 1").contains("already"));
    }

    @Test
    public void getResponse_listWithExtraText_isRejected() {
        Erina erina = newErina();
        erina.getResponse("todo read book");

        assertFalse(erina.getResponse("list all").contains("read book"));
    }

    @Test
    public void constructor_unreadableSaveFile_startsEmptyAndKeepsBackup() throws IOException {
        Files.writeString(saveFile(), "this is not a task\n");

        Erina erina = newErina();

        assertNotNull(erina.getLoadError());
        Path backup = tempDir.resolve("erina.txt.bak");
        assertEquals("this is not a task\n", Files.readString(backup));
        assertFalse(erina.getResponse("list").contains("1."));
    }
}
