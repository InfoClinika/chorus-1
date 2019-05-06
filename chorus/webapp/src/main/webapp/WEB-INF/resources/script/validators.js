"use strict";
(function () {


    angular.module("validators", ["error-catcher"])
        .directive("sameAs", sameAs)
        .directive("differentThen", differentThen)
        .directive("likeAs", likeAs)
        .directive("inputValidator", inputValidator)
        .directive("uiSelect2Validator", uiSelect2Validator)
        .directive("checkUiSelection", checkUiSelection)
        .directive("passwordStrength", passwordStrength)
        .directive("passwordBlackList", passwordBlackList);


    function sameAs() {
        return {
            require: "ngModel",
            link: function (scope, elm, attrs, ctrl) {
                ctrl.$parsers.unshift(function (viewValue) {
                    if (viewValue === scope.$eval(attrs.sameAs)) {
                        ctrl.$setValidity("sameAs", true);
                        scope.sameAs = true;
                        return viewValue;
                    }
                    ctrl.$setValidity("sameAs", false);
                    scope.sameAs = false;
                    return undefined;

                });
            }
        };
    }

    function differentThen() {
        return {
            require: "ngModel",
            link: function (scope, elm, attrs, ctrl) {
                scope.isDifferent = true;
                scope.$watch(attrs.ngModel, function (viewValue) {
                    if (!viewValue) {
                        return;
                    }
                    return check(viewValue, attrs.differentThen);
                });

                scope.$watch(attrs.differentThen, function (viewValue) {
                    if (!viewValue) {
                        return;
                    }
                    return check(viewValue, attrs.ngModel);
                });

                function check(viewValue, model) {
                    if (viewValue === scope.$eval(model)) {
                        ctrl.$setValidity("differentThen", false);
                        scope.isDifferent = false;
                    } else {
                        ctrl.$setValidity("differentThen", true);
                        scope.isDifferent = true;
                    }
                }
            }
        };
    }

    function likeAs() {
        return {
            require: "ngModel",
            link: function (scope, elm, attrs, ctrl) {
                scope.$watch(attrs.ngModel, function (viewValue) {
                    check(viewValue, attrs.likeAs);
                });

                scope.$watch(attrs.likeAs, function (viewValue) {
                    check(viewValue, attrs.ngModel);
                });

                function check(viewValue, model) {
                    if (viewValue === scope.$eval(model)) {
                        ctrl.$setValidity("likeAs", true);
                    } else {
                        ctrl.$setValidity("likeAs", false);
                    }
                }
            }
        };
    }

    function inputValidator() {
        return {
            require: "ngModel",
            link: function (scope, elm, attrs, ctrl) {
                var changed = false;

                function setValidity(val) {
                    if (!val) {
                        ctrl.$setValidity("inputValidator", false);
                    } else {
                        ctrl.$setValidity("inputValidator", true);
                    }
                    setTimeout(function () {
                        scope.$apply();
                    });
                }

                elm.on("blur", function () {
                    changed = true;
                    setValidity(ctrl.$viewValue);
                });
                scope.$watch(attrs.ngModel, function (newValue) {
                    if (changed) {
                        setValidity(newValue);
                    }
                    if (newValue) {
                        changed = true;
                    }
                });
                if (attrs.inputValidator) {
                    scope.$watch(attrs.inputValidator, function (val) {
                        changed = true;
                        ctrl.$setValidity("inputValidator", val ? true : false);
                    });
                }
                ctrl.$setValidity("inputValidator", attrs.inputValidator ? scope.$eval(attrs.inputValidator) : true);

            }
        };
    }

    function uiSelect2Validator() {
        return {
            require: "ngModel",
            link: function (scope, elm, attrs, ctrl) {
                var changed = false;

                function setValidity(val) {
                    if (!val) {
                        ctrl.$setValidity("uiSelect2Validator", false);
                    } else {
                        ctrl.$setValidity("uiSelect2Validator", true);
                    }
                    setTimeout(function () {
                        scope.$apply();
                    });
                }

                elm.select2().on("close", function () {
                    changed = true;
                    setValidity(ctrl.$viewValue);
                });
                scope.$watch(attrs.ngModel, function (newValue) {
                    if (changed) {
                        setValidity(newValue);
                    }
                });

                ctrl.$setValidity("uiSelect2Validator", true);

            }
        };
    }

    function checkUiSelection() {
        return {
            restrict: "A",
            require: "?ngModel",
            link: function (scope, elm, attrs, ngModel) {
                if (!ngModel) {
                    return;
                }
                var watchModel = attrs.ngModel;
                scope.$watch(watchModel, function (newVal, oldVal) {
                    if (newVal === undefined) {
                        setTimeout(function () {
                            ngModel.$setViewValue(oldVal);
                            scope.$apply(function () {
                                elm.select2("val", ngModel.$viewValue);
                            });
                        }, 0);
                    }
                });
            }
        };
    }

    function passwordStrength() {
        return {
            require: "ngModel",
            link: function (scope, elm, attrs, ctrl) {

                ctrl.$parsers.unshift(validateInputValue);

                function validateInputValue(viewValue) {

                    var pwdValidLength = viewValue && viewValue.length >= 8 ? "valid" : undefined;
                    var pwdHasCapitalLetter = viewValue && /[A-Z]/.test(viewValue) ? "valid" : undefined;
                    var pwdHasLetter = viewValue && /[a-z]/.test(viewValue) ? "valid" : undefined;
                    var pwdHasNumber = viewValue && /\d/.test(viewValue) ? "valid" : undefined;

                    if (pwdValidLength && pwdHasLetter && pwdHasNumber && pwdHasCapitalLetter) {
                        ctrl.$setValidity("passwordStrength", true);
                    } else {
                        ctrl.$setValidity("passwordStrength", false);
                    }

                    return viewValue;

                }
            }
        };
    }

    function passwordBlackList() {
        return {
            require: "ngModel",
            link: function (scope, elm, attrs, ctrl) {

                var blackList = getBlackList();
                ctrl.$parsers.unshift(validateInputValue);


                function validateInputValue(viewValue) {

                    var inBlackList = blackList.indexOf(viewValue) != -1;

                    if (inBlackList) {
                        ctrl.$setValidity("passwordBlackList", false);
                    } else {
                        ctrl.$setValidity("passwordBlackList", true);
                    }

                    return viewValue;
                }

                function getBlackList() {
                    return ["123456",
                        "password",
                        "12345678",
                        "qwerty",
                        "12345",
                        "123456789",
                        "football",
                        "1234",
                        "1234567",
                        "baseball",
                        "welcome",
                        "1234567890",
                        "abc123",
                        "111111",
                        "1qaz2wsx",
                        "dragon",
                        "master",
                        "monkey",
                        "letmein",
                        "login",
                        "princess",
                        "qwertyuiop",
                        "solo",
                        "passw0rd",
                        "starwars"];
                }
            }
        };
    }
})();

