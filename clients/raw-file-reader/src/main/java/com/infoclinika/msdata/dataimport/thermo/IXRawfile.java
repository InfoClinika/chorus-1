package com.infoclinika.msdata.dataimport.thermo;

import com.jniwrapper.win32.automation.IDispatch;
import com.jniwrapper.win32.automation.types.BStr;
import com.jniwrapper.win32.com.ComException;

/**
 * Represents Java interface for COM interface IXRawfile.
 */
public interface IXRawfile extends IDispatch {
    void open(
        BStr /*[in]*/ szFileName)
        throws ComException;

    void close()
        throws ComException;

}
