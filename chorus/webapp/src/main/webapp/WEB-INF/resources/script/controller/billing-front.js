"use strict";

var defaultPage = "/pages/dashboard.html";
(function () {
    angular
        .module("invoice", ["billing-back", "lab-head-back", "formatters", "sessionTimeoutHandler", "security-front",
                "security-back", "front-end", "modals", "watchFighters", "ui", "error-catcher", "infinite-scroll",
                "dashboard-common-directives", "template-components"],
            function ($locationProvider) {
//        $locationProvider.html5Mode(true);
            }
        )
        .config(["$routeProvider", function ($routeProvider) {
            $routeProvider
                .when("/subscription/:labId", {
                    controller: "subscription", templateUrl: "../pages/billing/subscription.html"
                })
                .when("/history/:labId", {
                    controller: "history", templateUrl: "../pages/billing/history.html"
                })
                .when("/list", {
                    controller: "list", templateUrl: "../pages/billing/list.html"
                })
                .when("/labInvoices", {
                    controller: "adminList", templateUrl: "../pages/billing/list.html"
                })
                .when("/", {
                    templateUrl: "../pages/billing/list.html", controller: "list"
                });
        }])
        .controller("mainBillingController", mainBillingController)
        .controller("history", historyController)
        .controller("subscription", subscriptionController)
        .controller("list", listController)
        .controller("adminList", adminListController)
        .controller("navigation", navigationController)
        .controller("billingDetailsController", billingDetailsController)
        .directive("usageTable", usageTableController)
        .directive("chargesTable", chargesTableController)
        .directive("usageItem", usageItemController)
        .factory("BillingSecurity", BillingSecurity)
        .factory("Navigation", ["$location", Navigation])
        .factory("expandMenuLab", expandMenuLab)
        .factory("expandMenuChargeItem", expandMenuChargeItem)
        .filter("filePrice", filePrice)
        .filter("price", price)
        .filter("formatDate", formatDate)
        .filter("formatMonth", formatMonth)
        .filter("featureActivity", featureActivity)
        .filter("toDays", toDays)
        .filter("storageVolumeSize", storageVolumeSize);


    function mainBillingController($scope) {
        $scope.$on("$routeChangeSuccess", function () {
            setTimeout(function () {
                $(".scroll-area").scrollTop(0);
                setScrollAreaHeight();
            }, 0);
        });
    }

    function billingDetailsController($scope, Billing) {

        activate();

        function activate() {
            Billing.getFeaturesPrices(function (prices) {
                $scope.prices = prices;
                console.log(prices);
            });
        }

    }

    function historyController($scope,
                               $rootScope,
                               $window,
                               $location,
                               $routeParams,
                               Security,
                               Billing,
                               BillingSecurity,
                               Navigation) {
        CommonLogger.setTags(["BILLING", "HISTORY-CONTROLLER"]);

        const PAY_PAL_NOTIFICATION_DIALOG_SELECTOR = "#pay-pal-notification-dialog";
        var payPalRedirectConfirmation = new Confirmation(PAY_PAL_NOTIFICATION_DIALOG_SELECTOR);

        BillingSecurity($scope);
        isUserAdmin(Security, function (result) {
            $scope.isAdmin = result;
        });
        Navigation.setShowInvoiceHistory(true);
        Navigation.setBackName("Billing");
        $rootScope.page = {
            title: "Chorus - Billing"
        };
        var lab = $routeParams.labId;

        $scope.previousCount = 0;
        $scope.previousMonth = null;

        $scope.history = [];
        $scope.pendingCharges = [];
        $scope.loadMoreHistory = loadMoreHistory;
        $scope.downloadHistory = downloadHistory;
        $scope.loadHistoryReference = loadHistoryReference;
        $scope.showDetails = showDetails;
        $scope.showPendingChargeDetails = showPendingChargeDetails;
        $scope.openSubscription = openSubscription;
        $scope.openProcessing = openProcessing;
        $scope.getPendingChargeDaySummary = getPendingChargeDaySummary;
        $scope.hasDetails = hasDetails;
        $scope.showPayPalRedirectNotification = showPayPalRedirectNotification;

        function loadMoreHistory() {

            function addToEndMonth(month) {
                var lines = $scope.history.months[$scope.history.months.length - 1].lines;
                $.each(month.lines, function (i, item) {
                    lines.push(item);
                });
            }

            function addNewMonths(month) {
                $scope.history.months.push(month);
                $scope.previousMonth = new Date(month.monthYear).getMonth();
            }

            $scope.loadingHistory = true;

            Billing.getPendingCharges({lab: lab}, function (response) {

                function findChargeDay(list, serverDate) {
                    for (var i = 0; i < list.length; ++i) {
                        if (list[i].serverDate == serverDate) {
                            return list[i];
                        }
                    }
                    return null;
                }


                var charges = response.value;
                var pendingCharges = [];
                var totalPendingCharge = 0;

                angular.forEach(charges, function (charge) {
                    var chargeDay = findChargeDay(pendingCharges, charge.serverDateFormatted);
                    if (!chargeDay) {
                        chargeDay = {
                            amount: 0,
                            timestamp: charge.timestamp,
                            serverDate: charge.serverDateFormatted,
                            charges: []
                        };
                        pendingCharges.push(chargeDay);
                    }
                    chargeDay.amount += charge.charge;
                    totalPendingCharge += charge.charge;
                    chargeDay.charges.push(charge);
                });

                $scope.pendingCharges = pendingCharges;
                $scope.showPendingCharges = totalPendingCharge != 0;

            });

            Billing.getStorageUsage({lab: lab}, function (response) {
                $scope.storageUsage = response.value;
            });

            Billing.moreHistory({lab: lab, previousCount: $scope.previousCount}, function (data) {

                if ($scope.previousMonth != null) {
                    if (data.months.length == 1) {
                        if (new Date(data.months[0].monthYear).getMonth() == $scope.previousMonth) {
                            addToEndMonth(data.months[0]);
                        } else {
                            addNewMonths(data.months[0]);
                        }
                    } else {
                        addToEndMonth(data.months[0]);
                        addNewMonths(data.months[1]);
                    }
                } else {
                    $scope.history = data;
                    $scope.previousMonth =
                        new Date($scope.history.months[$scope.history.months.length - 1].monthYear).getMonth();
                }

                $scope.currentMonthDate = new Date(data.months[data.months.length - 1].monthYear);
                $scope.previousCount += 7;
                $scope.currentHistoryItem = data;
                $scope.loadingHistory = false;
            });
        }


        function loadHistoryReference() {
            var date = new Date($scope.currentMonthDate);
            date.setMonth($scope.currentMonthDate.getMonth() - 1);
            Billing.moreMonthlyHistoryReference({lab: lab, month: date.getTime()}, function (data) {
                $scope.currentMonthDate = new Date(data.monthYear);
                $scope.history.months.push(data);
                $scope.currentMonth = data;
            });

        }

        function downloadHistory(month) {

            var path = subStringLastIndexOfFor(window.location.pathname, "/", 2) +
                "/billing/history/download?" + $.param({lab: lab, path: month.csvDataReference}, true);

            $.fileDownload(path, {
                failCallback: function () {
                    //fileDownloadErrorHandler("#file-download-message")
                    console.error("file download error"); // file download might fail because of absence such resources
                }
            });

        }


        function openSubscription() {
            $rootScope.dialogReturnUrl = $location.url();
            $location.url("/subscription/" + lab).replace();
        }

        function openProcessing() {
            $rootScope.dialogReturnUrl = $location.url();
            $location.url("/processing/" + lab).replace();
        }

        $scope.loadMoreHistory();
        $scope.lastMonthOpen = true;

        $scope.labId = lab;
        $scope.activeFilter = {
            filter: ""
        };
        $scope.details = Billing.labDetails({lab: lab});
        $scope.filterByDescription = function (line) {
            if ($scope.activeFilter.filter == "") {
                return true;
            }
            if ($scope.activeFilter.filter == "Charge") {
                return line.description == "Charge from credit card" || line.description == "Charge from balance";
            }
            return line.description == $scope.activeFilter.filter;
        };
        $scope.openMonth = function (month) {
            month.selected = !month.selected;
            $scope.lastMonthOpen =
                !(!month.selected && $scope.history.months.indexOf(month) == $scope.history.months.length - 1);
        };

        function showPendingChargeDetails(pendingCharge) {
            pendingCharge.showDetails = !pendingCharge.showDetails;
        }

        function getPendingChargeDaySummary(dayCharge) {
            var summary = "";
            var chargesWithAmount = $.grep(dayCharge.charges, function (c) {
                return c.charge > 0;
            });
            for (var i = 0; i < chargesWithAmount.length; ++i) {
                var charge = chargesWithAmount[i];
                summary += charge.feature + " Monthly Charge";
                if (i < chargesWithAmount.length - 1) {
                    summary += ", ";
                }
            }
            return summary;
        }

        function hasDetails(pendingChargeDay) {
            var chargesWithAmount = $.grep(pendingChargeDay.charges, function (c) {
                return c.charge > 0;
            });
            if (pendingChargeDay.amount == 0
                || chargesWithAmount.length == 0
                || chargesWithAmount.length == 1 && chargesWithAmount[0].feature == "Processing") {
                return false;
            }
            return true;
        }

        function showDetails(line) {
            line.showDetails = !line.showDetails;
            var dateFrom = new Date(line.toDate);
            var dateTo = new Date(line.date);
            if (!line.invoice) {
                Billing.getInvoice({
                    lab: $scope.labId,
                    dateFrom: dateFrom.getTime(),
                    dateTo: dateTo.getTime()
                }, function (responce) {
                    line.invoice = responce;
                });
            }
        }

        function showPayPalRedirectNotification() {
            payPalRedirectConfirmation.showPopup();
        }
    }

    function listController($scope,
                            $rootScope,
                            $window,
                            $location,
                            Billing,
                            Security,
                            expandMenuLab,
                            BillingSecurity,
                            Navigation) {
        CommonLogger.setTags(["BILLING", "LIST-CONTROLLER"]);
        BillingSecurity($scope);
        Navigation.setShowInvoiceHistory(false);
        Navigation.setBackCallback(function () {
            $location.url("/list").replace();
        });
        Navigation.setBackName("Labs");
        Billing.list(function (items) {
            $scope.labs = items;
            $scope.total = items.length;
        });
        $scope.sorting = {
            field: "labName",
            reverse: false
        };
        $rootScope.page = {
            title: "Chorus - Labs"
        };
        $scope.labOrder = false;
        $scope.changeLabOrder = function () {
            $scope.labOrder = !$scope.labOrder;
        };

        expandMenuLab($scope);
        $scope.openBilling = function (lab) {
            $location.url("/history/" + lab.labId).replace();
        };
    }

    function adminListController($scope,
                                 $rootScope,
                                 $window,
                                 $location,
                                 Billing,
                                 Security,
                                 expandMenuLab,
                                 Navigation,
                                 BillingSecurity,
                                 contentRequestParameters,
                                 PaginationPropertiesSettingService) {
        CommonLogger.setTags(["BILLING", "ADMIN-LIST-CONTROLLER"]);
        BillingSecurity($scope);
        Navigation.setShowInvoiceHistory(false);
        Navigation.setBackCallback(function () {
            $location.url("/labInvoices").replace();
        });
        Navigation.setBackName("Labs");
        var pageRequest = contentRequestParameters.getParameters("labInvoices");
        CommonLogger.log(pageRequest);
        $rootScope.page = {};
        $rootScope.page.filterScope = $scope;
        $rootScope.page.showPageableFilter = false;
        $rootScope.page.title = "Chorus - Labs";

        $scope.isAdmin = true;

        $scope.sorting = {field: "storeBalance", reverse: false};
        $scope.page.filter = pageRequest.filterQuery;

        function listLabs() {
            Billing.listAll(pageRequest, function (response) {
                $scope.labs = response.items;
                PaginationPropertiesSettingService.setPaginationProperties($scope, response);
            });
        }

        listLabs();

        expandMenuLab($scope);
        $scope.openBilling = function (lab) {
            $location.url("/history/" + lab.labId).replace();
        };

        $scope.showAdminTopUpBalanceDialog = setupAdminTopUpBalanceDialog;

        function setupAdminTopUpBalanceDialog(lab) {
            $scope.adminTopUpBalance = new Confirmation("#admin-topup-lab-balance", lab, {
                success: function () {
                    var request = {
                        lab: $scope.adminTopUpBalance.item.labId,
                        amount: $scope.adminTopUpBalance.amount * 100
                    };
                    Billing.adminTopUp(request, function () {
                        $scope.adminTopUpBalance.item.storeBalance += request.amount;
                        $scope.adminTopUpBalance.hidePopup();
                        $scope.adminTopUpBalance.confirmation.showPopup();
                    });
                }
            });
            $scope.adminTopUpBalance.confirmation = new Confirmation("#admin-top-up-confirmation", null, {
                success: function () {
                    $scope.adminTopUpBalance.confirmation.hidePopup();
                }
            });
            $scope.adminTopUpBalance.amount = undefined;
            $scope.adminTopUpBalance.showPopup();
        }
    }

    function navigationController($scope, $window, $route, $routeParams, $location, Billing, Navigation) {

        CommonLogger.setTags(["BILLING", "NAVIGATION-CONTROLLER"]);

        $scope.showButtons = Navigation;

        $scope.$watch(function () {
            return $route.current.params.labId;
        }, function (newId) {
            if (!newId) {
                $scope.lab = null;
                $scope.labName = "Billing Labs";
                return;
            }
            $scope.lab = $route.current.params.labId;
            Billing.labDetails({lab: $scope.lab}, function (result) {
                $scope.labName = result.labName;
            });
        });
        $scope.$on("$routeChangeSuccess", function () {
            $scope.lab = $routeParams.labId;
        });

    }

    function usageTableController(expandMenuChargeItem, $filter) {
        return {
            restrict: "E",
            templateUrl: "../pages/billing/usage-table.html",
            scope: {
                invoice: "=",
                isAdmin: "=",
                line: "="
            },
            controller: function ($scope) {

                const WITH_USAGES_FEATURE_TYPES = ["Translation", "Protein ID Search", "Public data download", "Download", "Daily archive storage", "Daily active storage"];
                const MONTHLY_CHARGED_FEATURE_TYPES = ["Processing", "Storage", "Archive storage"];
                const PROCESSING_FEATURE = "Processing";
                const FILES_SIZE_FILTER = $filter("fileSize");

                expandMenuChargeItem($scope);
                $scope.storageBillSelected = false;
                $scope.translationBillSelected = false;
                $scope.selectStorage = function () {
                    $scope.storageBillSelected = !$scope.storageBillSelected;
                };
                $scope.selectTranslation = function () {
                    $scope.translationBillSelected = !$scope.translationBillSelected;
                };
                $scope.getBalance = function (featureItem) {
                    if (featureItem.balance !== 0 && !featureItem.balance) {
                        return $scope.line.balance;
                    }
                    return featureItem.balance;
                };
                $scope.getFeatureItemDescription = function (item) {
                    if (MONTHLY_CHARGED_FEATURE_TYPES.indexOf(item.type) >= 0) {
                        if (item.type == PROCESSING_FEATURE) {
                            return " (enabled for one month)";
                        }
                        return " (" + item.totalLoggedChargeValue + " volume" + (item.totalLoggedChargeValue != 1 ?
                            "s" :
                            "") + " used in the previous month)";
                    }

                    var itemDescription = " (";
                    itemDescription += item.totalUsers;
                    itemDescription += item.totalUsers == 1 ? " user " : " users ";
                    itemDescription += item.totalFiles;
                    itemDescription += " files ";
                    itemDescription += FILES_SIZE_FILTER(item.totalLoggedChargeValue);
                    itemDescription += " size)";
                    return itemDescription;

                };
                $scope.itemWithUsages = function (item) {
                    return WITH_USAGES_FEATURE_TYPES.indexOf(item.type) >= 0;
                };
                $scope.message = "";

            }
        };
    }

    function chargesTableController($filter) {
        return {
            restrict: "E",
            templateUrl: "../pages/billing/charges-table.html",
            scope: {
                charges: "="
            },
            controller: function ($scope) {
                console.log("charges table", $scope.charges);
                const FILES_SIZE_FILTER = $filter("fileSize");

                $scope.getChargeItemDescription = function (charge) {
                    if (charge.feature == "Processing") {
                        return "Processing: for one month";
                    }

                    let size = FILES_SIZE_FILTER(charge.sizeInBytes);
                    if (charge.feature == "Storage") {
                        return "Active Storage: " + charge.featureAmountUsed + " volume" +
                            (charge.featureAmountUsed != 1 ? "s" : "") + " (" + size + ")";
                    } else if (charge.feature == "Archive storage") {
                        return "Archive Storage: " + charge.featureAmountUsed + " volume" +
                            (charge.featureAmountUsed != 1 ? "s" : "") + " (" + size + ")";
                    }
                    return "No Description";


                };
                $scope.message = "";
            }
        };
    }

    function usageItemController(expandMenuChargeItem) {
        return {
            restrict: "E",
            templateUrl: "../pages/billing/usage-item.html",
            scope: {
                bill: "=",
                showTime: "="
            },
            controller: function ($scope) {
                $scope.usageItemOrder = false;
                $scope.changeUsageItemOrder = function () {
                    $scope.usageItemOrder = !$scope.usageItemOrder;
                };
                $scope.userOrder = false;
                $scope.changeUserOrder = function () {
                    $scope.userOrder = !$scope.userOrder;
                };
                expandMenuChargeItem($scope);
            }

        };
    }

    function BillingSecurity($window, $routeParams, Security) {
        return function checkIsCurrentUserCanView() {
            isUserAdmin(Security, function (result) {
                if (!result) {
                    Security.isLabHead(function (result) {
                        if (!result.value) {
                            $window.location.replace($window.location.origin + defaultPage);
                        }
                    });
                }
            });
        };
    }

    function processingManagementController($scope, $routeParams, Billing) {
        console.log("process management");

        $scope.step = 1;
        $scope.save = save;
        $scope.next = next;
        $scope.back = back;
        $scope.getProcessingFormattedInfo = getProcessingFormattedInfo;
        $scope.getProcessingProlongationFormattedInfo = getProcessingProlongationFormattedInfo;
        $scope.onProcessingEnabledChanged = onProcessingEnabledChanged;
        $scope.getTotalPrice = getTotalPrice;
        $scope.labInfo = {};
        const PROCESSING_PROPERTY_NAME = "PROCESSING_FEATURE_COST";
        $scope.processingFeaturePrice = 0;


        activate();

        function activate() {
            Billing.labDetails({lab: $routeParams.labId}, function (details) {
                var processingFeature = details.labAccountFeatures.filter(function (feature, index) {
                    return feature.name === "PROCESSING";
                })[0];

                $scope.labInfo = details;
                $scope.details = {
                    labId: $routeParams.labId,
                    labName: details.labName,
                    accountType: details.accountType,
                    processingEnabled: processingFeature != undefined && processingFeature.active,
                    autoprolongateProcessing: processingFeature != undefined && processingFeature.autoProlongate
                };

                $scope.canChangeProcessing = !processingFeature || !processingFeature.active;
            });
            Billing.getBillingProperties({}, function (response) {
                $scope.processingFeaturePrice = response[PROCESSING_PROPERTY_NAME];
            });
        }

        function getProcessingFormattedInfo() {
            if (!$scope.details) {
                return;
            }
            return $scope.details.processingEnabled ? "Enabled" : "Disabled";
        }

        function getProcessingProlongationFormattedInfo() {
            if (!$scope.details) {
                return;
            }
            return $scope.details.autoprolongateProcessing ? "Yes" : "No";
        }

        function onProcessingEnabledChanged(newValue) {
            if (!newValue) {
                $scope.details.autoprolongateProcessing = false;
            }
        }

        function getTotalPrice() {
            if (!$scope.details) {
                return 0;
            }
            return $scope.details.processingEnabled ? $scope.processingFeaturePrice : 0;
        }

        function next() {
            $scope.step++;
        }

        function back() {
            $scope.step--;
        }

        function save() {
            Billing.updateProcessingFeatureState($scope.details, function () {
                hideModal();
            });
        }
    }

    function subscriptionController($scope, $routeParams, Billing) {
        console.log("subscription", $routeParams);

        $scope.step = 1;
        $scope.accountTypes = ["FREE", "ENTERPRISE"];
        var billingPropertyNames = {
            "FREE_ACCOUNT_STORAGE_LIMIT": "FREE_ACCOUNT_STORAGE_LIMIT",
            "ENTERPRISE_ACCOUNT_STORAGE_VOLUME_SIZE": "ENTERPRISE_ACCOUNT_STORAGE_VOLUME_SIZE",
            "ENTERPRISE_ACCOUNT_STORAGE_VOLUME_COST": "ENTERPRISE_ACCOUNT_STORAGE_VOLUME_COST",
            "FREE_ACCOUNT_ARCHIVE_STORAGE_LIMIT": "FREE_ACCOUNT_ARCHIVE_STORAGE_LIMIT",
            "ENTERPRISE_ACCOUNT_ARCHIVE_STORAGE_VOLUME_COST": "ENTERPRISE_ACCOUNT_ARCHIVE_STORAGE_VOLUME_COST",
            "ENTERPRISE_ACCOUNT_ARCHIVE_STORAGE_VOLUME_SIZE": "ENTERPRISE_ACCOUNT_ARCHIVE_STORAGE_VOLUME_SIZE"
        };

        $scope.save = save;
        $scope.next = next;
        $scope.back = back;
        $scope.getStorageVolumeCost = getStorageVolumeCost;
        $scope.getStorageVolumeSize = getStorageVolumeSize;
        $scope.getArchiveStorageVolumeCost = getArchiveStorageVolumeCost;
        $scope.getArchiveStorageVolumeSize = getArchiveStorageVolumeSize;
        $scope.getFreeStorageSize = getFreeStorageSize;
        $scope.labInfo = {};
        $scope.billingProperties = {};

        activate();

        function activate() {
            Billing.labDetails({lab: $routeParams.labId}, function (details) {
                $scope.labInfo = details;
                $scope.details = {
                    labId: $routeParams.labId,
                    labName: details.labName,
                    accountType: details.accountType
                };
                $scope.accountType = details.accountType;
            });
            Billing.getBillingProperties({}, function (response) {
                $scope.billingProperties = response;
            });
            Billing.checkCanMakeAccountFree({lab: $routeParams.labId}, function (response) {
                $scope.toFreeAccountCheckResult = response.value;
            });
        }

        function next() {
            $scope.step++;
        }

        function back() {
            $scope.step--;
        }

        function save() {
            if ($scope.accountType != $scope.details.accountType) {
                var requestFn = $scope.details.accountType == $scope.accountTypes[0] ?
                    Billing.makeAccountFree :
                    Billing.makeAccountEnterprise;
                var request = {
                    labId: $routeParams.labId
                };
                requestFn(request, function () {
                    hideModal();
                });
            } else {
                hideModal();
            }
        }

        function getFreeStorageSize() {
            return $scope.billingProperties[billingPropertyNames.FREE_ACCOUNT_STORAGE_LIMIT];
        }

        function getStorageVolumeCost() {
            return $scope.billingProperties[billingPropertyNames.ENTERPRISE_ACCOUNT_STORAGE_VOLUME_COST];
        }

        function getStorageVolumeSize() {
            return $scope.billingProperties[billingPropertyNames.ENTERPRISE_ACCOUNT_STORAGE_VOLUME_SIZE];
        }

        function getArchiveStorageVolumeCost() {
            return $scope.billingProperties[billingPropertyNames.ENTERPRISE_ACCOUNT_ARCHIVE_STORAGE_VOLUME_COST];
        }

        function getArchiveStorageVolumeSize() {
            return $scope.billingProperties[billingPropertyNames.ENTERPRISE_ACCOUNT_ARCHIVE_STORAGE_VOLUME_SIZE];
        }

    }

    function Navigation() {
        return {
            showInvoiceHistory: false,
            setShowInvoiceHistory: function (value) {
                this.showInvoiceHistory = value;
            },
            getShowInvoiceHistory: function () {
                return this.showInvoiceHistory;
            },
            onBack: function () {
                window.location.href = "../pages/dashboard.html";
            },
            setBackCallback: function (callback) {
                this.onBack = callback;
            },
            setBackName: function (string) {
                this.backName = string;
            }
        };
    }

    function expandMenuLab(Billing) {
        return initExpandMenu(function (item) {
            Billing.labDetails({lab: item.labId}, function (response) {
                item.details = response;
            });
        });
    }

    function expandMenuChargeItem(Billing) {
        return initExpandMenu(function (item) {

        });
    }

    function filePrice() {
        return function (input) {
            var price = parseFloat(input);
            if (price == 0) {
                return "less than $0.01";
            }
            return convertPrice(input);
        };
    }

    function price() {
        return function (input) {
            return convertPrice(input);
        };
    }

    function formatDate() {
        var monthNames = ["Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
        return function (input) {
            var date = new Date(input);
            var addZero = function (number) {
                if (number < 10) {
                    return "0" + number;
                }
                return number;
            };

            var formatHours = function (hours) {
                if (hours > 12) {
                    return hours - 12;
                }
                return hours;
            };
            var amPm = function (hours) {
                if (hours > 12) {
                    return "pm";
                }
                return "am";
            };
            return date.getDate() + " "
                + monthNames[date.getMonth()] + " "
                + date.getFullYear() + " "
                + addZero(formatHours(date.getHours())) + ":"
                + addZero(date.getMinutes()) + " " +
                amPm(date.getHours());
        };
    }

    function convertPrice(cents) {
        if (cents == undefined /*|| cents < 0*/) {
            return " ";
        }
        var result = (parseFloat(cents) / 100).toFixed(2);
        return result < 0 ? "-$" + Math.abs(result) : "$" + result;
    }

    function formatMonth() {
        var monthNames = ["January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"];
        return function (input) {
            var date = new Date(input);
            return monthNames[date.getMonth()] + " "
                + date.getFullYear();
        };
    }

    function featureActivity() {
        return function (input) {
            if (input) {
                return "Enabled";
            }

            return "Disabled";
        };
    }

    function toDays() {
        return function (hours) {
            if (hours >= 24) {
                return (parseFloat(hours) / 24).toFixed(1);
            }

            return hours + " h";
        };
    }

    function isUserAdmin(Security, callback) {
        Security.get({path: ""}, function (user) {
            var isAdmin = false;
            if (user.id != null) {
                var roles = user.authorities;
                var filteredAdminRoles = $.grep(roles, function (role) {
                    return role.authority == "ROLE_admin";
                });
                CommonLogger.log(filteredAdminRoles.length > 0 + " admin");
                isAdmin = filteredAdminRoles.length > 0;
            } else {
                isAdmin = undefined;
            }
            callback(isAdmin);
        });
    }

    function subStringLastIndexOfFor(string, searchString, times) {
        var result = string;
        for (var i = 0; i < times; i++) {
            result = result.substring(0, result.lastIndexOf(searchString));
        }
        return result;
    }

    function storageVolumeSize() {
        return function (input) {
            return (input / Math.pow(1024, 3)).toFixed(0) + " GB";
        };
    }
})();
