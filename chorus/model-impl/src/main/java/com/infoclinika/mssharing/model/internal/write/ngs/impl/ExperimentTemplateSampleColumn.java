package com.infoclinika.mssharing.model.internal.write.ngs.impl;

/**
 * @author timofei.kasianov 8/3/18
 */
public enum ExperimentTemplateSampleColumn {

    VENDOR("vendor"),
    VENDOR_ID("vendor_id"),
    VENDOR_PROJECT_NAME("vendor_project_name"),
    CELGENE_ID("celgene_id"),
    DA_PROJECT_ID("DA_project_id"),
    CELGENE_PROJECT_DESC("celgene_project_desc"),
    EXPERIMENT("experiment"),
    DISPLAY_NAME("display_name"),
    DISPLAY_NAME_SHORT("display_name_short"),
    CELL_TYPE("cell_type"),
    CELL_LINE("cell_line"),
    TISSUE("tissue"),
    CONDITION("condition"),
    XENOGRAFT("xenograft"),
    TIME_TREATMENT("time_treatment"),
    RESPONSE_DESC("response_desc"),
    RESPONSE("response"),
    COMPOUND("compound"),
    DOSE("dose"),
    BIOLOGICAL_REPLICATES_GROUP("biological_replicates_group"),
    TECHNICAL_REPLICATES_GROUP("technical_replicates_group"),
    EXPERIMENT_TYPE("experiment_type"),
    TECHNOLOGY("technology"),
    LIBRARY_PREP("library_prep"),
    EXOME_BAIT_SET("exome_bait_set"),
    RNA_SELECTION("rna_selection"),
    NT_EXTRACTION("nt_extraction"),
    ANTIBODY_TARGET("antibody_target"),
    REFERENCE_GENOME("reference_genome"),
    HOST_GENOME("host_genome"),
    STRANDED("stranded"),
    PAIRED_END("paired_end"),
    FILENAME("filename");

    private final String name;

    ExperimentTemplateSampleColumn(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
