package org.example;

import java.util.*;

public class Polynomial implements Comparable<Polynomial>, Cloneable {
    private HashMap<Integer, Double> coefficients; // Степень -> Коэффициент

    // Конструктор по умолчанию (нулевой полином)
    public Polynomial() {
        coefficients = new HashMap<>();
    }

    // Конструктор с HashMap
    public Polynomial(HashMap<Integer, Double> coefficients) {
        this.coefficients = new HashMap<>();
        for (Map.Entry<Integer, Double> entry : coefficients.entrySet()) {
            if (entry.getValue() != 0.0) { // Пропускаем нулевые коэффициенты
                this.coefficients.put(entry.getKey(), entry.getValue());
            }
        }
    }

    // Конструктор из массива коэффициентов (a[0] + a[1]x + a[2]x^2 + ...)
    public Polynomial(double[] coeffs) {
        coefficients = new HashMap<>();
        for (int i = 0; i < coeffs.length; i++) {
            if (coeffs[i] != 0.0) {
                coefficients.put(i, coeffs[i]);
            }
        }
        if (coefficients.isEmpty()) {
            coefficients.put(0, 0.0); // Нулевой полином
        }
    }

    // Сложение полиномов
    public Polynomial add(Polynomial other) {
        HashMap<Integer, Double> result = new HashMap<>(this.coefficients);
        for (Map.Entry<Integer, Double> entry : other.coefficients.entrySet()) {
            int degree = entry.getKey();
            double coeff = entry.getValue();
            result.put(degree, result.getOrDefault(degree, 0.0) + coeff);
            if (result.get(degree) == 0.0) {
                result.remove(degree); // Удаляем нулевые коэффициенты
            }
        }
        return new Polynomial(result);
    }

    // Вычитание полиномов
    public Polynomial subtract(Polynomial other) {
        HashMap<Integer, Double> result = new HashMap<>(this.coefficients);
        for (Map.Entry<Integer, Double> entry : other.coefficients.entrySet()) {
            int degree = entry.getKey();
            double coeff = entry.getValue();
            result.put(degree, result.getOrDefault(degree, 0.0) - coeff);
            if (result.get(degree) == 0.0) {
                result.remove(degree);
            }
        }
        return new Polynomial(result);
    }

    // Умножение полиномов
    public Polynomial multiply(Polynomial other) {
        HashMap<Integer, Double> result = new HashMap<>();
        for (Map.Entry<Integer, Double> entry1 : this.coefficients.entrySet()) {
            for (Map.Entry<Integer, Double> entry2 : other.coefficients.entrySet()) {
                int degree = entry1.getKey() + entry2.getKey();
                double coeff = entry1.getValue() * entry2.getValue();
                result.put(degree, result.getOrDefault(degree, 0.0) + coeff);
                if (result.get(degree) == 0.0) {
                    result.remove(degree);
                }
            }
        }
        return new Polynomial(result);
    }

    // Умножение на число
    public Polynomial multiply(double scalar) {
        HashMap<Integer, Double> result = new HashMap<>();
        for (Map.Entry<Integer, Double> entry : this.coefficients.entrySet()) {
            double coeff = entry.getValue() * scalar;
            if (coeff != 0.0) {
                result.put(entry.getKey(), coeff);
            }
        }
        return new Polynomial(result);
    }

    // Деление полиномов (возвращает частное)
    public Polynomial divide(Polynomial divisor) {
        if (divisor.isZero()) {
            throw new ArithmeticException("Деление на нулевой полином невозможно");
        }
        Polynomial quotient = new Polynomial();
        Polynomial remainder = new Polynomial(this.coefficients);
        int divisorDegree = divisor.getDegree();
        double divisorLeadCoeff = divisor.coefficients.getOrDefault(divisorDegree, 0.0);

        while (!remainder.isZero() && remainder.getDegree() >= divisorDegree) {
            int degreeDiff = remainder.getDegree() - divisorDegree;
            double coeff = remainder.coefficients.getOrDefault(remainder.getDegree(), 0.0) / divisorLeadCoeff;
            HashMap<Integer, Double> term = new HashMap<>();
            term.put(degreeDiff, coeff);
            Polynomial termPoly = new Polynomial(term);
            quotient = quotient.add(termPoly);
            remainder = remainder.subtract(divisor.multiply(termPoly));
        }
        return quotient;
    }

