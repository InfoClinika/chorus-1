package com.infoclinika.mssharing.platform.repository;

import com.infoclinika.mssharing.platform.entity.Dictionary;

/**
 * @author Herman Zamula
 */
public class DictionaryRepoItem extends Dictionary {

    public DictionaryRepoItem(long id, String name) {
        setId(id);
        this.setName(name);
    }
}
