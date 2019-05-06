"use strict";
(function () {

    angular.module("instrument-models")
        .factory("InstrumentModelValidation", function ($q, InstrumentModels) {

            return function validate(model, opts) {

                var errors = {};
                var deferred = $q.defer();

                if (!model.vendor.name) {
                    errors.vendor = "Vendor is required.";
                }

                if (!model.technologyType.name) {
                    errors.technologyType = "Technology is required.";
                }

                if (!model.instrumentType.name) {
                    errors.instrumentType = "Instrument type is required.";
                }

                if (model.extensions.length === 0) {
                    errors.extensions = "At least one extension is required";
                }

                if (!model.name || !model.name.trim()) {
                    errors.name = "Name is required.";
                    resolve();
                } else if (opts && opts.skipUniqueNameValidation) {
                    resolve();
                } else {
                    var request = {
                        name: model.name.trim(),
                        vendor: model.vendor.id || "",
                        modelId: model.id || ""
                    };
                    InstrumentModels.isNameUnique(request, function (response) {
                        var isNameUnique = response.value;
                        if (!isNameUnique) {
                            errors.name = "Instrument model with this name already exists.";
                        }
                        resolve();
                    });
                }

                return deferred.promise;

                function resolve() {
                    deferred.resolve(errors);
                }
            };
        });
})();
