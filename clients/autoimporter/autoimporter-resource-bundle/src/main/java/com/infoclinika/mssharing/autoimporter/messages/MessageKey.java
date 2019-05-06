package com.infoclinika.mssharing.autoimporter.messages;

/**
 * @author timofey.kasyanov
 *     date: 26.03.14.
 */
public enum MessageKey {
    MAIN_TITLE("main.title"),
    MAIN_LABEL_CONFIG_LIST("main.label.config.list"),
    MAIN_LABEL_LOGGED_IN("main.label.logged.in"),
    MAIN_LABEL_CURRENT_CONFIG("main.label.current.config"),
    MAIN_LABEL_TOTAL_CONFIGS("main.label.total.configs"),
    MAIN_LABEL_TOTAL_STARTED("main.label.total.started"),
    MAIN_LABEL_FOLDER("main.label.folder"),
    MAIN_LABEL_INSTRUMENT("main.label.instrument"),
    MAIN_LABEL_LABELS("main.label.labels"),
    MAIN_LABEL_COMPLETE_ACTION("main.label.complete.action"),
    MAIN_LABEL_COMPLETE_ACTION_NOTHING("main.label.complete.action.nothing"),
    MAIN_LABEL_COMPLETE_ACTION_DELETE_FILES("main.label.complete.action.delete.files"),
    MAIN_LABEL_COMPLETE_ACTION_MOVE_FILES("main.label.complete.action.move.files"),
    MAIN_TOOLTIP_BUTTON_ADD_CONFIG("main.tooltip.button.add.config"),
    MAIN_TOOLTIP_BUTTON_REMOVE_CONFIG("main.tooltip.button.remove.config"),
    MAIN_TOOLTIP_BUTTON_RUN_CONFIG("main.tooltip.button.run.config"),
    MAIN_TOOLTIP_BUTTON_STOP_CONFIG("main.tooltip.button.stop.config"),
    MAIN_TOOLTIP_BUTTON_COPY_TO_CLIPBOARD("main.tooltip.button.copy.to.clipboard"),
    MAIN_BUTTON_SIGN_OUT("main.button.sign.out"),
    MAIN_TAB_UPLOADING("main.tab.uploading"),
    MAIN_TAB_WAITING("main.tab.waiting"),
    MAIN_TAB_DUPLICATES("main.tab.duplicates"),
    MAIN_TABLE_COLUMN_NAME("main.table.column.name"),
    MAIN_TABLE_COLUMN_SIZE("main.table.column.size"),
    MAIN_TABLE_COLUMN_ZIP("main.table.column.zip"),
    MAIN_TABLE_COLUMN_UPLOAD("main.table.column.upload"),
    MAIN_TABLE_COLUMN_SPEED("main.table.column.speed"),
    MAIN_TABLE_UPLOAD_STATUS_WAITING("main.table.upload.status.waiting"),
    MAIN_TABLE_UPLOAD_STATUS_DONE("main.table.upload.status.done"),
    MAIN_TABLE_UPLOAD_STATUS_ERROR("main.table.upload.status.error"),
    MAIN_TABLE_UPLOAD_STATUS_DUPLICATE("main.table.upload.status.duplicate"),
    MAIN_TABLE_UPLOAD_STATUS_UNAVAILABLE("main.table.upload.status.unavailable"),
    MAIN_TABLE_UPLOAD_STATUS_ZIPPING("main.table.upload.status.zipping"),
    MAIN_TABLE_UPLOAD_STATUS_FINISHING("main.table.upload.status.finishing"),
    MAIN_TABLE_UPLOAD_STATUS_RETRYING("main.table.upload.status.retrying"),
    MAIN_TABLE_UPLOAD_STATUS_CANCELED("main.table.upload.status.canceled"),
    MAIN_TABLE_UPLOAD_STATUS_SIZE_MISMATCH("main.table.upload.status.size.mismatch"),

