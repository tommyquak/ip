/**
 * Entry point of the Erina chatbot.
 *
 * <p>At this stage (Level-0) Erina only greets the user and exits; it does not
 * yet read any input.
 */
public class Erina {
    /** Horizontal rule used to visually separate each of Erina's replies. */
    private static final String DIVIDER =
            "    ____________________________________________________________";

    public static void main(String[] args) {
        String banner = " _____      _             \n"
                + "| ____|_ __(_)_ __   __ _ \n"
                + "|  _| | '__| | '_ \\ / _` |\n"
                + "| |___| |  | | | | | (_| |\n"
                + "|_____|_|  |_|_| |_|\\__,_|\n";
        System.out.println(banner);

        greet();
        exit();
    }

    /** Prints Erina's opening message. */
    private static void greet() {
        System.out.println(DIVIDER);
        System.out.println("     Hello! I'm Erina");
        System.out.println("     What can I do for you?");
        System.out.println(DIVIDER);
    }

    /** Prints Erina's farewell message, shown just before the program ends. */
    private static void exit() {
        System.out.println(DIVIDER);
        System.out.println("     Bye. Hope to see you again soon!");
        System.out.println(DIVIDER);
    }
}
