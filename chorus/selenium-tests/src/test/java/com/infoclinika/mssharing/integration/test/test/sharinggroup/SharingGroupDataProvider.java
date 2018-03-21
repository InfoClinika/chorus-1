package com.infoclinika.mssharing.integration.test.test.sharinggroup;

import com.infoclinika.mssharing.integration.test.data.SharingGroupData;
import com.infoclinika.mssharing.integration.test.testdata.AbstractDataProvider;
import org.testng.annotations.DataProvider;

import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeName;

/**
 * @author Alexander Orlov
 */
public class SharingGroupDataProvider extends AbstractDataProvider {

    @DataProvider
    public static Object[][] createRemoveSharingGroup() {
        SharingGroupData sharingGroupData = new SharingGroupData.Builder()
                .name(randomizeName("Group"))
                .personToInviteName(environmentSpecificData.karrenKoe.getFullName())
                .personToInviteEmail(environmentSpecificData.karrenKoe.getEmail()).build();
        return new Object[][]{{sharingGroupData}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToCreateSharingGroupWithExistingName() {
        SharingGroupData sharingGroupData = new SharingGroupData.Builder()
                .name(randomizeName("Group"))
                .personToInviteName(environmentSpecificData.karrenKoe.getFullName())
                .personToInviteEmail(environmentSpecificData.karrenKoe.getEmail()).build();
        String alert = "Name is already taken";
        return new Object[][]{{sharingGroupData, alert}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToCreateSharingGroupWithoutSpecifyingRequiredFields() {
        SharingGroupData sharingGroupData = new SharingGroupData.Builder()
                .name(randomizeName("Group"))
                .personToInviteName(environmentSpecificData.karrenKoe.getFullName())
                .personToInviteEmail(environmentSpecificData.karrenKoe.getEmail()).build();
        return new Object[][]{{sharingGroupData}};
    }
}