    CONFIG_TITLE("config.title"),
    CONFIG_LABEL_FOLDER("config.label.folder"),
    CONFIG_LABEL_NAME("config.label.name"),
    CONFIG_LABEL_TECHNOLOGY_TYPE("config.label.technology.type"),
    CONFIG_LABEL_VENDOR("config.label.vendor"),
    CONFIG_LABEL_LABS("config.label.lab"),
    CONFIG_LABEL_INSTRUMENT("config.label.instrument"),
    CONFIG_LABEL_LABELS("config.label.labels"),
    CONFIG_LABEL_COMPLETE_ACTION("config.label.complete.action"),
    CONFIG_LABEL_ACTION_NOTHING("config.label.action.nothing"),
    CONFIG_LABEL_ACTION_DELETE_FILES("config.label.action.delete.files"),
    CONFIG_LABEL_ACTION_MOVE_FILES("config.label.action.move.files"),
    CONFIG_LABEL_SPECIFY_FOLDER("config.label.specify.folder"),
    CONFIG_LABEL_FOLDER_ERROR_EMPTY("config.label.folder.error.empty"),
    CONFIG_LABEL_FOLDER_ERROR_EXISTS("config.label.folder.error.exists"),
    CONFIG_LABEL_NAME_ERROR_EMPTY("config.label.name.error.empty"),
    CONFIG_LABEL_NAME_ERROR_EXISTS("config.label.name.error.exists"),
    CONFIG_LABEL_TECHNOLOGY_TYPE_ERROR_EMPTY("config.label.technology.type.error.empty"),
    CONFIG_LABEL_VENDOR_ERROR_EMPTY("config.label.vendor.error.empty"),
    CONFIG_LABEL_LAB_ERROR_EMPTY("config.label.lab.error.empty"),
    CONFIG_LABEL_INSTRUMENT_ERROR_EMPTY("config.label.instrument.error.empty"),
    CONFIG_LABEL_SPECIFY_FOLDER_ERROR_EMPTY("config.label.specify.folder.error.empty"),
    CONFIG_BUTTON_OK("config.button.ok"),
    CONFIG_BUTTON_CANCEL("config.button.cancel"),
    CONFIG_BUTTON_BROWSE("config.button.browse"),
    CONFIG_COMBO_SELECT_ONE("config.combo.one"),
    CONFIG_FILE_FILTER_FOLDERS_ONLY("config.file.filter.folders.only"),

    LOGIN_TITLE("login.title"),
    LOGIN_LABEL_EMAIL("login.label.email"),
    LOGIN_LABEL_PASSWORD("login.label.password"),
    LOGIN_LABEL_TOKEN("login.label.token"),
    LOGIN_BUTTON_SIGN_IN("login.button.sign.in"),

    MODALS_CONFIRM_TITLE("modals.confirm.title"),
    MODALS_ERROR_TITLE("modals.error.title"),
    MODALS_WARNING_TITLE("modals.warning.title"),
    MODALS_DELETE_CONFIG_TEXT("modals.delete.config.text"),
    MODALS_THERMO_UNAVAILABLE_TEXT("modals.thermo.unavailable.text"),
    MODALS_RUN_NOT_EMPTY_CONFIG_TEXT("modals.run.not.empty.config.text"),
    MODALS_LOGOUT_TEXT("modals.logout.text"),
    MODALS_CREATE_FOLDER_TEXT("modals.create.folder.text"),
    MODALS_CANNOT_CREATE_FOLDER_TEXT("modals.cannot.create.folder.text"),
    MODALS_NOT_INSTRUMENT_MODEL_TEXT("modals.no.instrument.model"),

    APP_ERROR_BAD_CREDENTIALS("app.error.bad.credentials"),
    APP_ERROR_SERVER_IS_NOT_RESPONDING("app.error.server.is.not.responding"),
    APP_ERROR_COMMON("app.error.common"),
    APP_ERROR_UPLOAD_LIMIT_EXCEEDED("app.error.upload.limit.exceeded");

    private final String key;

    MessageKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
