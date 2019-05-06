package com.infoclinika.mssharing.model.internal.write.ngs.impl;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.infoclinika.mssharing.model.internal.write.ngs.NgsExperimentTemplateParseException;
import com.infoclinika.mssharing.model.internal.write.ngs.api.NgsExperimentTemplateParser;
import com.infoclinika.mssharing.model.internal.write.ngs.api.dto.NgsExperimentTemplateData;
import com.infoclinika.mssharing.model.internal.write.ngs.api.dto.NgsExperimentTemplateSampleData;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.infoclinika.mssharing.model.internal.write.ngs.impl.ExperimentTemplateSampleColumn.*;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author timofei.kasianov 8/2/18
 */
@Component
public class NgsExperimentTemplateParserImpl implements NgsExperimentTemplateParser {

    private static final Set<String> HEADER_COLUMN_NAMES = Arrays
        .stream(ExperimentTemplateSampleColumn.values())
        .map(ExperimentTemplateSampleColumn::getName)
        .collect(Collectors.toSet());
    private static final String ANSWER_YES = "Yes";
    private static final String EMPTY_ROW_DATA_VALUE = "";

    @Override
    public NgsExperimentTemplateData parse(byte[] ngsExperimentTemplate) {

        try (
            final InputStream stream = new ByteArrayInputStream(ngsExperimentTemplate);
            final XSSFWorkbook workbook = new XSSFWorkbook(stream)
        ) {

            final NgsExperimentTemplateData templateData = new NgsExperimentTemplateData();
            final XSSFSheet firstSheet = workbook.getSheetAt(0);
            final Iterator<Row> rowIterator = firstSheet.iterator();
            final Row headerRow = iterateToDataHeaderRow(rowIterator);
            final Iterator<Cell> headerCellIterator = headerRow.cellIterator();
            final List<String> header = new ArrayList<>();
            final Map<Integer, Integer> columnIndexToOrder = new HashMap<>();

            while (headerCellIterator.hasNext()) {
                final Cell headerCell = headerCellIterator.next();
                final int columnIndex = headerCell.getColumnIndex();
                final String cellValueTrimmed = headerCell.getStringCellValue().trim();
                columnIndexToOrder.put(columnIndex, header.size());
                header.add(cellValueTrimmed);
            }

            // iteration continues after data header row
            while (rowIterator.hasNext()) {

                final Row row = rowIterator.next();
                final Iterator<Cell> cellIterator = row.cellIterator();
                final Map<String, String> rowData = createEmptyRowData(header);

                while (cellIterator.hasNext()) {

                    final Cell cell = cellIterator.next();
                    final String cellValue = getCellStringValue(cell);
                    final int columnIndex = cell.getColumnIndex();
                    final Integer headerIndex = columnIndexToOrder.get(columnIndex);

                    if (headerIndex != null) {
                        final String columnName = header.get(headerIndex);
                        rowData.put(columnName, cellValue);
                    }
                }

                if (emptyDataRow(rowData)) {
                    continue;
                }

                final NgsExperimentTemplateSampleData sampleData = transformToSampleData(rowData);
                templateData.getSamples().add(sampleData);
            }

            return templateData;

        } catch (Exception ex) {
            throw new NgsExperimentTemplateParseException("Couldn't parse experiment template.", ex);
        }
    }

    private Map<String, String> createEmptyRowData(List<String> header) {
        return header.stream().collect(Collectors.toMap(Function.identity(), c -> EMPTY_ROW_DATA_VALUE));
    }

    private boolean emptyDataRow(Map<String, String> rowData) {
        return rowData.keySet().stream().allMatch(key -> isEmpty(rowData.get(key)));
    }

