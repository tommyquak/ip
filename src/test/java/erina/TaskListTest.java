package erina;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import erina.task.Task;
import erina.task.Todo;

/**
 * Tests for {@link TaskList}, the list of tasks the user is keeping.
 */
public class TaskListTest {
    @Test
    public void newList_isEmpty() {
        TaskList tasks = new TaskList();
        assertTrue(tasks.isEmpty());
        assertEquals(0, tasks.size());
    }

    @Test
    public void add_task_growsAndKeepsOrder() {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("first"));
        tasks.add(new Todo("second"));

        assertEquals(2, tasks.size());
        assertEquals("first", tasks.get(0).getDescription());
        assertEquals("second", tasks.get(1).getDescription());
    }

    @Test
    public void remove_middleTask_returnsItAndClosesTheGap() {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("first"));
        tasks.add(new Todo("second"));
        tasks.add(new Todo("third"));

        Task removed = tasks.remove(1);

        assertEquals("second", removed.getDescription());
        assertEquals(2, tasks.size());
        // The task after the removed one moves up to fill the gap.
        assertEquals("third", tasks.get(1).getDescription());
    }
}
