package com.infoclinika.mssharing.wizard.upload.gui.swing.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author timofey.kasyanov
 *         date:   05.02.14
 */
public class FilesDropTarget extends DropTarget {

    private final FilesDropListener dropListener;

    public FilesDropTarget(FilesDropListener dropListener) {

        checkNotNull(dropListener);

        this.dropListener = dropListener;
    }

    @Override
    public synchronized void drop(DropTargetDropEvent event) {

        event.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

        final Transferable transferable = event.getTransferable();

        try {

            final List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);

            dropListener.filesDropped(files);

        } catch (UnsupportedFlavorException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    public static interface FilesDropListener {

        void filesDropped(List<File> files);

    }

}
