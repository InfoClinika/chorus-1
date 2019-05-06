package com.infoclinika.mssharing.model.internal.entity.restorable;

import java.util.stream.Stream;

/**
 * @author Vitalii Petkanych
 */
public enum LibraryPrep {
    UNDEFINED("Undefined"),
    SCRIPTSEQ("Scriptseq"),
    NEXTERA("Nextera"),
    TRUSEQ("TruSeq SBS v4");

    private final String title;

    LibraryPrep(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static LibraryPrep getByTitle(String title) {
        return Stream.of(values())
            .filter(v -> title.equalsIgnoreCase(v.title))
            .findFirst()
            .orElse(UNDEFINED);
    }
}
