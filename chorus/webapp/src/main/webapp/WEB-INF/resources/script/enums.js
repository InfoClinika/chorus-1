"use strict";

(function () {

    angular.module("enums", [])
        .constant("InstrumentStudyType", {
            MS: "Mass Spectrometry",
            MA: "Micro-array",
            NGS: "Next-Gen Sequencing"
        })
        .constant("BillingFeatures", {
            ARCHIVE_STORAGE: "ARCHIVE_STORAGE",
            ANALYSE_STORAGE: "ANALYSE_STORAGE",
            DOWNLOAD: "DOWNLOAD",
            PUBLIC_DOWNLOAD: "PUBLIC_DOWNLOAD",
            STORAGE_VOLUMES: "STORAGE_VOLUMES",
            ARCHIVE_STORAGE_VOLUMES: "ARCHIVE_STORAGE_VOLUMES"
        })
        .constant("LabFeatures", {
            BILLING: "BILLING",
            EDITABLE_COLUMNS: "EDITABLE_COLUMNS",
            LTQ: "LTQ",
            PARSE_RULES_SELECTION: "PARSE_RULES_SELECTION"
        });

})();
