package com.yummybook.spring.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Utils {

    public static Optional<List<Integer>> getNumbersOfPage(int totalPages, int currentPage) {
        List<Integer> pageNumbers = null;
        if (totalPages > 0 && totalPages <= 10) {
            pageNumbers = IntStream.rangeClosed(0, totalPages - 1).boxed().collect(Collectors.toList());
        } else if (totalPages > 10 && currentPage >= 4) {
            pageNumbers = IntStream.rangeClosed(currentPage - 4, currentPage + 5).boxed().collect(Collectors.toList());
        } else if (totalPages > 10) {
            pageNumbers = IntStream.rangeClosed(0, 9).boxed().collect(Collectors.toList());
        }
        return Optional.of(pageNumbers);
    }

    public static long calcAverageRating(long totalRating, long totalVoteCount) {
        if (totalRating == 0 || totalVoteCount == 0) {
            return 0;
        }
        return totalRating / totalVoteCount;
    }
}
