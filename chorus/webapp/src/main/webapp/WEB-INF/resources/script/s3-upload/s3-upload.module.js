"use strict";
(function () {

    agGrid.initialiseAgGridWithAngular1(angular);

    angular.module("s3UploadModule", ["dialogs", "formatters", "agGrid", "experiments-back"]);

})();
