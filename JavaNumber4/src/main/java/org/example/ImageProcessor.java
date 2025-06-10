package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ImageProcessor {
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".bmp");
    private static final AtomicBoolean cancelled = new AtomicBoolean(false);

    public static void main(String[] args) {
        // Проверка аргументов командной строки
        if (args.length < 2 || args.length > 4) {
            printUsage();
            return;
        }

        // Парсинг аргументов
        String sourceDirPath = args[0];
        boolean recursive = false;
        String operation = null;
        double scaleFactor = 0.0;
        String targetDirPath = null;

        for (int i = 1; i < args.length; i++) {
            String arg = args[i].toLowerCase();
            if (arg.equals("/sub")) {
                recursive = true;
            } else if (arg.equals("/s")) {
                if (operation != null) {
                    System.err.println("Ошибка: Указан более одного флага операции.");
                    printUsage();
                    return;
                }
                operation = "/s";
                if (i + 1 >= args.length) {
                    System.err.println("Ошибка: Не указан коэффициент растяжения для /s.");
                    printUsage();
                    return;
                }
                try {
                    scaleFactor = Double.parseDouble(args[++i]);
                    if (scaleFactor <= 0) {
                        System.err.println("Ошибка: Коэффициент растяжения должен быть положительным.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Ошибка: Неверный формат коэффициента растяжения.");
                    return;
                }
            } else if (arg.equals("/n")) {
                if (operation != null) {
                    System.err.println("Ошибка: Указан более одного флага операции.");
                    printUsage();
                    return;
                }
                operation = "/n";
            } else if (arg.equals("/r")) {
                if (operation != null) {
                    System.err.println("Ошибка: Указан более одного флага операции.");
                    printUsage();
                    return;
                }
                operation = "/r";
            } else if (arg.equals("/c")) {
                if (operation != null) {
                    System.err.println("Ошибка: Указан более одного флага операции.");
                    printUsage();
                    return;
                }
                operation = "/c";
                if (i + 1 >= args.length) {
                    System.err.println("Ошибка: Не указан целевой каталог для /c.");
                    printUsage();
                    return;
                }
                targetDirPath = args[++i];
            } else {
                System.err.println("Ошибка: Неизвестный аргумент: " + arg);
                printUsage();
                return;
            }
        }

        if (operation == null) {
            System.err.println("Ошибка: Не указан флаг операции (/s, /n, /r или /c).");
            printUsage();
            return;
        }

        // Проверка исходного каталога
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.isDirectory() || !sourceDir.exists()) {
            System.err.println("Ошибка: Исходный каталог не существует или не является каталогом.");
            return;
        }

        // Проверка целевого каталога для /c
        File targetDir = null;
        if (operation.equals("/c")) {
            targetDir = new File(targetDirPath);
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                System.err.println("Ошибка: Не удалось создать целевой каталог.");
                return;
            }
            if (!targetDir.isDirectory()) {
                System.err.println("Ошибка: Целевой путь не является каталогом.");
                return;
            }
        }

        // Запуск потока для отслеживания Esc
        Thread escListener = new Thread(() -> {
            try {
                while (true) {
                    if (System.in.available() > 0 && System.in.read() == 27) { // Esc = 27
                        cancelled.set(true);
                        System.out.println("Операция отменена пользователем.");
                        break;
                    }
                    Thread.sleep(100);
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("Ошибка при отслеживании Esc: " + e.getMessage());
            }
        });
        escListener.setDaemon(true);
        escListener.start();

        // Обработка каталога
        ExecutorService executor = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors());
        try {
            processDirectory(sourceDir, recursive, operation, scaleFactor, targetDir, executor);
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            System.err.println("Ошибка: Обработка прервана: " + e.getMessage());
        } finally {
            if (!executor.isShutdown()) {
                executor.shutdownNow();
            }
        }
    }

    private static void printUsage() {
        System.err.println("Использование: java ImageProcessor <sourceDir> [/sub] [/s <scaleFactor> | /n | /r | /c <targetDir>]");
        System.err.println("Пример: java ImageProcessor /path/to/source /sub /s 2.0");
        System.err.println("Флаги:");
        System.err.println("  /sub: Рекурсивный обход подкаталогов.");
        System.err.println("  /s: Растянуть изображение (задаётся коэффициент > 0).");
        System.err.println("  /n: Построить негативное изображение.");
        System.err.println("  /r: Удалить файл изображения.");
        System.err.println("  /c: Скопировать изображение в целевой каталог.");
    }

    private static void processDirectory(File dir, boolean recursive, String operation,
                                         double scaleFactor, File targetDir, ExecutorService executor) {
        if (cancelled.get()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (cancelled.get()) break;

            if (file.isDirectory() && recursive) {
                processDirectory(file, recursive, operation, scaleFactor, targetDir, executor);
            } else if (file.isFile() && isImageFile(file)) {
                File finalTargetDir = targetDir; // Для использования в лямбда
                executor.submit(() -> processImage(file, operation, scaleFactor, finalTargetDir));
            }
        }
    }

    private static boolean isImageFile(File file) {
        String extension = getFileExtension(file).toLowerCase();
        return IMAGE_EXTENSIONS.contains(extension);
    }

    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot == -1 ? "" : name.substring(lastDot);
    }

    private static void processImage(File file, String operation, double scaleFactor, File targetDir) {
        if (cancelled.get()) return;

        try {
            switch (operation) {
                case "/s":
                    scaleImage(file, scaleFactor);
                    break;
                case "/n":
                    negateImage(file);
                    break;
                case "/r":
                    if (!file.delete()) {
                        System.err.println("Ошибка: Не удалось удалить файл: " + file.getAbsolutePath());
                    }
                    break;
                case "/c":
                    copyImage(file, targetDir);
                    break;
            }
        } catch (IOException e) {
            System.err.println("Ошибка обработки файла " + file.getAbsolutePath() + ": " + e.getMessage());
        }
    }

    private static void scaleImage(File file, double scaleFactor) throws IOException {
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            System.err.println("Ошибка: Не удалось прочитать изображение: " + file.getAbsolutePath());
            return;
        }
        int newWidth = (int) (image.getWidth() * scaleFactor);
        int newHeight = (int) (image.getHeight() * scaleFactor);
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        ImageIO.write(scaledImage, getFileExtension(file).substring(1), file);
    }

    private static void negateImage(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            System.err.println("Ошибка: Не удалось прочитать изображение: " + file.getAbsolutePath());
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage negatedImage = new BufferedImage(width, height, image.getType());
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);
                int r = 255 - (rgb >> 16 & 0xFF);
                int g = 255 - (rgb >> 8 & 0xFF);
                int b = 255 - (rgb & 0xFF);
                int negatedRgb = (r << 16) | (g << 8) | b;
                negatedImage.setRGB(x, y, negatedRgb);
            }
        }
        ImageIO.write(negatedImage, getFileExtension(file).substring(1), file);
    }

    private static void copyImage(File file, File targetDir) throws IOException {
        Path sourcePath = file.toPath();
        Path targetPath = new File(targetDir, file.getName()).toPath();
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }
}