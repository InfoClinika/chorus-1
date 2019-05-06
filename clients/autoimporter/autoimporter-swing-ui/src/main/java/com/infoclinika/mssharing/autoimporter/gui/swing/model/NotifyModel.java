package com.infoclinika.mssharing.autoimporter.gui.swing.model;

import java.util.List;

/**
 * @author timofey.kasyanov
 *     date:   21.01.14
 */
public interface NotifyModel<T> {

    void notifyInit(List<T> items);

    void notifyAdd(T o);

    void notifyRemove(T o);

}
