package com.infoclinika.mssharing.clients.common.dto;

import com.infoclinika.mssharing.dto.response.DictionaryDTO;

/**
 * @author timofey.kasyanov
 *     date:   28.01.14
 */
public class DictionaryWrapper {
    private final DictionaryDTO dictionary;

    public DictionaryWrapper(DictionaryDTO dictionary) {
        this.dictionary = dictionary;
    }

    public DictionaryDTO getDictionary() {
        return dictionary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DictionaryWrapper wrapper = (DictionaryWrapper) o;

        if (!dictionary.getName().equals(wrapper.dictionary.getName())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return dictionary.getName().hashCode();
    }

    @Override
    public String toString() {
        return dictionary.getName();
    }

}
