package de.gtopcu.imagesorting;

import java.util.Scanner;
import java.util.function.Consumer;

public final class ContinuousConsoleReader {
    private ContinuousConsoleReader() {
    }

    private static final String PROMPT_PREFIX = "> ";

    public static void start(Consumer<String> consumer) {
        new Thread(() -> {
            final Scanner scanner = new Scanner(System.in);
            System.out.print(PROMPT_PREFIX);
            while (scanner.hasNext()) {
                consumer.accept(scanner.nextLine());
                System.out.print(PROMPT_PREFIX);
            }
        }).start();
    }
}
