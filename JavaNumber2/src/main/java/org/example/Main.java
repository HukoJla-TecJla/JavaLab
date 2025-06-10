package org.example;

public class Main {
    public static void main(String[] args) {
        // Создание полиномов: p1 = 2x^2 + 3x + 1, p2 = x^2 - 2
        Polynomial p1 = new Polynomial(new double[]{1, 3, 2});
        Polynomial p2 = new Polynomial(new double[]{-2, 0, 1});
        System.out.println("p1 = " + p1); // 2x^2 + 3x + 1
        System.out.println("p2 = " + p2); // x^2 - 2

        // Сложение
        System.out.println("p1 + p2 = " + p1.add(p2)); // 3x^2 + 3x - 1

        // Вычитание
        System.out.println("p1 - p2 = " + p1.subtract(p2)); // x^2 + 3x + 3

        // Умножение
        System.out.println("p1 * p2 = " + p1.multiply(p2)); // 2x^4 + 3x^3 - x^2 - 6x - 2

        // Умножение на число
        System.out.println("p1 * 2 = " + p1.multiply(2)); // 4x^2 + 6x + 2

        // Деление
        System.out.println("p1 / p2 = " + p1.divide(p2)); // 2
        System.out.println("p1 % p2 = " + p1.remainder(p2)); // 3x + 5

        // Сравнение
        System.out.println("p1 > p2: " + (p1.compareTo(p2) > 0)); // true (степень одинакова, сравниваются коэффициенты)

        // Клонирование
        Polynomial p1Clone = p1.clone();
        System.out.println("Клон p1 = " + p1Clone); // 2x^2 + 3x + 1
    }
}