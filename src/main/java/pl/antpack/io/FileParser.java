package pl.antpack.io;

import pl.antpack.model.Item;
import pl.antpack.model.KnapsackProblem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileParser {

    private FileParser() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static KnapsackProblem parseKnapsackFile(File file) throws IOException, InvalidFileFormatException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (!file.exists()) {
            throw new IOException("File does not exist: " + file.getAbsolutePath());
        }
        if (!file.canRead()) {
            throw new IOException("File is not readable: " + file.getAbsolutePath());
        }
        if (file.length() == 0) {
            throw new InvalidFileFormatException("File is empty");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int capacity = parseCapacity(reader);
            List<Item> items = parseItems(reader);

            if (items.isEmpty()) {
                throw new InvalidFileFormatException("File must contain at least one item");
            }

            return new KnapsackProblem(capacity, items);
        } catch (NumberFormatException e) {
            throw new InvalidFileFormatException("Invalid numeric value in file", e);
        }
    }

    private static int parseCapacity(BufferedReader reader) throws IOException, InvalidFileFormatException {
        String capacityLine = reader.readLine();

        if (capacityLine == null || capacityLine.trim().isEmpty()) {
            throw new InvalidFileFormatException("Missing capacity value in first line");
        }

        try {
            int capacity = Integer.parseInt(capacityLine.trim());

            if (capacity <= 0) {
                throw new InvalidFileFormatException("Capacity must be a positive integer, got: " + capacity);
            }

            return capacity;
        } catch (NumberFormatException e) {
            throw new InvalidFileFormatException("Invalid capacity format: " + capacityLine, e);
        }
    }

    private static List<Item> parseItems(BufferedReader reader) throws IOException, InvalidFileFormatException {
        List<Item> items = new ArrayList<>();
        String line;
        int lineNumber = 2;

        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                lineNumber++;
                continue;
            }

            Item item = parseItemLine(line, lineNumber, items.size());
            items.add(item);
            lineNumber++;
        }

        return items;
    }

    private static Item parseItemLine(String line, int lineNumber, int itemId) throws InvalidFileFormatException {
        String[] parts = line.trim().split("\\s+");

        if (parts.length != 2) {
            throw new InvalidFileFormatException(
                    String.format("Line %d: Expected 2 values (weight value), got %d: %s",
                            lineNumber, parts.length, line));
        }

        try {
            int weight = Integer.parseInt(parts[0]);
            int value = Integer.parseInt(parts[1]);

            if (weight <= 0) {
                throw new InvalidFileFormatException(
                        String.format("Line %d: Weight must be positive, got: %d", lineNumber, weight));
            }

            if (value <= 0) {
                throw new InvalidFileFormatException(
                        String.format("Line %d: Value must be positive, got: %d", lineNumber, value));
            }

            return new Item(itemId, weight, value);
        } catch (NumberFormatException e) {
            throw new InvalidFileFormatException(
                    String.format("Line %d: Invalid number format: %s", lineNumber, line), e);
        }
    }
}
