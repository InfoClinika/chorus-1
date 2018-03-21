var Confirmation = function(selector, item, options) {
    this.popupSelector = selector;
    this.item = item;
    this.options = options;
};
Confirmation.prototype.showPopup = function() {
    $(this.popupSelector).modal("show");
};
Confirmation.prototype.hidePopup = function () {
    $(this.popupSelector).modal("hide");
};
Confirmation.prototype.getName = function () {
    return this.options.getName();
};
Confirmation.prototype.removeItem = function () {
    this.hidePopup();
    this.options.success(this.item);
};
Confirmation.prototype.ok = function() {
    this.options.success(this.item);
    this.hidePopup();
};


var OperationDialog = function (selector, options) {
    this.popupSelector = selector;
    this.options = options;
    this.model = {};
};
OperationDialog.prototype.showPopup = function() {
    $(this.popupSelector).modal("show");
};
OperationDialog.prototype.hidePopup = function () {
    $(this.popupSelector).modal("hide");
};
OperationDialog.prototype.applyOperation = function () {
    this.hidePopup();
    this.options.success(this.model);
};

function showConfirm(options) {
    $("#"+options.id).html(options.message).dialog({
        title: options.title,
        draggable: false,
        dialogClass: options.dialogClass,
        modal: true,
        resizable: false,
        width: 450,
        buttons: {
            "OK": function () {
                $(this).dialog("close");
                if (options.onOk) options.onOk();
            }
        }
    });
}
