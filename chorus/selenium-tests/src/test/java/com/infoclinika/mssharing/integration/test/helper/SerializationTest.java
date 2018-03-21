package com.infoclinika.mssharing.integration.test.helper;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
* @author Oleksii Tymchenko
*/
public class SerializationTest {

//    final static class InstrumentData {
//        private String serialNumber;
//        private int resolution;
//        private Owner owner;
//
//        public String getSerialNumber() {
//            return serialNumber;
//        }
//
//        public void setSerialNumber(String serialNumber) {
//            this.serialNumber = serialNumber;
//        }
//
//        public int getResolution() {
//            return resolution;
//        }
//
//        public void setResolution(int resolution) {
//            this.resolution = resolution;
//        }
//
//        public Owner getOwner() {
//            return owner;
//        }
//
//        public void setOwner(Owner owner) {
//            this.owner = owner;
//        }
//
//        InstrumentData() {
//        }
//
//        InstrumentData(String serialNumber, int resolution, Owner owner) {
//            this.serialNumber = serialNumber;
//            this.resolution = resolution;
//            this.owner = owner;
//        }
//
//        @Override
//        public String toString() {
//            return "InstrumentData{" +
//                    "serialNumber='" + serialNumber + '\'' +
//                    ", resolution=" + resolution +
//                    ", owner=" + owner +
//                    '}';
//        }
//    }
//
//    final static class LabeledInstrument {
//        private String label;
//        private InstrumentData data;
//
//        public String getLabel() {
//            return label;
//        }
//
//        public void setLabel(String label) {
//            this.label = label;
//        }
//
//        public InstrumentData getData() {
//            return data;
//        }
//
//        public void setData(InstrumentData data) {
//            this.data = data;
//        }
//    }
//
//    final static class Owner {
//        private String username;
//        private String password;
//
//        Owner(String username, String password) {
//            this.username = username;
//            this.password = password;
//        }
//
//        public String getUsername() {
//            return username;
//        }
//
//        public void setUsername(String username) {
//            this.username = username;
//        }
//
//        public String getPassword() {
//            return password;
//        }
//
//        public void setPassword(String password) {
//            this.password = password;
//        }
//
//        @Override
//        public String toString() {
//            return "Owner{" +
//                    "username='" + username + '\'' +
//                    ", password='" + password + '\'' +
//                    '}';
//        }
//    }
//
//    final static class Instruments {
//        private List<LabeledInstrument> instruments = new LinkedList<>();
//
//        public List<LabeledInstrument> getInstruments() {
//            return instruments;
//        }
//
//        public void setInstruments(List<LabeledInstrument> instruments) {
//            this.instruments = instruments;
//        }
//    }
//
//    public static void main(String[] args) throws IOException {
//
//
//        final String password = "123";
//        final Owner vasya = new Owner("Vasyua", password);
//        final InstrumentData instrumentData = new InstrumentData("1234AA", 2000, vasya);
//
//
//        final File tempDir = Files.createTempDir();
//        final File destFile = new File(tempDir, "instrument1.bin");
//
////        final Gson gson = new Gson();
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        final String serialized = gson.toJson(instrumentData);
//        System.out.println(" - Serialized: " + serialized);
//        System.out.println(" - Writing to the file: " + destFile.getAbsolutePath());
//        final FileOutputStream fos = new FileOutputStream(destFile);
//        IOUtils.write(serialized, fos);
//        fos.flush();

//        System.out.println(" - Writing to the file: " + destFile.getAbsolutePath());
//        final FileOutputStream fos = new FileOutputStream(destFile);
//        final XMLEncoder xmlEncoder = new XMLEncoder(fos);
//        xmlEncoder.writeObject(instrumentData);
//        fos.flush();

//        Assert.assertEquals(password);
//
//        final File sourceFile = new File("/var/folders/4f/zfxc768s4hj0b0s92f29xmd80000gw/T/1412934321172-0/instrument1.bin");
//        final List lines = IOUtils.readLines(new FileInputStream(sourceFile));
//        final StringBuilder fileContentsBuilder = new StringBuilder();
//        for (Object line : lines) {
//            fileContentsBuilder.append(line);
//        }
//
//        final String rawObject = fileContentsBuilder.toString();
//        final InstrumentData deserializedData = gson.fromJson(rawObject, InstrumentData.class);
//        deserializedData.setSerialNumber(new Ran);
//


//        System.out.println(deserializedData);
//    }
//
//    static class InsrumentDataSupplier {
//        static InstrumentData getByLabel(String label, boolean randomizeName) {
//            Gson gson = new Gson();
//
//            throw new RuntimeException("not implemented");
//        }
//
//        static List<InstrumentData> getAllInstruments() {
//            throw new RuntimeException("not implemented");
//        }
//
//
//    }
//
//    static class Deserializer {
//        public static <T> T deserialize(final String source, Class<T> clazz) {
//            final Gson gson = new Gson();
//            return gson.fromJson(source, clazz);
//        }
//    }
//}
//
//private static final String DATA_ROOT_FOLDER = "selenium-tests\\src\\test\\resources\\data";
//
//public InstrumentData getInstrumentByName(String name) {
//        return null;
//        }
//
//
//public static void main(String[] args) throws IOException {
//
//        List<LockMassData> lockMassDataList = new ArrayList<>();
//        lockMassDataList.add(new LockMassData(2.1, "-1"));
//        List<LockMassData> defaultLockMass = new ArrayList<>();
//        defaultLockMass.add(new LockMassData(556.2771, "+1"));
//        defaultLockMass.add(new LockMassData(785.8426, "+2"));
//
//        InstrumentData instrumentData1 = new InstrumentData(
//        "Thermo Instrument without auto translation",
//        SampleData.VENDOR_THERMO,
//        SampleData.MODEL_EXACTIVE_PLUS,
//        "serial",
//        SampleData.LAB_FIRST_CHORUS,
//        "hlpc",
//        "peripherals",
//        false,
//        lockMassDataList,
//        false);
//
//        InstrumentData instrumentData2 = new InstrumentData(
//        "Thermo Instrument with auto translation",
//        SampleData.VENDOR_THERMO,
//        SampleData.MODEL_EXACTIVE_PLUS,
//        "serial",
//        SampleData.LAB_FIRST_CHORUS,
//        "hlpc",
//        "peripherals",
//        false,
//        lockMassDataList,
//        true);
//
//        InstrumentData instrumentData3 = new InstrumentData(
//        "Thermo Instrument with default lock mass",
//        SampleData.VENDOR_THERMO,
//        SampleData.MODEL_EXACTIVE_PLUS,
//        "serial",
//        SampleData.LAB_FIRST_CHORUS,
//        "hlpc",
//        "peripherals",
//        true,
//        defaultLockMass,
//        true
//        );
//
//        InstrumentData instrumentData4 = new InstrumentData(
//        "AB SCIEX Instrument",
//        SampleData.VENDOR_AB_SCIEX,
//        SampleData.MODEL_TRIPLE_TOF_5600,
//        "serial",
//        SampleData.LAB_FIRST_CHORUS,
//        "hlpc",
//        "peripherals",
//        true,
//        defaultLockMass,
//        true
//        );

//        List<InstrumentData> list = new ArrayList<>();
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//
//        list.add(instrumentData1);
//        list.add(instrumentData2);
//        list.add(instrumentData3);
//        list.add(instrumentData4);
//        Type type = new TypeToken<List<InstrumentData>>() {
//        }.getType();
//        String json = gson.toJson(list);
//        System.out.println(json);
//        final File destFile = new File(DATA_ROOT_FOLDER, "instrument.json");
//        System.out.println(" - Writing to the file: " + destFile.getAbsolutePath());
//        final FileOutputStream fos = new FileOutputStream(destFile);
//        IOUtils.write(json, fos);
//        fos.flush();

//        System.out.println("---Deserialization  in java Object---");
//        String json = new Scanner(new File(DATA_ROOT_FOLDER + "\\instrument.json")).useDelimiter("\\Z").next();
//        System.out.println(json);
//        List<InstrumentData> desData = gson.fromJson(json, type);
//        InstrumentData dataForTest = null;
//        for (InstrumentData instrumentData : desData) {
//        if (instrumentData.getName().equals("Test Instrument")){
//        dataForTest = instrumentData;
//        }
//
//        }
//
//        System.out.println(dataForTest);
//
//
        }


