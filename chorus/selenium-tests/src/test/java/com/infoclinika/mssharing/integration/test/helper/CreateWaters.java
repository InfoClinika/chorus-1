package com.infoclinika.mssharing.integration.test.helper;

import com.infoclinika.mssharing.integration.test.data.file.FileData;

import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeName;

/**
 * @author Alexander Orlov
 */
public class CreateWaters {

    static FileData fileData = new FileData(
            randomizeName("File"),
            randomizeName("File")
            );

    public static void main(String[] args) {
        for (int i=1; i<200; i++) {
            fileData.createWatersFolderWithSizeInMb(2);
        }
    }
}
