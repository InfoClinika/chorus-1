"use strict";

(function () {

    angular.module("dialogs")

        .factory("FilterBuilderService", function () {
            return {
                emptyFilter: {
                    conjunction: true,
                    predicates: []
                },
                isFilterEmpty: isFilterEmpty,
                filterChanged: filterChanged,
                files: [
                    {prop: "id", title: "ID", type: "number"},
                    {prop: "name", title: "Name", type: "string"},
                    {prop: "instrument", title: "Instrument", type: "string"},
                    {prop: "laboratory", title: "Laboratory", type: "string"},
                    {prop: "uploadDate", title: "Upload Date", type: "date"},
                    {prop: "labels", title: "Labels", type: "string"},
                    {prop: "annotationInstrument", title: "Annotation Instrument", type: "string"},
                    {prop: "sizeInBytes", title: "Size(in bytes)", type: "number"},
                    {prop: "userName", title: "User Name", type: "string"},
                    {prop: "userLabels", title: "User Labels", type: "string"},
                    {prop: "fileCondition", title: "File Condition", type: "string"},
                    {prop: "instrumentMethod", title: "Instrument Method", type: "string"},
                    //{prop: "endRt", title:"End Time", type: "string"},
                    //{prop: "startRt", title:"Start Time", type: "string"},
                    {prop: "creationDate", title: "Creation Date", type: "date"},
                    {prop: "comment", title: "Comment", type: "string"},
                    //{prop: "startMz", title:"Start mz", type: "string"},
                    //{prop: "endMz", title:"End mz", type: "string"},
                    {prop: "fileName", title: "File Name", type: "string"},
                    {prop: "seqRowPosition", title: "Position", type: "string"},
                    {prop: "sampleName", title: "Sample Name", type: "string"},
                    {prop: "translateFlag", title: "Translate Flag", type: "string"},
                    {prop: "instrumentSerialNumber", title: "Instrument Serial", type: "string"},
                    {prop: "phone", title: "Phone", type: "string"},
                    {prop: "instrumentName", title: "Instrument Name", type: "string"}
                ],
                mergeWithPredicate: mergeWithPredicate,
                predicateByTechnologyType: createPredicateByTechnologyType,
                predicateByInstrumentId: createPredicateByInstrumentId,
                predicateByInstrumentModelId: createPredicateByInstrumentModelId,
                predicateBySpecieId: createPredicateBySpecieId
            };
        });

    function isFilterEmpty(filterComposed) {
        return !filterComposed || !filterComposed.predicates;
    }

    function filterChanged(f1, f2) {
        var changed = f1.conjunction !== f2.conjunction
            || f1.predicates.length !== f2.predicates.length;
        if (!changed) {
            var p1 = f1.predicates;
            var p2 = f2.predicates;
            for (var i = 0, l = p1.length; i < l; i++) {
                var v1 = p1[i];
                var v2 = p2.find(function (p) {
                    return p.prop === v1.prop && p.predicate === v1.predicate && p.value === v1.value;
                });
                if (angular.isDefined(v2)) {
                    changed = true;
                    break;
                }
            }
        }
        return changed;
    }

    function mergeWithPredicate(filter, predicate) {
        var existed = findPredicateByProp(filter.predicates, predicate.prop);

        if (filter.conjunction && existed !== undefined) {
            existed.operator = predicate.operator;
            existed.value = predicate.value;
        } else {
            filter.predicates.push(predicate);
        }

        return filter;
    }

    function createPredicateByInstrumentModelId(instrumentModelId) {
        return {
            operator: "EQUAL",
            prop: "instrumentModelId",
            value: instrumentModelId
        };
    }

    function createPredicateByTechnologyType(technologyTypeId) {
        return {
            operator: "EQUAL",
            prop: "studyType",
            value: technologyTypeId
        };
    }

    function createPredicateByInstrumentId(instrumentId) {
        return {
            operator: "EQUAL",
            prop: "instrumentId",
            value: instrumentId
        };
    }

    function createPredicateBySpecieId(specieId) {
        return {
            operator: "EQUAL",
            prop: "specieId",
            value: specieId
        };
    }

    function findPredicateByProp(predicates, prop) {
        return predicates.find(function (predicate) {
            return predicate.prop === prop;
        });
    }

})();
