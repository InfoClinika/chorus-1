package com.infoclinika.mssharing.model.internal.helper.isaexport;

import com.google.common.collect.ImmutableMap;
import com.infoclinika.mssharing.model.internal.entity.ExperimentSampleType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : Alexander Serebriyan
 */
public class TmtLabelsMappingProvider {

    private static final Map<ExperimentSampleType, LabelMapping> mapping =
        Collections.unmodifiableMap(createTmtLabelsMapping());

    private TmtLabelsMappingProvider() {
    }

    public static Map<ExperimentSampleType, LabelMapping> get() {
        return mapping;
    }

    private static Map<ExperimentSampleType, LabelMapping> createTmtLabelsMapping() {
        final HashMap<ExperimentSampleType, LabelMapping> mapping = new HashMap<>();
        mapping.put(
            ExperimentSampleType.CHANNEL_1,
            new LabelMapping.Builder()
                .setTmt(ImmutableMap.of("2", "126.127726", "6", "126.127726", "10", "126.127725"))
                .setItraq(ImmutableMap.of("4", "114", "8", "113"))
                .build()
        );

        mapping.put(
            ExperimentSampleType.CHANNEL_2,
            new LabelMapping.Builder()
                .setTmt(ImmutableMap.of("2", "127.131081", "6", "127.124761", "10", "127.124760"))
                .setItraq(ImmutableMap.of("4", "115", "8", "114"))
                .build()
        );

        mapping.put(
            ExperimentSampleType.CHANNEL_3,
            new LabelMapping.Builder()
                .setTmt(ImmutableMap.of("6", "128.134436", "10", "127.131079"))
                .setItraq(ImmutableMap.of("4", "116", "8", "115"))
                .build()
        );

        mapping.put(
            ExperimentSampleType.CHANNEL_4,
            new LabelMapping.Builder()
                .setTmt(ImmutableMap.of("6", "129.131471", "10", "128.128114"))
                .setItraq(ImmutableMap.of("4", "117", "8", "116"))
                .build()
        );

        mapping.put(
            ExperimentSampleType.CHANNEL_5,
            new LabelMapping.Builder()
                .setTmt(ImmutableMap.of("6", "130.141145", "10", "128.134433"))
                .setItraq(ImmutableMap.of("8", "117"))
                .build()
        );

        mapping.put(
            ExperimentSampleType.CHANNEL_6,
            new LabelMapping.Builder()
                .setTmt(ImmutableMap.of("6", "131.138180", "10", "129.131468"))
                .setItraq(ImmutableMap.of("8", "118"))
                .build()
        );

        mapping.put(
            ExperimentSampleType.CHANNEL_7,
            new LabelMapping.Builder()
                .setTmt(ImmutableMap.of("10", "129.137787"))
                .setItraq(ImmutableMap.of("8", "119"))
                .build()
        );

        mapping.put(
            ExperimentSampleType.CHANNEL_8,
            new LabelMapping.Builder()
                .setTmt(ImmutableMap.of("10", "130.134822"))
                .setItraq(ImmutableMap.of("8", "120"))
                .build()
        );

        mapping.put(
            ExperimentSampleType.CHANNEL_9,
            new LabelMapping.Builder()
                .setTmt(ImmutableMap.of("10", "130.141141"))
                .build()
        );

        mapping.put(
            ExperimentSampleType.CHANNEL_10,
            new LabelMapping.Builder()
                .setTmt(ImmutableMap.of("10", "131.138176"))
                .build()
        );

        return mapping;
    }

    static class LabelMapping {
        Map<String, String> tmt;
        Map<String, String> itraq;

        private LabelMapping(Map<String, String> tmt, Map<String, String> itraq) {
            this.tmt = tmt;
            this.itraq = itraq;
        }

        Map<String, String> getTmt() {
            return tmt;
        }

        Map<String, String> getItraq() {
            return itraq;
        }

        private static class Builder {
            private Map<String, String> tmt;
            private Map<String, String> itraq;

            private Builder setTmt(Map<String, String> tmt) {
                this.tmt = tmt;
                return this;
            }

            private Builder setItraq(Map<String, String> itraq) {
                this.itraq = itraq;
                return this;
            }

            private LabelMapping build() {
                return new LabelMapping(tmt, itraq);
            }
        }

    }
}
