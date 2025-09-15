package com.example.datagath.util;

import java.util.*;
import java.util.stream.*;

public class Stats {

    public static double mean(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    public static double median(List<Double> values) {
        if (values.isEmpty())
            return 0.0;
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int n = sorted.size();
        if (n % 2 == 0) {
            return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
        } else {
            return sorted.get(n / 2);
        }
    }

    public static double stdDev(List<Double> values) {
        if (values.isEmpty())
            return 0.0;
        double mean = mean(values);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }

    public static List<Double> zScores(List<Double> values) {
        double mean = mean(values);
        double std = stdDev(values);
        if (std == 0.0) {
            return values.stream().map(v -> 0.0).toList(); // avoid div/0
        }
        return values.stream()
                .map(v -> (v - mean) / std)
                .toList();
    }

    public static double mode(List<Double> values) {
        if (values.isEmpty())
            return 0.0;
        Map<Double, Long> freq = values.stream()
                .collect(Collectors.groupingBy(v -> v, Collectors.counting()));

        return freq.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get()
                .getKey();
    }

    public static Map<String, String> top10Strings(List<String> values) {
        // Count frequency of each string
        Map<String, Long> frequencyMap = values.stream()
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        // Sort by count descending, take top 10, and collect into Map<String, String>
        return frequencyMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> String.valueOf(e.getValue()),
                        (e1, e2) -> e1, // in case of duplicates (won't happen here)
                        LinkedHashMap::new // preserve order
                ));
    }
}
