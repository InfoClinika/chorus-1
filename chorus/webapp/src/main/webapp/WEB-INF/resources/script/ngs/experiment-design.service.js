"use strict";

(function () {

    angular.module("ngsExperiment")

        .factory("ExperimentDesignService", function ($http, $q) {
            return {
                wrap: function (samples, factors, annotations) {
                    return new ExperimentDesignWrapper(samples, factors, annotations);
                }
            };
        });

    function ExperimentDesignWrapper(samples, factors, annotations) {
        var _samples = samples;
        var _factors = factors;
        var _annotations = annotations;
        this.addAnnotation = function (name, isNumeric, units) {
            var id = findItemIdByName(name, _annotations);
            if (id === undefined) {
                id = _annotations.length;
                _annotations.push({
                    id: id,
                    name: name,
                    isNumeric: isNumeric,
                    units: isNumeric ? units : null
                });
            }
            angular.forEach(_samples, function (sample) {
                if (!sample.annotationValues) {
                    sample.annotationValues = [];
                }
                sample.annotationValues[id] = "";
            });
            return id;
        };
        this.addFactor = function (name, isNumeric, units) {
            var id = findItemIdByName(name, _factors);
            if (id === undefined) {
                id = _factors.length;
                _factors.push({
                    id: id,
                    name: name,
                    isNumeric: isNumeric,
                    units: isNumeric ? units : null
                });
            }
            angular.forEach(_samples, function (sample) {
                if (!sample.factorValues) {
                    sample.factorValues = [];
                }
                sample.factorValues[id] = "";
            });
            return id;
        };
    }

    function findItemIdByName(name, items) {
        var item = items.find(function (item) {
            return item.name === name;
        });
        return item ? item.id : undefined;
    }

})();
