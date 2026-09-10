package erina;

import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private Erina newErina() {
        return new Erina(tempDir.resolve("erina.txt"));
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
}
