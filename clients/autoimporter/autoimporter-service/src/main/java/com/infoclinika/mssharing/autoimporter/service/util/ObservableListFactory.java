package com.infoclinika.mssharing.autoimporter.service.util;

import com.infoclinika.mssharing.autoimporter.model.bean.DuplicateItem;
import com.infoclinika.mssharing.autoimporter.model.bean.UploadItem;
import com.infoclinika.mssharing.autoimporter.model.bean.WaitItem;
import com.infoclinika.mssharing.autoimporter.service.api.ObservableList;
import com.infoclinika.mssharing.autoimporter.service.api.internal.ObserverList;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.inject.Inject;

/**
 * @author Ruslan Duboveckij
 */
public abstract class ObservableListFactory {

    @Inject
    @Qualifier("observerListImpl")
    private ObserverList<WaitItem> waitObserverList;

    @Inject
    @Qualifier("observerUploadListImpl")
    private ObserverList<UploadItem> observerUploadList;

    @Inject
    @Qualifier("observerDuplicateListImpl")
    private ObserverList<DuplicateItem> duplicateObserverList;

    protected abstract ObservableList<WaitItem> createObservableWaitList();

    protected abstract ObservableList<UploadItem> createObservableUploadList();

    protected abstract ObservableList<DuplicateItem> createObservableDuplicateList();

    public ObservableList<WaitItem> newObservableWaitList(String folder) {
        final ObservableList<WaitItem> observableWaitList = createObservableWaitList();
        observableWaitList.init(folder);
        observableWaitList.setObserver(waitObserverList);
        return observableWaitList;
    }

    public ObservableList<UploadItem> newObservableUploadList(String folder) {
        final ObservableList<UploadItem> observableUploadList = createObservableUploadList();
        observableUploadList.init(folder);
        observableUploadList.setObserver(observerUploadList);
        return observableUploadList;
    }

    public ObservableList<DuplicateItem> newObservableDuplicateList(String folder) {
        final ObservableList<DuplicateItem> observableDuplicateList = createObservableDuplicateList();
        observableDuplicateList.init(folder);
        observableDuplicateList.setObserver(duplicateObserverList);
        return observableDuplicateList;
    }

}
