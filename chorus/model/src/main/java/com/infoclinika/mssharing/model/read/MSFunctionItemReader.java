package com.infoclinika.mssharing.model.read;

/**
 * @author vladislav.kovchug
 */
public interface MSFunctionItemReader {

    Iterable<MSFunctionItemInfo> readAll();

    class MSFunctionItemInfo {
        public final long id;
        public final String functionName;
        public final Long mzGridParamsId;

        public MSFunctionItemInfo(long id, String functionName, Long mzGridParamsId) {
            this.id = id;
            this.functionName = functionName;
            this.mzGridParamsId = mzGridParamsId;
        }
    }
}
