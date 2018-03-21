angular.module("enums", [])
    .constant("InstrumentStudyType", {
        MS: "Mass Spectrometry",
        MA: "Micro-array",
        NG: "NGS"
    })
    .constant("BillingFeatures", {
        ARCHIVE_STORAGE: "ARCHIVE_STORAGE",
        ANALYSE_STORAGE: "ANALYSE_STORAGE",
        TRANSLATION: "TRANSLATION",
        DOWNLOAD: "DOWNLOAD",
        PROTEIN_ID_SEARCH: "PROTEIN_ID_SEARCH",
        PUBLIC_DOWNLOAD: "PUBLIC_DOWNLOAD",
        PROCESSING: "PROCESSING",
        STORAGE_VOLUMES: "STORAGE_VOLUMES",
        ARCHIVE_STORAGE_VOLUMES: "ARCHIVE_STORAGE_VOLUMES"
    })
    .constant("LabFeatures", {
        PROTEIN_ID_SEARCH: "PROTEIN_ID_SEARCH",
        PROTEIN_ID_SEARCH_RESULTS: "PROTEIN_ID_SEARCH_RESULTS",
        TRANSLATION: "TRANSLATION",
        BILLING: "BILLING",
        MICROARRAYS: "MICROARRAYS",
        EDITABLE_COLUMNS: "EDITABLE_COLUMNS"
    });
