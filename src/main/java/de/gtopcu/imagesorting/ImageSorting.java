package de.gtopcu.imagesorting;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class ImageSorting {
    private static final String NAME_PREFIX = "IMG_%s";
    private static final String NOT_IMAGE_FILES_DIRECTORY_NAME = "NOT_IMAGE_FILES";

    private ImageSorting() {
    }

    public static void main(String[] args) {
        final File file = new File(System.getProperty("user.dir"));
        System.out.println("[ImageSorting] Are you sure that you want sort the images inside this directory:");
        System.out.println("[ImageSorting] '" + file.getAbsolutePath() + "'");
        System.out.println("[ImageSorting] Start the sorting by pressing [y/N].");
        ContinuousConsoleReader.start(input -> {
            try {
                if (input.equalsIgnoreCase("y")) {
                    final int totalRenaming = doSortFiles(file);
                    System.out.printf("[ImageSorting] Renamed '%s' files%n", totalRenaming);
                } else if (input.equalsIgnoreCase("n")) {
                    System.out.println("[ImageSorting] You successfully terminated the application");
                } else {
                    System.out.println("[ImageSorting] Press 'y' to start sorting or 'N' to terminate the application.");
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    private static int doSortFiles(File directory) throws IOException {
        if (directory.getName().contains(NOT_IMAGE_FILES_DIRECTORY_NAME)) {
            return 0;
        }
        final File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            return 0;
        }
        int renaming = 0;
        final List<File> images = new ArrayList<>();
        for (final File file : files) {
            if (file.isDirectory()) {
                renaming += doSortFiles(file);
                continue;
            }
            if (!isImage(file)) {
                doNotImageFile(directory, file);
                continue;
            }
            images.add(file);
        }
        for (int i = 0; i < images.size(); i++) {
            final File before = images.get(i);
            final File after = doRenameFile(before, UUID.randomUUID().toString()).toFile();
            images.set(i, after);
        }
        images.sort(FileComparator.INSTANCE);
        for (int i = 0, j = i + 1; i < images.size(); i++, j++) {
            final File before = images.get(i);
            final String name = String.format(NAME_PREFIX, j);
            doRenameFile(before, name);
            renaming++;
            System.out.printf("[ImageSorting] Renamed '%s' to '%s'%n", before.getName(), name);
        }
        return renaming;
    }

    private static void doNotImageFile(File directory, File file) throws IOException {
        final Path path = Paths.get(directory.toPath().toString(), NOT_IMAGE_FILES_DIRECTORY_NAME);
        if (Files.notExists(path)) {
            Files.createDirectory(path);
        }
        Files.move(file.toPath(), Paths.get(path.toString(), file.getName()));
    }

    private static boolean isImage(File file) throws IOException {
        return ImageIO.read(file) != null;
    }

    private static String findFileExtension(File file) {
        final String name = file.getName();
        if (!name.contains(".")) {
            return name;
        }
        final int index = name.lastIndexOf(".") + 1;
        if (index >= name.length()) {
            return name;
        }
        return name.substring(index);
    }

    private static Path doRenameFile(File file, String name) throws IOException {
        final String extension = findFileExtension(file);
        final String result = name + "." + extension;
        final Path from = file.toPath();
        final Path destination = from.resolveSibling(result);
        return Files.move(from, destination);
    }

    private static class FileComparator implements Comparator<File> {
        public static final FileComparator INSTANCE = new FileComparator();

        private FileComparator() {
        }

        @Override
        public int compare(File o1, File o2) {
            try {
                return findCreationTime(o1).compareTo(findCreationTime(o2));
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        }

        private FileTime findCreationTime(File file) throws IOException {
            return Files.readAttributes(file.toPath(), BasicFileAttributes.class).creationTime();
        }
    }
}
