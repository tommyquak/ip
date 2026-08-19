import java.util.Scanner;

/**
 * Entry point of the Erina chatbot.
 *
 * <p>At this stage (Level-1) Erina echoes back whatever the user types,
 * and stops when the user enters the {@value #EXIT_COMMAND} command.
 */
public class Erina {
    /** Horizontal rule used to visually separate each of Erina's replies. */
    private static final String DIVIDER =
            "    ____________________________________________________________";

    /** Command that ends the conversation. */
    private static final String EXIT_COMMAND = "bye";

    public static void main(String[] args) {
        String banner = " _____      _             \n"
                + "| ____|_ __(_)_ __   __ _ \n"
                + "|  _| | '__| | '_ \\ / _` |\n"
                + "| |___| |  | | | | | (_| |\n"
                + "|_____|_|  |_|_| |_|\\__,_|\n";
        System.out.println(banner);

        greet();

        // Read one command per line until the user asks to exit.
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            if (input.equals(EXIT_COMMAND)) {
                break;
            }
            reply(input);
        }

        exit();
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