    // Остаток от деления полиномов
    public Polynomial remainder(Polynomial divisor) {
        if (divisor.isZero()) {
            throw new ArithmeticException("Деление на нулевой полином невозможно");
        }
        Polynomial remainder = new Polynomial(this.coefficients);
        int divisorDegree = divisor.getDegree();
        double divisorLeadCoeff = divisor.coefficients.getOrDefault(divisorDegree, 0.0);

        while (!remainder.isZero() && remainder.getDegree() >= divisorDegree) {
            int degreeDiff = remainder.getDegree() - divisorDegree;
            double coeff = remainder.coefficients.getOrDefault(remainder.getDegree(), 0.0) / divisorLeadCoeff;
            HashMap<Integer, Double> term = new HashMap<>();
            term.put(degreeDiff, coeff);
            Polynomial termPoly = new Polynomial(term);
            remainder = remainder.subtract(divisor.multiply(termPoly));
        }
        return remainder;
    }

    // Проверка, является ли полином нулевым
    private boolean isZero() {
        return coefficients.isEmpty() || (coefficients.size() == 1 && coefficients.getOrDefault(0, 0.0) == 0.0);
    }

    // Получение степени полинома
    public int getDegree() {
        if (isZero()) return 0;
        return coefficients.keySet().stream().max(Integer::compare).orElse(0);
    }

    // Реализация Comparable
    @Override
    public int compareTo(Polynomial other) {
        int thisDegree = this.getDegree();
        int otherDegree = other.getDegree();
        if (thisDegree != otherDegree) {
            return Integer.compare(thisDegree, otherDegree);
        }
        // Сравнение по коэффициентам, начиная с высшей степени
        for (int degree = thisDegree; degree >= 0; degree--) {
            double thisCoeff = this.coefficients.getOrDefault(degree, 0.0);
            double otherCoeff = other.coefficients.getOrDefault(degree, 0.0);
            int cmp = Double.compare(thisCoeff, otherCoeff);
            if (cmp != 0) return cmp;
        }
        return 0;
    }

    // Реализация Cloneable
    @Override
    public Polynomial clone() {
        try {
            Polynomial cloned = (Polynomial) super.clone();
            cloned.coefficients = new HashMap<>(this.coefficients);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Ошибка клонирования", e);
        }
    }

    // Переопределение toString
    @Override
    public String toString() {
        if (isZero()) return "0";
        StringBuilder sb = new StringBuilder();
        List<Integer> degrees = new ArrayList<>(coefficients.keySet());
        Collections.sort(degrees, Collections.reverseOrder()); // Сортировка по убыванию степени
        boolean first = true;
        for (int degree : degrees) {
            double coeff = coefficients.get(degree);
            if (coeff == 0.0) continue;
            // Знак
            if (!first && coeff > 0) sb.append("+");
            if (coeff < 0) sb.append("-");
            // Коэффициент
            double absCoeff = Math.abs(coeff);
            if (Math.abs(coeff) != 1.0 || degree == 0) {
                sb.append(formatNumber(Math.abs(coeff)));
            } else if (coeff == -1.0 && first) {
                sb.append("-");
            }
            // Переменная и степень
            if (degree > 0) {
                sb.append("x");
                if (degree > 1) {
                    sb.append("^").append(degree);
                }
            }
            first = false;
        }
        return sb.length() == 0 ? "0" : sb.toString();
    }

    // Форматирование числа (убираем .0 для целых)
    private String formatNumber(double x) {
        if (x == (long) x) {
            return String.valueOf((long) x);
        }
        return String.valueOf(x);
    }
}