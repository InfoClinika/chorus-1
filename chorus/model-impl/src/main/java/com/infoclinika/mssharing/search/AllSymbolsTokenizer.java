package com.infoclinika.mssharing.search;

import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.util.Version;

import java.io.Reader;

/**
*  @author Herman Zamula
*/
public class AllSymbolsTokenizer extends CharTokenizer {

    public AllSymbolsTokenizer(Version matchVersion, Reader input) {
        super(matchVersion, input);
    }

    /**
     * Allow all symbols to tokenize
     *
     * @param c - character
     * @return true
     */
    @Override
    protected boolean isTokenChar(int c) {
        return true;
    }
}
