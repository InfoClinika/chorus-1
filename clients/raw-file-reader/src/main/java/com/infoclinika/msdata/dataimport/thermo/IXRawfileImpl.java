package com.infoclinika.msdata.dataimport.thermo;

import com.jniwrapper.*;
import com.jniwrapper.win32.automation.impl.IDispatchImpl;
import com.jniwrapper.win32.automation.types.BStr;
import com.jniwrapper.win32.com.ComException;
import com.jniwrapper.win32.com.types.CLSID;
import com.jniwrapper.win32.com.types.ClsCtx;

/**
 * Represents COM interface IXRawfile.
 */
// HidingField check fails with error:
// class file for com.jniwrapper.win32.com.impl.IUnknownImpl$NativeResourceImpl not found
@SuppressWarnings("HidingField")
public class IXRawfileImpl extends IDispatchImpl
    implements com.infoclinika.msdata.dataimport.thermo.IXRawfile {

    public IXRawfileImpl(CLSID clsid, ClsCtx dwClsContext) throws ComException {
        super(clsid, dwClsContext);
    }

    @Override
    public void open(
        BStr /*[in]*/ szFileName)
        throws ComException {
        invokeStandardVirtualMethod(
            7,
            Function.STDCALL_CALLING_CONVENTION,
            new Parameter[] {
                szFileName == null ? (Parameter) PTR_NULL : new Const(szFileName)
            }
        );
    }

    @Override
    public void close()
        throws ComException {
        invokeStandardVirtualMethod(
            8,
            Function.STDCALL_CALLING_CONVENTION,
            new Parameter[0]
        );
    }

    public void inAcquisition(
        Int32 /*[in,out]*/ pbInAcquisition)
        throws ComException {
        invokeStandardVirtualMethod(
            37,
            Function.STDCALL_CALLING_CONVENTION,
            new Parameter[] {
                pbInAcquisition == null ? (Parameter) PTR_NULL : new Pointer(pbInAcquisition)
            }
        );
    }

}
