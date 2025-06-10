package org.example;

import java.util.*;

public class MaxNumberPath {
    private static final int SIZE = 3;
    private static String maxNumber = "0"; // Максимальное число как строка

    public static void main(String[] args) {
        // Пример матрицы 3x3 с цифрами от 1 до 9
        int[][] matrix = {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };

        // Проверка корректности матрицы
        if (!isValidMatrix(matrix)) {
            System.err.println("Ошибка: Матрица должна содержать цифры от 1 до 9 без повторений.");
            return;
        }

        // Перебор всех стартовых позиций
        boolean[][] visited = new boolean[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                StringBuilder currentNumber = new StringBuilder();
                dfs(matrix, i, j, visited, currentNumber);
            }
        }

        // Вывод результата
        System.out.println("Максимальное число: " + maxNumber);
    }

    // Проверка валидности матрицы
    private static boolean isValidMatrix(int[][] matrix) {
        if (matrix.length != SIZE || matrix[0].length != SIZE) {
            return false;
        }
        Set<Integer> digits = new HashSet<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int digit = matrix[i][j];
                if (digit < 1 || digit > 9 || !digits.add(digit)) {
                    return false;
                }
            }
        }
        return true;
    }

    // Глубокий поиск для перебора путей
    private static void dfs(int[][] matrix, int i, int j, boolean[][] visited, StringBuilder currentNumber) {
        // Проверка границ и посещения
        if ((i < 0) || (i >= SIZE) || (j < 0)|| (j >= SIZE) || visited[i][j]) {
            return;
        }

        // Добавляем текущую цифру
        visited[i][j] = true;
        currentNumber.append(matrix[i][j]);

        // Если собраны все 9 цифр, сравниваем с максимумом
        if (currentNumber.length() == SIZE * SIZE) {
            String number = currentNumber.toString();
            if (number.compareTo(maxNumber) > 0) {
                maxNumber = number;
            }
        } else {
            // Возможные направления: вверх, вниз, влево, вправо
            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] dir : directions) {
                int ni = i + dir[0];
                int nj = j + dir[1];
                dfs(matrix, ni, nj, visited, currentNumber);
            }
        }

        // Откат (backtracking)
        visited[i][j] = false;
        currentNumber.deleteCharAt(currentNumber.length() - 1);
    }
}