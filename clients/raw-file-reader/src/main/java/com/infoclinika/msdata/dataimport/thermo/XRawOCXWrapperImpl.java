package com.infoclinika.msdata.dataimport.thermo;

import com.jniwrapper.Int32;
import com.jniwrapper.win32.automation.types.BStr;
import com.jniwrapper.win32.com.ComException;
import com.jniwrapper.win32.com.types.CLSID;
import com.jniwrapper.win32.com.types.ClsCtx;
import com.jniwrapper.win32.ole.OleFunctions;

public class XRawOCXWrapperImpl {
    //    private static final CLSID CLASS_ID = CLSID.create("{55A25FF7-F437-471F-909A-D7F2B5930805}");
    // 5FE970B1-29C3-11D3-811D-00104B304896      1D23188D-53FE-4C25-B032-DC70ACDBDC02
    private static CLSID CLASS_ID = null;
    //5FE970B1-29C3-11D3-811D-00104B304896      1D23188D-53FE-4C25-B032-DC70ACDBDC02

    private final IXRawfileImpl _rawfile;

    public static XRawOCXWrapperImpl create() {
        if (CLASS_ID == null) {
            CLASS_ID = CLSID.create("{1D23188D-53FE-4C25-B032-DC70ACDBDC02}");
        }
        return new XRawOCXWrapperImpl();
    }

    public XRawOCXWrapperImpl() {
        OleFunctions.oleInitialize();
        try {
            _rawfile = new IXRawfileImpl(CLASS_ID, ClsCtx.INPROC_SERVER);
        } catch (ComException e) {
            throw new RuntimeException(
                "Failed to create IXRawfile COM object. Error message: " + e.getLocalizedMessage());
        }
    }

    public void open(String fileName) throws Exception {
        _rawfile.open(new BStr(fileName));
    }

    public void close() throws Exception {
        _rawfile.close();
    }

    public boolean inAcquisition() throws Exception {
        Int32 result = new Int32();
        _rawfile.inAcquisition(result);

        return result.getValue() == 1;
    }

}


