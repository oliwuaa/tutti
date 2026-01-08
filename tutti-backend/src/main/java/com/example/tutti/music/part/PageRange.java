package com.example.tutti.music.part;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class PageRange {
    private Integer pageStart;
    private Integer pageEnd;

    public static PageRange of(Integer start, Integer end) {
        if (start == null || end == null || start <= 0 || end < start) {
            throw new IllegalArgumentException("Nieprawidłowy zakres stron: " + start + "-" + end);
        }
        return new PageRange(start, end);
    }

    public int getPageCount() {
        return pageEnd - pageStart + 1;
    }
}