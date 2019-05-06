package com.infoclinika.mssharing.platform.fileserver.impl;

import com.infoclinika.mssharing.platform.fileserver.StorageService;
import com.infoclinika.mssharing.platform.fileserver.StoredObject;
import com.infoclinika.mssharing.platform.fileserver.model.NodePath;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

/**
 * @author Pavel Kaplin
 */
public class InMemoryStorageService implements StorageService {
    private Map<NodePath, StoredObject> storage = newHashMap();

    public InMemoryStorageService() {
    }

    public void put(NodePath path, StoredObject object) {
        checkStorage();
        storage.put(path, object);
    }

    public StoredObject get(NodePath path) {
        checkStorage();
        return checkNotNull(storage.get(path), "wrong path");
    }

    @Override
    public void delete(NodePath path) {
        checkStorage();
        storage.remove(path);
    }

    //Dirty hack for tests (Mockito does not initialize fields properly)
    private void checkStorage() {
        if (storage == null) {
            storage = newHashMap();
        }
    }
}
