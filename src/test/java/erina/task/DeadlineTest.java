package erina.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Deadline}, focusing on how its date is shown and saved.
 *
 * <p>The two formats are deliberately different: the display form is for
 * people, the save form must round-trip through {@code LocalDate.parse}.
 */
public class DeadlineTest {
    @Test
    public void toString_showsDateInReadableForm() {
        Deadline deadline = new Deadline("return book", LocalDate.of(2019, 10, 15));
        assertEquals("[D][ ] return book (by: Oct 15 2019)", deadline.toString());
    }

    @Test
    public void toSaveString_keepsIsoDateAndDoneFlag() {
        Deadline deadline = new Deadline("return book", LocalDate.of(2019, 10, 15));
        deadline.markAsDone();
        assertEquals("D | 1 | return book | 2019-10-15", deadline.toSaveString());
    }
}
