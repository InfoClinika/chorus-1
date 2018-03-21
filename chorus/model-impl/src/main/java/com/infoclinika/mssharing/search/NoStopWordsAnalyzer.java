package com.infoclinika.mssharing.search;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;

/**
 * Ignore all stop words like <i>the</i>, <i>a</i>, <i>an</i> etc.
 * Uses {@link AllSymbolsTokenizer}.
 *
 * @author Herman Zamula
 */
public class NoStopWordsAnalyzer extends StopwordAnalyzerBase {


    public NoStopWordsAnalyzer(Version version) {
        super(version, Collections.emptySet());
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        final AllSymbolsTokenizer src = new AllSymbolsTokenizer(matchVersion, reader);
        //TokenStream tok = new StandardFilter(matchVersion, src);
        TokenStream tok = new LowerCaseFilter(matchVersion, src);
        tok = new StopFilter(matchVersion, tok, stopwords);
        return new TokenStreamComponents(src, tok) {
            @Override
            protected boolean reset(final Reader reader) throws IOException {
                return super.reset(reader);
            }
        };
    }

}