package com.infoclinika.mssharing.wizard.messages;

/**
 * @author timofey.kasyanov
 *         date: 26.03.14.
 */
public enum MessageKey {
    MAIN_TITLE("main.title"),
    MAIN_BUTTON_NEXT("main.button.next"),
    MAIN_BUTTON_BACK("main.button.back"),
    MAIN_BUTTON_RESET("main.button.reset"),
    MAIN_BUTTON_UPLOAD("main.button.upload"),
    MAIN_BUTTON_START_NEW("main.button.start.new"),
    MAIN_BUTTON_CANCEL("main.button.cancel"),

    LOGIN_TITLE("login.title"),
    LOGIN_LABEL_EMAIL("login.label.email"),
    LOGIN_LABEL_PASSWORD("login.label.password"),
    LOGIN_LABEL_TOKEN("login.label.token"),
    LOGIN_BUTTON_SIGN_IN("login.button.sign.in"),

    MODALS_CONFIRM_TITLE("modals.confirm.title"),
    MODALS_ERROR_TITLE("modals.error.title"),
    MODALS_WARNING_TITLE("modals.warning.title"),
    MODALS_CANCEL_UPLOAD_TEXT("modals.cancel.upload.text"),
    MODALS_NO_INSTRUMENTS_TEXT("modals.no.instruments.text"),
    MODALS_FILES_FILTERED_TEXT("modals.files.filtered.text"),
    MODALS_NO_VENDOR_TEXT("modals.no.vendor.text"),
    MODALS_NO_INSTRUMENT_MODEL("modals.no.instrument.model"),
    MODALS_GETTING_FILES_TEXT("modals.getting.files.text"),

    TABLE_COLUMN_NAME("table.column.name"),
    TABLE_COLUMN_SIZE("table.column.size"),
    TABLE_COLUMN_SPECIE("table.column.specie"),
    TABLE_COLUMN_LABELS("table.column.labels"),
    TABLE_COLUMN_ZIP("table.column.zip"),
    TABLE_COLUMN_UPLOAD("table.column.upload"),
    TABLE_COLUMN_SPEED("table.column.speed"),

    ONE_LABEL_TECHNOLOGY_TYPE("one.label.technology.type"),
    ONE_LABEL_VENDOR("one.label.vendor"),
    ONE_LABEL_lAB("one.label.lab"),
    ONE_LABEL_INSTRUMENT("one.label.instrument"),
    ONE_LABEL_SPECIE("one.label.specie"),
    ONE_COMBO_SELECT_ONE("one.combo.select.one"),
    ONE_DESCRIPTION("one.description"),

    TWO_LABEL_NOTE("two.label.note"),
    TWO_BUTTON_BROWSE("two.button.browse"),
    TWO_BUTTON_REMOVE("two.button.remove"),
    TWO_DESCRIPTION("two.description"),

    THREE_LABEL_NOTE_ONE("three.label.note.one"),
    THREE_LABEL_NOTE_TWO("three.label.note.two"),

    FOUR_DESCRIPTION("four.description"),

    APP_ERROR_SERVER_NOT_RESPONDING("app.error.server.not.responding"),
    APP_ERROR_BAD_CREDENTIALS("app.error.bad.credentials"),
    APP_ERROR_EMPTY_CREDENTIALS("app.error.empty.credentials"),
    APP_ERROR_EMPTY_TOKEN("app.error.empty.token"),
    APP_ERROR_UPLOAD_LIMIT_EXCEEDED("app.error.upload.limit.exceeded"),

    ITEM_STATUS_WAIT("item.status.wait"),
    ITEM_STATUS_DONE("item.status.done"),
    ITEM_STATUS_ZIP("item.status.zip"),
    ITEM_STATUS_DUPLICATE("item.status.duplicate"),
    ITEM_STATUS_ERROR("item.status.error"),
    ITEM_STATUS_FINISH("item.status.finish"),
    ITEM_STATUS_UNAVAILABLE("item.status.unavailable");

    private final String key;

    MessageKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
