package com.infoclinika.mssharing.desktop.upload.service.api.list;

/**
 * @author timofey.kasyanov
 *     date:   27.01.14
 */
public interface ListListener<T> {

    void onAdd(T item);

    void onRemove(T item);

    void onChange(T item, Object params);

    void onClear();

}
