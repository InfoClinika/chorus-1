"use strict";

(function () {

    angular.module("experiments-front")
        .controller("import-ngs-experiment", importNGSExperimentController);

    function importNGSExperimentController($scope, $q, Laboratories, InstrumentTechnologyTypes, InstrumentStudyType,
                                           Projects, Instruments, $http) {

        var IMPORT_NGS_EXPERIMENT = "../experiments/importNGSExperiment";
        var ACCEPTABLE_EXTENSIONS = ["xls", "xlsx"];
        var FILE_INPUT_SELECTOR = "#importFileChooser";
        var labToInstrumentsMap = {};

        $scope.page.title = "Import NGS Experiment";
        var vm = {
            loadingInProgress: false,
            projects: [],
            labs: [],
            instruments: [],
            form: {
                file: null
            },
            importExperiment: importExperiment,
            getAcceptableExtensions: getAcceptableExtensions
        };
        $scope.vm = vm;

        init();

        function init() {

            $(FILE_INPUT_SELECTOR).bind("change", function (event) {
                if (event.target.files && event.target.files.length) {
                    $scope.$apply(function () {
                        onFileSelect(event.target.files[0]);
                    });
                }
            });

            $scope.$watch("vm.form.lab", onLabSelectionChanged);

            var promise = loadAllDataFromServer();
            promise.then(allDataLoaded);

            function loadAllDataFromServer() {
                var promises = [];
                promises.push(loadProjects().then(function (projects) {
                    vm.projects = projects;
                }));
                promises.push(loadLabs().then(function (labs) {
                    vm.labs = labs;
                }));
                promises.push(loadStudyTypes().then(function (studyTypes) {
                    vm.studyTypes = studyTypes;
                }));

                return $q.all(promises);

                function loadProjects() {
                    var defer = $q.defer();
                    Projects.query({filter: "allowedForWriting"}, defer.resolve);
                    return defer.promise;
                }

                function loadLabs() {
                    var defer = $q.defer();
                    Laboratories.query(defer.resolve);
                    return defer.promise;
                }

                function loadStudyTypes() {
                    var defer = $q.defer();
                    InstrumentTechnologyTypes.query(defer.resolve);
                    return defer.promise;
                }
            }

            function allDataLoaded() {
                vm.form.project = getDefaultOptionValue(vm.projects, vm.form.project);
                vm.form.lab = getDefaultOptionValue(vm.labs, vm.form.lab);
                vm.ngsStudyType = vm.studyTypes.find(function (type) {
                    return type.name === InstrumentStudyType.NGS;
                });
            }

            function onLabSelectionChanged() {

                var labId = vm.form.lab;
                vm.form.instrument = null;

                if (labId) {
                    if (labToInstrumentsMap[labId]) {
                        vm.instruments = labToInstrumentsMap[labId];
                        selectFirstInstrument(vm.instruments);
                    } else {
                        Instruments.byLabAndStudyType({
                            id: labId,
                            studyTypeId: vm.ngsStudyType.id
                        }, function (instruments) {
                            labToInstrumentsMap[labId] = instruments || [];
                            vm.instruments = labToInstrumentsMap[labId];
                            selectFirstInstrument(vm.instruments);
                        });
                    }
                } else {
                    vm.instruments = [];
                }

                function selectFirstInstrument(instruments) {
                    if (instruments.length > 0) {
                        vm.form.instrument = instruments[0].id;
                    }
                }
            }
        }

        function isFormValid() {
            var valid = true;
            vm.form.validation = {};

            if (!vm.form.name) {
                vm.form.validation.name = "Name is required";
                valid = false;
            }
            if (!vm.form.lab) {
                vm.form.validation.name = "Laboratory is required";
                valid = false;
            }
            if (!vm.form.instrument) {
                vm.form.validation.instrument = "Instrument is required";
                valid = false;
            }
            if (!vm.form.project) {
                vm.form.validation.project = "Project is required";
                valid = false;
            }
            if (!vm.form.file) {
                vm.form.validation.file = "File is required";
                valid = false;
            }

            return valid;
        }

        function importExperiment() {

            vm.importErrorMessage = null;

            if (!isFormValid()) {
                return;
            }

            var formData = new FormData();
            formData.append("name", vm.form.name);
            formData.append("project", vm.form.project);
            formData.append("lab", vm.form.lab);
            formData.append("instrument", vm.form.instrument);
            formData.append("file", vm.form.file);

            vm.loadingInProgress = true;
            $http.post(IMPORT_NGS_EXPERIMENT, formData, {
                withCredentials: true,
                headers: {"Content-Type": null},
                transformRequest: angular.identity
            }).success(function (data) {
                vm.loadingInProgress = false;
                if (!data.errorMessage) {
                    $(".import-ngs-experiment").trigger("hide");
                } else {
                    vm.importErrorMessage = data.errorMessage;
                }
            });
        }

        function onFileSelect(file) {
            vm.form.file = file;
        }

        function getAcceptableExtensions() {
            var result = "";
            ACCEPTABLE_EXTENSIONS.forEach(function (ext) {
                result += "." + ext + ",";
            });
            return result;
        }
    }

})();
