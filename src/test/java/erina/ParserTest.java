package erina;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import erina.task.Deadline;
import erina.task.Todo;

/**
 * Tests for {@link Parser}, which turns user-typed text into objects.
 *
 * <p>Parser is a natural unit-testing target: every method is a pure
 * function from a string to a value or an error, with no state to set up.
 */
public class ParserTest {
    @Test
    public void parseTodo_normalDescription_returnsTodo() throws ErinaException {
        Todo todo = Parser.parseTodo("read book");
        assertEquals("[T][ ] read book", todo.toString());
    }

    @Test
    public void parseTodo_emptyDescription_throws() {
        ErinaException e = assertThrows(ErinaException.class, () -> Parser.parseTodo(""));
        assertEquals("OOPS!!! The description of a todo cannot be empty.", e.getMessage());
    }

    @Test
    public void parseDeadline_descriptionAndDate_returnsDeadline() throws ErinaException {
        Deadline deadline = Parser.parseDeadline("return book /by 2019-10-15");
        assertEquals("[D][ ] return book (by: Oct 15 2019)", deadline.toString());
    }

    @Test
    public void parseDeadline_missingByPart_throws() {
        assertThrows(ErinaException.class, () -> Parser.parseDeadline("return book"));
    }

    @Test
    public void parseDeadline_textInsteadOfDate_throws() {
        assertThrows(ErinaException.class, () -> Parser.parseDeadline("return book /by Sunday"));
    }

    @Test
    public void parseDate_isoText_returnsThatDate() throws ErinaException {
        assertEquals(LocalDate.of(2019, 10, 15), Parser.parseDate("2019-10-15"));
    }

    @Test
    public void parseDate_impossibleDate_throws() {
        assertThrows(ErinaException.class, () -> Parser.parseDate("2019-13-40"));
    }

    @Test
    public void parseIndex_numberInRange_returnsZeroBasedIndex() throws ErinaException {
        // The user's 2 refers to the second task, which is index 1 internally.
        assertEquals(1, Parser.parseIndex("2", 3));
    }

    @Test
    public void parseIndex_notANumber_throws() {
        ErinaException e = assertThrows(ErinaException.class, () -> Parser.parseIndex("abc", 3));
        assertEquals("OOPS!!! \"abc\" is not a task number.", e.getMessage());
    }

    @Test
    public void parseIndex_numberOutOfRange_throws() {
        assertThrows(ErinaException.class, () -> Parser.parseIndex("4", 3));
    }

    @Test
    public void parseIndex_emptyArgument_throws() {
        assertThrows(ErinaException.class, () -> Parser.parseIndex("", 3));
    }
}