    private NgsExperimentTemplateSampleData transformToSampleData(Map<String, String> rowData) {

        return NgsExperimentTemplateSampleData.builder()
            .setVendor(rowData.get(VENDOR.getName()))
            .setVendorId(rowData.get(VENDOR_ID.getName()))
            .setVendorProjectName(rowData.get(VENDOR_PROJECT_NAME.getName()))
            .setCelgeneId(rowData.get(CELGENE_ID.getName()))
            .setDaProjectId(rowData.get(DA_PROJECT_ID.getName()))
            .setCelgeneProjectDescription(rowData.get(CELGENE_PROJECT_DESC.getName()))
            .setExperiment(rowData.get(EXPERIMENT.getName()))
            .setDisplayName(rowData.get(DISPLAY_NAME.getName()))
            .setDisplayNameShort(rowData.get(DISPLAY_NAME_SHORT.getName()))
            .setCellType(rowData.get(CELL_TYPE.getName()))
            .setCellLine(rowData.get(CELL_LINE.getName()))
            .setTissue(rowData.get(TISSUE.getName()))
            .setConditions(getAllValuesByBaseName(rowData, CONDITION.getName()))
            .setXenograft(getBooleanFromYesNoAnswer(rowData.get(XENOGRAFT.getName())))
            .setTimeTreatment(rowData.get(TIME_TREATMENT.getName()))
            .setResponseDescriptions(getAllValuesByBaseName(rowData, RESPONSE_DESC.getName()))
            .setResponses(getAllValuesByBaseName(rowData, RESPONSE.getName()))
            .setCompounds(getAllValuesByBaseName(rowData, COMPOUND.getName()))
            .setDoses(getAllValuesByBaseName(rowData, DOSE.getName()))
            .setBiologicalReplicatesGroup(getLongValue(rowData.get(BIOLOGICAL_REPLICATES_GROUP.getName())))
            .setTechnicalReplicatesGroup(getLongValue(rowData.get(TECHNICAL_REPLICATES_GROUP.getName())))
            .setExperimentType(rowData.get(EXPERIMENT_TYPE.getName()))
            .setTechnology(rowData.get(TECHNOLOGY.getName()))
            .setLibraryPrep(rowData.get(LIBRARY_PREP.getName()))
            .setExomeBaitSet(rowData.get(EXOME_BAIT_SET.getName()))
            .setRnaSelection(rowData.get(RNA_SELECTION.getName()))
            .setNtExtraction(rowData.get(NT_EXTRACTION.getName()))
            .setAntibodyTarget(rowData.get(ANTIBODY_TARGET.getName()))
            .setReferenceGenome(rowData.get(REFERENCE_GENOME.getName()))
            .setHostGenome(rowData.get(HOST_GENOME.getName()))
            .setStranded(rowData.get(STRANDED.getName()))
            .setPairedEnd(getBooleanFromYesNoAnswer(rowData.get(PAIRED_END.getName())))
            .setFilename(rowData.get(FILENAME.getName()))
            .build();
    }

    private Map<Integer, String> getAllValuesByBaseName(Map<String, String> rowData, String baseName) {
        final Map<Integer, String> indexToValue = new HashMap<>();

        rowData.keySet()

            .forEach(columnName -> {

                if (!columnName.startsWith(baseName)) {
                    return;
                }

                if (columnName.equals(baseName)) {
                    indexToValue.put(0, rowData.get(columnName));
                    return;
                }

                final String indexSuffix = columnName.substring(baseName.length());
                final Integer index = Ints.tryParse(indexSuffix);

                if (index != null) {
                    indexToValue.put(index, rowData.get(columnName));
                }
            });

        return indexToValue;
    }

    private Long getLongValue(String stringValue) {
        if (isEmpty(stringValue)) {
            return null;
        }
        return Longs.tryParse(stringValue);
    }

    private Boolean getBooleanFromYesNoAnswer(String stringValue) {
        if (isEmpty(stringValue)) {
            return null;
        }
        return ANSWER_YES.equalsIgnoreCase(stringValue);
    }

    private String getCellStringValue(Cell cell) {
        switch (cell.getCellTypeEnum()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    return Long.toString(cell.getDateCellValue().getTime());
                } else {
                    return Long.toString(Math.round(cell.getNumericCellValue()));
                }
            default:
                return null;
        }
    }

    private Row iterateToDataHeaderRow(Iterator<Row> rowIterator) {
        while (rowIterator.hasNext()) {
            final Row row = rowIterator.next();
            final Iterator<Cell> cellIterator = row.cellIterator();

            if (!cellIterator.hasNext()) {
                continue;
            }

            final Cell firstCell = cellIterator.next();
            final CellType cellType = firstCell.getCellTypeEnum();

            if (cellType != CellType.STRING) {
                continue;
            }

            final String firstCellValueTrimmed = firstCell.getStringCellValue().trim();

            if (HEADER_COLUMN_NAMES.contains(firstCellValueTrimmed)) {
                return row;
            }
        }

        throw new NgsExperimentTemplateParseException("Couldn't find data header row in experiment template " +
            "spreadsheet"
        );
    }

}
