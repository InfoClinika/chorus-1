package com.infoclinika.mssharing.integration.test.test.instrument;

import com.infoclinika.mssharing.integration.test.data.SampleData;
import com.infoclinika.mssharing.integration.test.data.experiment.*;
import com.infoclinika.mssharing.integration.test.data.instrument.InstrumentData;
import com.infoclinika.mssharing.integration.test.data.instrument.OptionalFields;
import com.infoclinika.mssharing.integration.test.testdata.AbstractDataProvider;
import org.testng.annotations.DataProvider;

import java.util.LinkedList;
import java.util.List;

import static com.infoclinika.mssharing.integration.test.utils.Strings.randomizeName;

/**
 * @author Alexander Orlov
 */
public class InstrumentDataProvider extends AbstractDataProvider {

    @DataProvider
    public static Object[][] createInstrumentStayingOnAllProjectsPage() {
        InstrumentData instrumentData = new InstrumentData.Builder()
                .name(randomizeName("Instrument"))
                .vendor(SampleData.VENDOR_AGILENT)
                .model(SampleData.MODEL_220_GCMS_ION_TRAP_SYSTEM)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("AH999X")).build();
        return new Object[][]{{instrumentData}};
    }

    @DataProvider
    public static Object[][] createInstrumentStayingOnInstrumentsPage() {
        InstrumentData instrumentData = new InstrumentData.Builder()
                .name(randomizeName("Instrument"))
                .vendor(SampleData.VENDOR_AGILENT)
                .model(SampleData.MODEL_220_GCMS_ION_TRAP_SYSTEM)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("AH999X")).build();
        return new Object[][]{{instrumentData}};
    }

    @DataProvider
    public static Object[][] editInstrument() {
        OptionalFields optionalFields1 = new OptionalFields.Builder()
                .hLPC(randomizeName("hlpc"))
                .peripherals(randomizeName("peripherals")).build();
        InstrumentData instrumentData1 = new InstrumentData.Builder()
                .name(randomizeName("Instrument"))
                .vendor(SampleData.VENDOR_THERMO)
                .model(SampleData.MODEL_EXACTIVE_PLUS)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("serial"))
                .optionalFields(optionalFields1).build();
        OptionalFields optionalFields2 = new OptionalFields.Builder()
                .hLPC(randomizeName("edited hlpc"))
                .peripherals(randomizeName("edited peripherals")).build();
        InstrumentData instrumentData2 = new InstrumentData.Builder()
                .name(randomizeName("Edited Instrument"))
                .serialNumber(randomizeName("edited serial"))
                .optionalFields(optionalFields2).build();
        return new Object[][]{{instrumentData1, instrumentData2}};
    }

    @DataProvider
    public static Object[][] removeInstrument() {
        InstrumentData instrumentData = new InstrumentData.Builder()
                .name(randomizeName("Instrument"))
                .vendor(SampleData.VENDOR_THERMO)
                .model(SampleData.MODEL_EXACTIVE_PLUS)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("serial")).build();
        return new Object[][]{{instrumentData}};
    }

    @DataProvider
    public static Object[][] instrumentFormLimitationsDuringCreation() {
        InstrumentData instrumentData = new InstrumentData.Builder()
                .name(randomizeName("Instrument"))
                .vendor(SampleData.VENDOR_THERMO)
                .model(SampleData.MODEL_EXACTIVE_PLUS)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("serial")).build();
        return new Object[][]{{instrumentData}};
    }

    @DataProvider
    public static Object[][] instrumentFormLimitationsDuringEditing() {
        InstrumentData instrumentData = new InstrumentData.Builder()
                .name(randomizeName("Instrument"))
                .vendor(SampleData.VENDOR_THERMO)
                .model(SampleData.MODEL_EXACTIVE_PLUS)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("serial")).build();
        return new Object[][]{{instrumentData}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToCreateInstrumentsWithIdenticalSerialNumbers() {
        InstrumentData instrumentData1 = new InstrumentData.Builder()
                .name(randomizeName("Instrument1"))
                .vendor(SampleData.VENDOR_THERMO)
                .model(SampleData.MODEL_EXACTIVE_PLUS)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("serial1")).build();
        InstrumentData instrumentData2 = new InstrumentData.Builder()
                .name(randomizeName("Instrument2"))
                .vendor(SampleData.VENDOR_THERMO)
                .model(SampleData.MODEL_EXACTIVE_PLUS)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(instrumentData1.getSerialNumber()).build();
        return new Object[][]{{instrumentData1, instrumentData2}};
    }

    @DataProvider
    public static Object[][] shouldNotAllowToCreateInstrumentsWithIdenticalNames() {
        InstrumentData instrumentData1 = new InstrumentData.Builder()
                .name(randomizeName("Instrument1"))
                .vendor(SampleData.VENDOR_THERMO)
                .model(SampleData.MODEL_EXACTIVE_PLUS)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("serial1")).build();
        InstrumentData instrumentData2 = new InstrumentData.Builder()
                .name(instrumentData1.getName())
                .vendor(SampleData.VENDOR_THERMO)
                .model(SampleData.MODEL_EXACTIVE_PLUS)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("serial2")).build();
        return new Object[][]{{instrumentData1, instrumentData2}};
    }

    @DataProvider
    public static Object[][] editInstrumentNameOnly() {
        InstrumentData instrumentData = new InstrumentData.Builder()
                .name(randomizeName("Instrument"))
                .vendor(SampleData.VENDOR_AGILENT)
                .model(SampleData.MODEL_220_GCMS_ION_TRAP_SYSTEM)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("serial")).build();
        return new Object[][]{{instrumentData, randomizeName("Edited Name")}};
    }

    @DataProvider
    public static Object[][] shareInstrument() {
        InstrumentData instrumentData = new InstrumentData.Builder()
                .name(randomizeName("Instrument"))
                .vendor(SampleData.VENDOR_THERMO)
                .model(SampleData.MODEL_EXACTIVE_PLUS)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("serial")).build();
        return new Object[][]{{instrumentData, environmentSpecificData.geneSimmonsAtGmail, environmentSpecificData.pavelKaplinAtGmail}};
    }

    @DataProvider
    public static Object[][] requestInstrument() {
        InstrumentData instrumentData = new InstrumentData.Builder()
                .name(randomizeName("Instrument"))
                .vendor(SampleData.VENDOR_THERMO)
                .model(SampleData.MODEL_EXACTIVE_PLUS)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("serial")).build();
        return new Object[][]{{instrumentData, environmentSpecificData.geneSimmonsAtGmail, environmentSpecificData.pavelKaplinAtGmail}};
    }

    @DataProvider
    public static Object[][] createInstrumentWithLockMasses(){
        List<LockMz> lockMzList = new LinkedList<>();
        lockMzList.add(new LockMz.Builder().lockMass("3").charge("+2").build());
        lockMzList.add(new LockMz.Builder().lockMass("1.5").charge("-3").build());
        OptionalFields optionalFields = new OptionalFields.Builder()
                .hLPC(randomizeName("hlpc"))
                .peripherals(randomizeName("peripherals"))
                .lockMasses(lockMzList).build();
        InstrumentData instrumentData = new InstrumentData.Builder()
                .name(randomizeName("Instrument"))
                .vendor(SampleData.VENDOR_THERMO)
                .model(SampleData.MODEL_EXACTIVE_PLUS)
                .laboratory(environmentSpecificData.defaultLaboratory)
                .serialNumber(randomizeName("serial"))
                .optionalFields(optionalFields).build();
        return new Object[][]{{instrumentData}};
    }


}
