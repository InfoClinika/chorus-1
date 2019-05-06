"use strict";

$(document).ready(function () {
    setScrollAreaHeight();
    initInterface();
    $(window).resize(setScrollAreaHeight);
});

function setScrollAreaHeight() {
    var scrollArea = $("#scroll-area");
    var sideBar = $(".main-holder .sidebar");
    var mainViewHolder = $(".dashboard-box");
    var offset = parseInt(scrollArea.css("margin-top"));
    var newHeight = $(window).height() - offset;
    if (scrollArea.height() != newHeight) {
        scrollArea.css("height", newHeight + "px");
    }
    var footerOuterHeight = $("#footer").outerHeight();
    sideBar.css("height", newHeight - footerOuterHeight + "px");
    var paginationHeight = $(".pagination-box").outerHeight();
    mainViewHolder.css("height", newHeight - footerOuterHeight - paginationHeight + "px");
}

function initInterface() {
    //updateScrollAreaMinHeight();
    //bindExpandAnimation();
    bindScrollEvents();
}

function bindExpandAnimation() {
    $(".expand-switcher").bind("click", function () {
        $(this).unbind("click");
        var destList = $(this).next("ul");
        destList.slideToggle(300, function () {
            bindExpandAnimation();
            /*updateScrollAreaMinHeight();*/
        });
        $(this).toggleClass("collapsed");
        return false;
    });
}

function bindScrollEvents() {
    var objectToWatch = $(".table-shadow");
    if (!objectToWatch.offset()) {
        return;
    }
    var initialVertOffset = objectToWatch.offset().top;
    $("#scroll-area").scroll(function (e) {
        if (objectToWatch.offset().top < initialVertOffset) {
            $(".fixed-area").addClass("drop-shadow");
        } else {

            $(".fixed-area").removeClass("drop-shadow");
        }
    });
}

function loading($http) {
    return {
        restrict: "A",
        link: function (scope, elm, attrs) {
            scope.isLoading = function () {
                return $http.pendingRequests.length > 0;
            };

            scope.$watch(scope.isLoading, function (v) {
                if (v) {
                    elm.show();
                } else {
                    elm.hide();
                }
            });
        }
    };
}

angular.module("front-end", ["error-catcher"])
    .directive("loading", loading)
    .directive("ellipsize", function () {
        return function (scope, element, attr) {
            var elementHeight = scope.$eval(attr.ellipsize);
            if (!elementHeight && attr.ellipsize !== "auto") {
                elementHeight = $(element).height();
            }
            $(element).dotdotdot({
                wrap: "letter",
                watch: true,
                height: elementHeight
            });
            setTimeout(function () {
                $(element).trigger("update");
            }, 0);
        };
    })
    .filter("truncate", function () {
        return function (input, symbols) {
            if (input && input.length > symbols) {
                return input.substring(0, symbols) + "...";
            }
            return input;
        };
    })
    .filter("nullToEmptyString", function () {
        return function (input) {
            if (input == null || input == undefined) {
                return "";
            }
            return input;
        };
    })
    .factory("formatInstrument", function () {
        return function (item) {
            var text = item.text;
            var title = item.element[0].title;
            return "<span class='select2-results'>" + text + "</span><span class='instrument-sn'>" + title + "</span>";
        };
    })
    .factory("hideContextMenus", function () {
        return function () {
            $(".context-menu")
                .removeClass("open-context-menu")
                .css("display", "none");
        };
    })
    .factory("contextMenuHelper", function () {

        function getFullElementHeight(element) {
            return element.height() + parseInt(element.css("padding-top"))
                + parseInt(element.css("padding-bottom"))
                + parseInt(element.css("margin-bottom"))
                + parseInt(element.css("margin-top"));
        }

        function handleArrowHover(child, arrow, arrowClass) {
            var hoveredChild = $(".context-menu.open-context-menu li:" + child);
            hoveredChild.unbind("hover").hover(function () {
                CommonLogger.log("add class: " + arrowClass);
                arrow.addClass(arrowClass);
            }, function () {
                CommonLogger.log("remvove class: " + arrowClass);
                arrow.removeClass(arrowClass);
            });

            arrow.unbind("hover").hover(function () {
                hoveredChild.addClass("hovered");
            }, function () {
                hoveredChild.removeClass("hovered");
            });
        }

        return function (element, event) {

            var contextMenu = $(element);
            contextMenu.addClass("open-context-menu");

            var offset = getFullElementHeight($("#footer"));
            var height = $(".context-menu.open-context-menu").height();
            var arrow = $(".context-menu.open-context-menu .arrow");
            arrow.removeClass("context-menu-arrow-down context-menu-arrow-up");

            var child = "first-child";
            var arrowClass = "context-menu-arrow-up";

            if (height + event.pageY + offset > $(window).height()) {
                arrowClass = "context-menu-arrow-down";
                child = "last-child";
                contextMenu.addClass("context-menu-top");
            }

            handleArrowHover(child, arrow, arrowClass);

            return {
                topPosition: function (relativePos, heigh) {

                    var offset = getFullElementHeight($("#footer"));
                    var height = $(".context-menu.open-context-menu").height();

                    var arrowHeight = $(".context-menu.open-context-menu .arrow").height();

                    var top = relativePos + heigh || event.pageY;
                    if (height + event.pageY + offset > $(window).height()) {
                        top = (relativePos ? relativePos : event.pageY) - height - 2 * arrowHeight;
                        child = "last-child";
                        contextMenu.addClass("context-menu-top");
                    }

                    return top;
                },
                leftPosition: function () {
                    var left = event.pageX;
                    var width = $(".context-menu.open-context-menu .arrow").width();
                    return left - width / 2;
                }
            };
        };
    })
    .factory("contentTypes", function () {
        return [
            "projects",
            "experiments",
            "processing-runs",
            "labInvoices",
            "files",
            "instruments",
            "groups",
            "laboratories",
            "translation",
            "file-access-log",
            "scripts",
            "instrument-models",
            "issues"
        ];
    })
    .factory("currentContentType", function ($location, contentTypes) {
        return function () {
            var types = contentTypes;
            var parts = $location.path().split("/").reverse();
            var type = "";
            for (var i = 0; i < parts.length; ++i) {
                if (types.lastIndexOf(parts[i]) >= 0) {
                    type = parts[i];
                    break;
                }
            }
            return type;
        };
    })
    .factory("currentContentTypeParams", function (contentRequestParameters, currentContentType) {
        return function () {
            return contentRequestParameters.getParameters(currentContentType());
        };
    })
    .factory("contentRequestParameters", function (contentTypes, $route, $location) {

        var types = contentTypes;
        var lastPath = null;

        var defaultParameters = {
            defaults: {
                page: 1,
                items: 25,
                sortingField: "name",
                asc: true,
                filterQuery: ""
            },
            projects: {
                sortingField: "modified",
                asc: false
            },
            experiments: {
                sortingField: "modified",
                asc: false
            },
            "processing-runs": {
                sortingField: "date",
                asc: false
            },
            files: {
                sortingField: "uploadDate",
                asc: false
            },
            instruments: {},
            groups: {},
            laboratories: {},
            labInvoices: {items: 1000},
            translation: {
                sortingField: "modified",
                asc: false
            },
            "file-access-log": {
                sortingField: "operationDate",
                asc: false
            },
            "scripts": {
                sortingField: "creationDate",
                asc: false
            },
            "instrument-models": {},
            "issues": {
                sortingField: "id",
                asc: false
            }
        };

        var parametersHolder = {
            projects: {},
            labInvoices: {},
            experiments: {},
            "processing-runs": {},
            files: {},
            instruments: {},
            "instrument-models": {},
            groups: {},
            laboratories: {},
            translation: {},
            "file-access-log": {},
            "scripts": {},
            "issues": {}
        };

        function toDefault() {
            for (var i = 0; i < types.length; i++) {
                var type = types[i];
                //get parameters holder object for the type
                parametersHolder[type] = {};
                //get default parameters object for the type
                var typeDefaults = defaultParameters[type];
                //get common defaults object (contains all props)
                var commonDefaults = defaultParameters.defaults;
                var obj = {};
                for (var prop in commonDefaults) {
                    obj[prop] = typeDefaults[prop] != undefined ? typeDefaults[prop] : commonDefaults[prop];
                }
                parametersHolder[type] = obj;
            }
        }

        //set all to default when initialize
        toDefault();

        return {
            getParameters: function (type) {
                var locPath = $location.path();
                if (lastPath == null) {
                    lastPath = locPath;
                }
                if (lastPath != locPath) {
                    lastPath = locPath;
                    toDefault();
                }
                var params = parametersHolder[type];
                if (params) {
                    params.paged = "paged";
                    params.filter = $route.current.params.filter;
                }

                // restore filter query
                var query = $location.search().q;
                if (query != undefined && query != null) {
                    params.filterQuery = query;
                } else {
                    params.filterQuery = "";
                }
                return params;
            },
            setParameters: function (type, params) {
                var typeParamObject = parametersHolder[type] || {};
                for (var prop in params) {
                    typeParamObject[prop] = params[prop];
                }
            }
        };
    })
    .factory("columnsEditor", function () {
        return function ($scope, Columns) {
            function ColumnsManager() {
                //visible columns default set
                this._defaultColumns = [];

                //visible columns saved set
                this._savedColumns = [];

                //all possible columns list
                this._availableColumns = [];

                //initial order/visibility of all columns
                this._initialColumns = [];

                //current order/visibility of all columns
                //ngModel
                this.currentColumns = [];

                this._defaultColumnsLoaded = false;
                this._savedColumnsLoaded = false;
                this._initialColumnsLoaded = false;
                this._availableColumnsLoaded = false;

                ColumnsManager.prototype.loadDefaultColumns = function (callback) {
                    return this.__loadResource("defaultColumns", "_defaultColumnsLoaded", "_defaultColumns", callback);
                };

                ColumnsManager.prototype.loadCurrentColumns = function (callback) {
                    return this.__loadResource("selectedColumnSet", "_savedColumnsLoaded", "_savedColumns", callback);
                };

                ColumnsManager.prototype.loadAvailableColumns = function (callback) {
                    return this.__loadResource("available", "_availableColumnsLoaded", "_availableColumns", callback);
                };

                ColumnsManager.prototype.setInitialColumns = function (columns) {
                    this._initialColumns = columns;
                    this._initialColumnsLoaded = true;
                };

                ColumnsManager.prototype.isDefaultColumnsLoaded = function () {
                    return this._defaultColumnsLoaded;
                };

                ColumnsManager.prototype.isSavedColumnsLoaded = function () {
                    return this._savedColumnsLoaded;
                };

                ColumnsManager.prototype.isAvailableColumnsLoaded = function () {
                    return this._availableColumnsLoaded;
                };

                ColumnsManager.prototype.isInitialColumnsLoaded = function () {
                    return this._initialColumnsLoaded;
                };

                ColumnsManager.prototype.getDefaultColumns = function () {
                    return this._defaultColumns;
                };

                ColumnsManager.prototype.getAvailableColumns = function () {
                    return this._availableColumns;
                };

                ColumnsManager.prototype.getInitialColumns = function () {
                    return this._initialColumns;
                };

                ColumnsManager.prototype.getSavedColumns = function () {
                    return this._savedColumns;
                };

                ColumnsManager.prototype.isAllLoaded = function () {
                    return this.isDefaultColumnsLoaded() && this.isSavedColumnsLoaded() &&
                        this.isAvailableColumnsLoaded() && this.isInitialColumnsLoaded();
                };

                ColumnsManager.prototype.isDirty = function () {
                    var initiallySelected = $.grep(this.getInitialColumns(), function (col) {
                        return col.visible;
                    });
                    var currentlySelected = $.grep(this.currentColumns, function (col) {
                        return col.visible;
                    });

                    var toArray = function (cols) {
                        var names = [];
                        for (var idx in cols) {
                            names.push(cols[idx].name);
                        }
                        return names;
                    };

                    if (initiallySelected.length != currentlySelected.length) {
                        return true;
                    }
                    for (var idx in initiallySelected) {
                        var c1 = initiallySelected[idx];
                        var c2 = currentlySelected[idx];
                        if (c1.name != c2.name) {
                            return true;
                        }
                    }
                    return false;
                };

                ColumnsManager.prototype.isDefault = function () {
                    var idx = 0;
                    //Column set is similar to Default when
                    //its first defaultColumns.length columns are checked and have similar order as in the Default set
                    for (; idx < this.getDefaultColumns().length; idx++) {
                        var c1 = this.getDefaultColumns()[idx];
                        var c2 = this.currentColumns[idx];
                        if (c1.name != c2.name || !c2.visible) {
                            return false;
                        }
                    }
                    //...and when none other column is checked.
                    for (; idx < this.currentColumns.length; idx++) {
                        if (this.currentColumns[idx].visible) {
                            return false;
                        }
                    }
                    return true;
                };

                ColumnsManager.prototype.restoreDefault = function () {
                    this.composeColumnsList(this.getDefaultColumns());
                };

                ColumnsManager.prototype.save = function () {
                    var selected = $.grep(this.currentColumns, function (col) {
                        return col.visible;
                    });
                    var counter = 0;
                    var columns = $.map(selected, function (item) {
                        return {
                            columnId: item.id,
                            order: counter++
                        };
                    });
                    Columns.save({name: "primary", isPrimary: true, columns: columns}, function () {
                        hideModal();
                    });
                };

                /**
                 * Creates ordered list of all available columns and stores it in this.currentColumns.
                 * Specifies which of these columns are visible
                 *
                 * The columns list is composed in the following way:
                 * 1. process "visible" columns (which are passed through @checkedColumns parameter)
                 * 2. add not "hideable" visible columns to the list first
                 * 3. add the rest of visible columns
                 * 4. set "visible" property for all added columns to "true"
                 * 5  add columns, which are not yet added, to this list.
                 *
                 * @param checkedColumns an ordered list of columns which should be visible
                 */
                ColumnsManager.prototype.composeColumnsList = function (checkedColumns) {
                    var columnsList = [];
                    var nonHideableColumns = $.grep(checkedColumns, function (column) {
                        return column.hideable == false;
                    });
                    columnsList = columnsList.concat(nonHideableColumns);

                    var hideableColumns = $.grep(checkedColumns, function (column) {
                        return column.hideable != false;
                    });
                    columnsList = columnsList.concat(hideableColumns);
                    $.each(columnsList, function (idx, col) {
                        col.visible = true;
                        if (col.id == undefined) {
                            //for some reason "id" field is mapped to "originalColumn" when we retrieve columns list from a server,
                            //but when we perform POST-requests, the server expects it to be called "id"
                            col.id = col.originalColumn;
                        }
                    });

                    var notPresentInAvailableColumns = $.grep(this.getAvailableColumns(), function (column) {
                        var matchingColumns = $.grep(columnsList, function (presentColumn) {
                            return presentColumn.name === column.name;
                        });
                        return matchingColumns.length == 0;
                    });
                    $.each(notPresentInAvailableColumns, function (idx, col) {
                        col.visible = false;
                    });

                    columnsList = columnsList.concat(notPresentInAvailableColumns);
                    this.currentColumns = columnsList;
                };

                ColumnsManager.prototype.__loadResource =
                    function (resourceUrl, resourceLoadedFlagName, resourceReferenceName, callback) {
                        var self = this;
                        if (!self[resourceLoadedFlagName]) {
                            Columns[resourceUrl](function (columns) {
                                self[resourceReferenceName] = columns;
                                self[resourceLoadedFlagName] = true;
                                if (callback) {
                                    callback(self[resourceReferenceName]);
                                }
                            });
                        } else if (callback) {
                            callback(self[resourceReferenceName]);
                        }
                    };
            }

            //init ColumnsManager
            var columnsManager = new ColumnsManager();
            columnsManager.loadDefaultColumns(function () {
                columnsManager.loadAvailableColumns(function () {
                    columnsManager.loadCurrentColumns(function () {
                        columnsManager.composeColumnsList(columnsManager.getSavedColumns());
                        //copy availableColumns names/visibility for futher verification on "dirtyness"
                        var initialColumns = [];
                        for (var idx in columnsManager.currentColumns) {
                            initialColumns.push({
                                name: columnsManager.currentColumns[idx].name,
                                visible: columnsManager.currentColumns[idx].visible
                            });
                        }
                        columnsManager.setInitialColumns(initialColumns);
                    });
                });
            });
            $scope.cm = columnsManager;

            $scope.sortableOptions = {
                items: "li:not(.ui-state-disabled)"
            };
        };
    })
    .factory("changeableColumnsHelper", function () {

        var scrollBoardUnits = 100;
        var defaultColumnStyle = {};

        return function ($scope, Columns) {

            var totalUnits = 0;
            var defaultViewWidth = 1130;//px

            $scope.$on("mainViewChanged", function (e, val) {
                if (totalUnits <= scrollBoardUnits) {
                    return;
                }
                var newWidth = parseInt(val) * totalUnits / scrollBoardUnits;
                $scope.viewStyle = {
                    width: newWidth + "px"
                };
                $scope.viewStyleContent = {
                    height: $(".sidebar").outerHeight() + "px",
                    width: $scope.viewStyle.width
                };
                $scope.$apply();
            });

            $scope.displayedColumnNames = [];
            $scope.hidableColumns = [];
            $scope.viewStyle = {};
            $scope.viewStyleContent = {};


            $scope.getStyle = function (index) {
                var item = $scope.displayedColumnNames[index];
                if (!item) {
                    return defaultColumnStyle;
                }
                return item.style;
            };

            $scope.compoundColumnStyles = function (styles) {
                var width = 0;
                $.each(styles, function (i, val) {
                    width += parseFloat(val.width);
                });
                return {
                    width: width + "%"
                };
            };

            function checkUnits(columns) {
                $.each(columns, function (i, col) {
                    totalUnits += col.units;
                });

                function recalculateUnits() {
                    var ratio = scrollBoardUnits / totalUnits;
                    $.each(columns, function (i, col) {
                        col.units = col.units * ratio;
                    });
                }

                if (totalUnits > scrollBoardUnits) {
                    var newWidth = defaultViewWidth * totalUnits / scrollBoardUnits;
                    recalculateUnits();
                    $scope.viewStyle = {
                        width: newWidth + "px"
                    };
                    $scope.viewStyleContent = {
                        height: $(".sidebar").outerHeight() + "px",
                        width: $scope.viewStyle.width
                    };
                }
                if (totalUnits < scrollBoardUnits) {
                    recalculateUnits();
                }

                $.each(columns, function (i, col) {
                    col.style = {
                        width: col.units + "%"
                    };
                });
            }

            function processColumns(columns) {
                $.each(columns, function (i, val) {
                    //CommonLogger.log(val);
                    val.title = val.name;
                });
                checkUnits(columns);
                $scope.displayedColumnNames = columns;
                $scope.hidableColumns = $.grep(columns, function (col) {
                    return col.hideable;
                });
            }

            Columns.selectedColumnSet(processColumns);

            $scope.getCellValue = function (columnIndex, item) {
                var value = item.columns[$scope.hidableColumns[columnIndex].modelViewName];
                return value || "";
            };

            $scope.getCellTitle = function (columnIndex, item) {
                return $scope.getCellValue(columnIndex, item);
            };
        };
    })
    .directive("rightClickContextMenu", ["$parse", "hideContextMenus", "contextMenuHelper",
        function ($parse, hideContextMenus, contextMenuHelper) {

            var contextScope;

            //Hide context menu on click.
            function bindHideContextMenu() {
                var hideContext = function () {
                    $(".context-menu").css("display", "none");
                    if (contextScope) {
                        contextScope.contextOpened = false;
                    }
                };
                var menuHolder = $("body").addClass("context-menu-holder");
                menuHolder.bind("click", hideContext);
                menuHolder.bind("contextmenu", hideContext);
                $(".scroll-area").scroll(hideContext);
            }

            bindHideContextMenu();

            return function (scope, element, attrs) {
                var context;
                contextScope = scope;

                function showContext(event) {
                    event.stopPropagation();
                    event.preventDefault();

                    scope.contextOpened = true;

                    hideContextMenus();
                    context = $(element).find(".context-menu");
                    var menuHelper = contextMenuHelper(context, event);
                    var top = menuHelper.topPosition();
                    var left = menuHelper.leftPosition();
                    if (top < 0) {
                        top = 0;
                    }
                    context
                        .css("position", "fixed")
                        .css("top", top)
                        .css("left", left)
                        .css("display", "block");
                }

                element.on("contextmenu", showContext);
            };
        }])
    .directive("buttonClickContextMenu", ["$parse", "hideContextMenus", "contextMenuHelper",
        function ($parse, hideContextMenus, contextMenuHelper) {
            return function (scope, element, attrs) {
                var contextMenuShift = {top: 0, left: 0};
                if (attrs.contextMenuShift) {
                    contextMenuShift = $parse(attrs.contextMenuShift)();
                }

                element.bind("click", function (event) {
                    event.stopPropagation();
                    event.preventDefault();
                    hideContextMenus();
                    var buttonElement = $(element);
                    var helper = contextMenuHelper($(element).siblings(".context-menu"), event);
                    var top = helper.topPosition(
                        buttonElement.offset().top,
                        buttonElement.height()
                    ) + contextMenuShift.top;
                    if (top < 0) {
                        top = 0;
                    }
                    var left = buttonElement.offset().left + contextMenuShift.left;
                    $(element).siblings(".context-menu")
                        .css("position", "fixed")
                        .css("top", top)
                        .css("left", left)
                        .css("display", "block");
                });
            };
        }])
    .directive("dblClickContextMenu", ["$parse", "hideContextMenus", "contextMenuHelper",
        function ($parse, hideContextMenus, contextMenuHelper) {

            var contextScope;

            //Hide context menu on click.
            function bindHideContextMenu() {
                var hideContext = function () {
                    $(".context-menu").css("display", "none");
                    if (contextScope) {
                        contextScope.contextOpened = false;
                    }
                };
                var menuHolder = $("body").addClass("context-menu-holder");
                menuHolder.bind("click", hideContext);
                menuHolder.bind("contextmenu", hideContext);
                $(".scroll-area").scroll(hideContext);
            }

            bindHideContextMenu();

            return function (scope, element, attrs) {
                var context;
                contextScope = scope;

                function showContext(event) {
                    event.stopPropagation();
                    event.preventDefault();

                    scope.contextOpened = true;

                    hideContextMenus();
                    context = $(element).find(".context-menu");
                    var menuHelper = contextMenuHelper(context, event);
                    var top = menuHelper.topPosition();
                    var left = menuHelper.leftPosition();

                    context
                        .css("position", "fixed")
                        .css("top", top)
                        .css("left", left)
                        .css("display", "block");
                }

                element.on("dblclick", showContext);
            };
        }])
    .factory("animatedScroll", function () {
        return function () {
            $(".dashboard-box").animate({scrollTop: 0}, "fast");
        };
    })
    .factory("applyPaging", function ($route, contentRequestParameters, currentContentType) {
        return function ($scope, page) {
            var currentType = currentContentType();
            var currentParams = contentRequestParameters.getParameters(currentType);
            if ($scope.pageFilter == undefined) {
                $scope.pageFilter = currentParams.filterQuery != undefined ? currentParams.filterQuery : "";
            }
            if ($scope.composedFilter == undefined) {
                $scope.composedFilter = currentParams.advancedFilter != undefined ? currentParams.advancedFilter : {};
            }

            var query = {
                "page": page || 1,
                "items": $scope.maxItems || currentParams.items,
                "sortingField": currentParams.sortingField,
                "asc": currentParams.asc,
                "filterQuery": $scope.pageFilter,
                "advancedFilter": $scope.composedFilter && $scope.composedFilter.predicates &&
                    $scope.composedFilter.predicates.length > 0 ?
                    $scope.composedFilter :
                    null
            };
            if ($scope.sorting) {
                query.sortingField = $scope.sorting.field;
                query.asc = $scope.sorting.asc;
            }

            contentRequestParameters.setParameters(currentType, query);
            $route.reload();
        };
    })
    .filter("advancedFilter", function ($filter) {
        return function (list, scope) {
            if (!scope) {
                return list;
            }

            if (scope.advancedFilter && scope.advancedFilter.composedFilter.predicates &&
                scope.advancedFilter.composedFilter.predicates.length != 0) {
                scope.filter = "";
                var filtered = [];
                angular.forEach(list, function (item) {
                    var itemInFilteredArr = scope.advancedFilter.composedFilter.conjunction;
                    angular.forEach(scope.advancedFilter.composedFilter.predicates, function (predicate) {
                        if (scope.advancedFilter.composedFilter.conjunction) {
                            if (!scope.advancedFilter.composedFilter.applyToItem(item, predicate)) {
                                itemInFilteredArr = false;
                            }
                        } else { // if disjunction
                            if (scope.advancedFilter.composedFilter.applyToItem(item, predicate)) {
                                itemInFilteredArr = true;
                            }
                        }
                    });
                    if (itemInFilteredArr) {
                        filtered.push(item);
                    }
                });
                return filtered;
            }
            if (!scope.filter || scope.filter.trim().length == 0) {
                return list;
            }
            var arrSearch = scope.filter.split(" ");
            var lookup = "";
            var result = [];

            arrSearch.forEach(function (item) {
                lookup = $filter("filter")(list, item);
                console.log(lookup);
                if (lookup.length > 0) {
                    result = result.concat(lookup);
                }
            });
            return result;


        };
    })
    .directive("setValue", function () {
        return function ($scope, element, attr) {
            $scope.$eval(attr.setValue);
        };
    })
    .directive("contenteditable", function () {
        return {
            restrict: "A",
            require: "?ngModel",
            link: function (scope, element, attrs, ngModel) {
                if (!ngModel) {
                    return;
                }
                element.text(scope.$eval(attrs.ngModel) || "");

                ngModel.$render = function () {
                    element.text(ngModel.$viewValue || "");
                };

                element.bind("blur keyup change", function () {
                    setTimeout(function () {
                        scope.$apply(read);
                    }, 0);
                });
                read();

                function read() {
                    ngModel.$setViewValue(element.text() ? element.text() : ""); // to remove redundant tag <br> when content is erased
                }
            }
        };
    })
    .directive(
        "unrouteSorting",
        function ($location, contentRequestParameters, currentContentTypeParams, currentContentType) {
            return {
                restrict: "A",
                priority: -1,
                link: function (scope, element, attrs) {
                    if (!scope.sorting) {
                        return;
                    }

                    element.addClass("sortable");

                    scope.$watch("sorting.reverse", function (n, o) {
                        if (scope.sorting.field === attrs.unrouteSorting) {
                            element.find("i").remove();
                            element.prepend(n === false || n === "false" ?
                                "<i class=\"icon icon-chevron-up\">" :
                                "<i class=\"icon icon-chevron-down\">");
                        }
                    });

                    scope.$watch("sorting.field", function (n, o) {
                        if (n === attrs.unrouteSorting) {
                            element.addClass("active");
                        } else {
                            element.find("i").remove();
                            element.removeClass("active");
                        }
                    });

                    element.bind("click", function () {
                        var IsCurrentStateFalse = scope.sorting.reverse === false || scope.sorting.reverse === "false";
                        scope.sorting.field = attrs.unrouteSorting;
                        scope.sorting.reverse = IsCurrentStateFalse;
                        contentRequestParameters.setParameters(
                            currentContentType(),
                            {
                                sortingField: scope.sorting.field,
                                asc: !IsCurrentStateFalse
                            }
                        );
                        scope.$apply();
                    });
                }
            };
        }
    )
    .directive("routeSorting", function (applyPaging, currentContentTypeParams) {
        return function ($scope, element, attrs) {
            setTimeout(function () {
                var options = $scope.$eval(attrs.sortingOptions);
                if (options) {
                    if (options.disable) {
                        return;
                    }
                }
                var currentParams = currentContentTypeParams();
                $scope.sorting = {
                    asc: currentParams.asc || false,
                    field: currentParams.sortingField
                };
                var icons = {
                    "asc": "<i class=\"icon icon-chevron-up\">",
                    "desc": "<i class=\"icon icon-chevron-down\">",
                    "none": ""
                };
                var state = $scope.sorting.asc === true || $scope.sorting.asc === "true" ? "asc" : "desc";

                $(element).addClass("sortable");

                function apply() {
                    $(element)
                        .prepend(state == "asc" ?
                            "<i class=\"icon icon-chevron-up\">" :
                            "<i class=\"icon icon-chevron-down\">");
                    $(element).addClass("active");
                    $scope.$apply();
                }

                if (attrs.routeSorting == $scope.sorting.field) {
                    setTimeout(function () {
                        //$(element).find("i").remove();
                        apply();
                    }, 0);
                }


                function applyState(newState) {
                    state = newState;

                    $(element).find("i").remove();
                    $(element).prepend(icons[state]);
                    if (state === "none") {
                        $(element).removeClass("active");
                    } else {
                        $(element).addClass("active");
                    }
                    $scope.sorting.field = attrs.routeSorting;
                    $scope.sorting.asc = state === "asc";

                    if (state !== "none") {
                        applyPaging($scope);
                    }

                    $scope.$apply();
                }

                element.bind("click", function () {
                    applyState(state === "asc" ? "desc" : "asc");
                });
            });
        };
    })
    .directive(
        "routePagination",
        function ($routeParams, applyPaging, animatedScroll, currentContentTypeParams, mainViewResizeService) {
            return {
                restrict: "E",
                replace: true,
                templateUrl: "../pages/component/pagination.html",
                compile: function (elem, attr) {
                    return function ($scope, element, attrs, cntrl) {

                        function evaluate(what) {
                            return $scope.$eval(what);
                        }

                        $scope.$watch(attrs.totalItems, function (value) {
                            if (value) {
                                setup();
                                setTimeout(function () {
                                    setScrollAreaHeight();
                                }, 1);
                            }
                        });

                        function setup() {
                            var params = currentContentTypeParams();
                            $scope.sorting = {
                                asc: params.asc || false,
                                field: params.sortingField
                            };
                            $scope.totalItems = evaluate(attrs.totalItems);
                            $scope.maxItems = params.items ? params.items : evaluate(attrs.maxItems);
                            $scope.maxPages = Math.ceil($scope.totalItems / $scope.maxItems);
                            $scope.currentPage =
                                $scope.pageNumber = params.page ? Number(params.page) : evaluate(attrs.defaultPage);
                            var maxShown = evaluate(attrs.maxShown);
                            $scope.items = evaluate(attrs.items);
                            $scope.scrollTop = animatedScroll;

                            $scope.showCount = evaluate(attrs.maxItems);
                            $scope.range = function (start, end) {
                                var ret = [];
                                if (!end) {
                                    end = start;
                                    start = 0;
                                }
                                for (var i = start; i < end; i++) {
                                    ret.push(i);
                                }
                                return ret;
                            };
                            $scope.setCurrentPage = function (page) {
                                if (!isNumber(page) || page <= 0) {
                                    return;
                                }
                                if (page > $scope.maxPages) {
                                    page = $scope.maxPages;
                                }
                                $scope.pageNumber = page;
                                $scope.currentPage = page;
                            };
                            $scope.setMaxItems = function (value) {
                                if ($scope.maxItems != value) {
                                    $scope.maxItems = value;
                                    $scope.currentPage = undefined;
                                }
                            };

                            $scope.$watch("currentPage", function (neVal, oldVal) {
                                setTimeout(function () {
                                    $scope.pageNumber = neVal;
                                });
                                if (neVal != oldVal) {
                                    applyPaging($scope, neVal);
                                }
                            });
                            $scope.back = function () {
                                if ($scope.currentPage > 1) {
                                    $scope.currentPage--;
                                }
                            };
                            $scope.next = function () {
                                if ($scope.currentPage < $scope.maxPages) {
                                    $scope.currentPage++;
                                }
                            };
                            $scope.setPage = function () {
                                $scope.currentPage = this.n + 1;
                            };
                            $scope.getMaxShown = function () {
                                if ($scope.currentPage > Math.floor(maxShown / 2)) {
                                    var lastPage = $scope.currentPage + Math.floor(maxShown / 2);
                                    return lastPage > $scope.maxPages ? $scope.maxPages : lastPage;
                                }
                                return maxShown > $scope.maxPages ? $scope.maxPages : maxShown;
                            };
                            $scope.getMinShown = function () {
                                var current = $scope.currentPage - 1;
                                var floor = Math.floor(maxShown / 2);
                                var mod = current - current % maxShown;
                                if ($scope.currentPage <= $scope.maxPages - floor) {
                                    return current > floor ? current - floor : mod;
                                }
                                return $scope.maxPages - maxShown >= 0 ? $scope.maxPages - maxShown : 0;

                            };
                        }

                        mainViewResizeService.checkWidth();
                    };
                }
            };
        }
    )
    .directive("paginationFilter", function (applyPaging, $location) {
        return {
            template: "<div class=\"filter-container pull-right\">\
                        <i class=\"icon filter-big\"></i>\
                        <input id=\"filterList\" class=\"filter-table\"\
                        type=\"text\"  placeholder=\"Filter list\" \
                        ng-model=\"pageFilter\"\
                        on-enter=\"pageable.onFilterEnter()\"/>\
                       </div>",
            replace: true,
            restrict: "E",
            scope: {
                filterScope: "="
            },
            controller: function ($scope) {
                var lastUrl = $location.url();
                $scope.$watch("filterScope", function (scope, old) {
                    if (!scope) {
                        return;
                    }
                    scope.pageFilter = $scope.pageFilter;
                });

                $scope.$watch("pageFilter", function (filter, old) {
                    if (filter != old && !filter.length) {
                        if ($scope.filterScope) {
                            doFilter();
                        }
                    }
                });

                $scope.$on("$locationChangeSuccess", function (current, next, prev) {
                    if (lastUrl != "") {
                        lastUrl = "";
                    }

                    if ($location.search().q != $scope.pageFilter) {
                        $scope.pageFilter = "";
                    }
                });

                function doFilter() {
                    var query = $scope.pageFilter;
                    $scope.filterScope.pageFilter = query;
                    applyPaging($scope.filterScope);
                    if (query != undefined && query != "") {
                        $location.search("q", query).replace();
                    } else {
                        $location.search("q", null);
                    }
                }

                $scope.pageable = {
                    onFilterEnter: function () {
                        doFilter();
                    }
                };
            }
        };
    })
    .directive("switchClassAnimated", function () {
        return {
            restrict: "A",
            priority: -99,
            link: function ($scope, elem, attrs) {
                var classForAdd = "";
                var parent = $(elem).parent();
                $(elem).dblclick(function () {
                    var val = $scope.$eval(attrs.switchClassAnimated);
                    CommonLogger.log(val);
                    if (val) {
                        classForAdd = val;
                        var opened = $(".dashboard-box").find("." + val);
                        $.each(opened, function () {
                            $(this).removeClass(val, 150);
                        });
                        parent.toggleClass(val, 200);
                    } else {
                        parent.removeClass(classForAdd, 150);
                    }
                });
            }

        };
    })
    .directive("setAttribute", function () {
        return function ($scope, elem, attrs) {
            var attr = $scope.$eval(attrs.setAttribute);
            $(elem).attr(attr);
        };
    })
    .factory("lockMzCommons", function () {

        return function ($scope) {

            $scope.removeValue = function (index) {
                $scope.values.splice(index, 1);
            };

            $scope.addValue = function (value) {
                var lockMass = parseFloat(value.lockMass);
                var charge = parseInt(value.charge);
                var item = {lockMass: lockMass, charge: charge};
                var isHasBeenAdded = $scope.values.filter(function (item) {
                    return item.lockMass == lockMass && item.charge == charge;
                }).length > 0;
                if (lockMass != undefined
                    && !isNaN(lockMass)
                    && !isHasBeenAdded
                    && lockMass >= 0
                    && charge != undefined
                    && charge != 0
                    && (charge >= -3 || charge <= 3)
                ) {
                    $scope.values.push(item);
                    $scope.values.sort($scope.sortMasses);
                }
            };

            $scope.sortMasses = function (a, b) {
                return a.lockMass - b.lockMass;
            };
            return $scope;
        };
    })
    .directive("lockMasses", function (lockMzCommons) {
        return {
            restrict: "E",
            templateUrl: "../pages/component/experiment-lock-masses.html",
            scope: {
                values: "="
            },
            controller: function ($scope) {
                lockMzCommons($scope);
            },
            link: function (scope, element, attrs) {
                if (scope.values) {
                    scope.values.sort(scope.sortMasses);
                }
            }
        };
    })
    .directive("lockMassesSelector", function (lockMzCommons) {
        return {
            restrict: "E",
            templateUrl: "../pages/component/lock-masses-charge.html",
            scope: {
                values: "=" //{charge: Int, lockMass: float}
            },
            controller: function ($scope) {
                lockMzCommons($scope);
            }
        };
    })
    .directive("setFocus", function () {
        return {
            restrict: "A",
            link: function (scope, element, attr) {
                scope.$watch(attr.setFocus, function (n, o) {
                    if (n != 0 && n) {
                        element[0].focus();
                    }
                });
            }
        };
    })
    .directive("onContentLoad", function () {
        return function (scope, elem, attrs) {
            scope.$eval(attrs.onContentLoad);
        };
    })
    .directive("syncOperationControl", function () {
        return {
            restrict: "A",
            scope: {
                eventHandler: "=",
                successHandler: "=",
                errorHandler: "=",
                resourceObject: "="
            },
            link: function ($scope, $element, $attrs) {
                $element.bind($attrs.event || "click", function () {
                    $element.attr("disabled", true);
                    if ($scope.resourceObject) {
                        $scope.resourceObject["$" + $attrs.eventHandler](successHandler, failHandler);
                    } else {
                        $scope.eventHandler(successHandler, failHandler);
                    }
                });

                function successHandler(response) {
                    $element.attr("disabled", false);
                    if ($scope.successHandler) {
                        $scope.successHandler(response);
                    }
                }

                function failHandler(error) {
                    $element.attr("disabled", false);
                    if ($scope.errorHandler) {
                        $scope.errorHandler(error);
                    }
                }
            }
        };
    })
    .factory("experimentIconDetails", [function () {
        var experimentIconDetails = {};

        experimentIconDetails.getDownloadTitle = function (experiment) {
            if (experiment.downloadAvailable) {
                return "Experiment is ready to download";
            }
            if (experiment.hasUnArchiveDownloadOnlyRequest) {
                return "Experiment download request is in progress. Currently, Experiment download is unavailable";
            } else if (experiment.hasUnArchiveRequest) {
                return "Experiment unArchiving request is in progress. Currently, Experiment download is unavailable";
            }
            return "Experiment download is unavailable";


        };

        experimentIconDetails.getAnalyzesTitle = function (experiment) {
            return experiment.analyzesCount > 0 ? "Processing Runs" : "No Processing Runs";
        };
        experimentIconDetails.getIconStyleClass = function (experiment) {
            if (experiment.downloadAvailable) {
                return "quickDownload";
            }
            if (experiment.hasUnArchiveDownloadOnlyRequest) {
                return "download-in-progress";
            } else if (experiment.hasUnArchiveRequest) {
                return "download-in-time";
            }
            return "slowDownload";


        };

        return experimentIconDetails;
    }])
    .filter("fileSizeFormatter", function () {
        var units = [
            "bytes",
            "KB",
            "MB",
            "GB",
            "TB",
            "PB"
        ];

        return function (bytes) {
            if (isNaN(parseFloat(bytes)) || !isFinite(bytes)) {
                return "?";
            }

            var unit = 0;
            while (bytes >= 1024) {
                bytes /= 1024;
                unit++;
            }

            return bytes.toFixed(2) + " " + units[unit];
        };
    });

//TODO: [stanislav.kurilin] I'm not sure that it belongs here. I believe if there will be more such stuff we should extract it.
//Creates directive for details action.
function detailsLink(arg) {
    var title = arg.title || "Show Details";
    var optionalParam = arg.urlParam || "";
    var icon = arg.icon || "icon edit";
    var dataTarget = arg.dataTarget || "#details"; //why do we need it?
    return function () {
        return {
            scope: {
                eid: "@", show: "=", path: "@", returnurl: "@", text: "=", urlParam: "=" //optional default is location path
            },
            controller: function ($rootScope, $scope, $location) {
                var urlParam = optionalParam || $scope.urlParam || "";

                $scope.showDetails = function (id) {
                    $rootScope.dialogReturnUrl = $location.url();
                    $location.url($location.path() + "/" + id + urlParam);
                };
            },
            restrict: "E",
            replace: true,
            template: "\
            <a\
                ng-click=\"showDetails(eid)\"\
                class=\"\"\
                data-toggle=\"modal\" data-target=\"" + dataTarget + "\"\
                title=\"" + title + "\"><i class=\"" + icon + "\"></i><span class=\"details-link-name\" set-text=\"text || 'Edit'\"></span>\
            </a>"
        };
    };
}


function detailsDirective(arg) {
    var title = arg.title || "Show Details";
    var dataTarget = arg.dataTarget || "#details"; //why do we need it?
    return function () {
        return {
            scope: {
                eid: "@", show: "=", path: "@", returnurl: "@", canEdit: "@" //optional default is location path
            },
            controller: function ($rootScope, $scope, $location) {
                $scope.showDetails = function (id) {
                    $rootScope.dialogReturnUrl = $location.url();
                    var url = $location.path().substring(0, $location.path().lastIndexOf("/"));
                    $location.url(url);
                };
            },
            restrict: "E",
            template: "\
            <button\
                ng-show=\"show\" \
                ng-click=\"showDetails(eid)\"\
                class=\"table-button btn btn-success\"\
                data-toggle=\"modal\" data-target=\"" + dataTarget + "\"\
                title=\"" + title + "\">\
            <i class=\"icon details\" ng-hide=\"canEdit\"></i>\
            <i class=\"icon-edit\" ng-show=\"canEdit\"></i>\
            </button>"
        };
    };
}

//helper for creating directives for linked names. arg.sub should be specified.
//todo generalize to entities without linked names
function linkedName(arg) {
    return function ($location) {
        return {
            restrict: "E",
            controller: function ($rootScope, $scope, $attrs) {
                var sub = angular.isDefined(arg.sub) ? arg.sub : $scope.$eval($attrs.sub);
                $scope.linkLabel = $scope.$eval($attrs.name);
                $scope.linkUrl = "#" + $location.path() + "/" + $scope.$eval($attrs.eid) + "/" + sub;
                $scope.saveLocation = function () {
                    $rootScope.dialogReturnUrl = $location.url();
                };
            },
            template: "<a ng-click=\"saveLocation()\" href=\"{{linkUrl}}\" class=\"table-link\" ng-bind=\"linkLabel\"></a>"
        };
    };
}

//Helper for creating Expand Menu factories
function initExpandMenu(openMenuFn) {
    return function ($scope) {
        $scope.toggleExpandMenu = function (item) {
            $scope.opened = $scope.opened == item ? null : item;
            if ($scope.opened == item) {
                openMenuFn(item, $scope);
            }
        };
        $scope.setOpened = function (item) {
            $scope.opened = $scope.opened == item ? null : item;
        };
        $scope.onOpenExpandMenu = function (event, item, open) {
            if (open) {
                openMenuFn(item, $scope);
            }
            //
        };
    };
}


function userOrGroupSelection(arg) {
    var isEmailNotificationsAvailable = arg.isEmailNotificationsAvailable || false;
    var groupSelectionAvailable = arg.groupSelectionAvailable || false;

    var filterUserSelection = function ($scope) {
        $scope.users = jQuery.grep($scope.users, function (user) {
            return shouldNotBeExcluded($scope, user.email);
        });
    };

    var shouldNotBeExcluded = function ($scope, email) {
        var excludesCount = 0;
        angular.forEach($scope.excludeEmails, function (excludeEmail) {
            if (email == excludeEmail) {
                excludesCount++;
            }
        });
        return excludesCount == 0;
    };

    var selectionScope = {
        users: "=", groups: "=", selectedUsers: "=", selectedGroups: "=", emailNotification: "=",
        excludeEmails: "=", selectionDisabled: "=", inviteHandler: "=", userDescription: "=", onlyRegisteredUsers: "="
    };
    if (arg.getInvitedUsers) {
        selectionScope.invitedUsers = "=";
    }

    return function () {
        return createSelectionDialog({
            scope: selectionScope,
            args: arg,
            defineScopeFunctions: function ($scope) {
                $scope.isUser = function (item) {
                    //TODO: Rename method (eg. 'isAvailableUser()')
                    var filteredUsers = $.grep($scope.users, function (user) {
                        return item.email && user.email == item.email;
                    });
                    var selectedUsers = $.grep($scope.selectedUsers, function (selectedUser) {
                        return item.email && selectedUser.email == item.email;
                    });
                    return filteredUsers.length > 0 || selectedUsers.length > 0;
                };
                $scope.isGroup = function (item) {
                    return $scope.groups && $.grep($scope.groups, function (group) {
                        return item.name && group.name == item.name;
                    }).length > 0;
                };
                $scope.isInvited = function (item) {
                    return !$scope.isUser(item) && !$scope.isGroup(item);
                };
                $scope.userEmail = function (item) {
                    return item.email;
                };
                $scope.userName = function (item) {
                    return item.name;
                };
                $scope.groupName = function (item) {
                    return item.name;
                };
                $scope.isEmailNotificationsAvailable = function () {
                    return isEmailNotificationsAvailable;
                };
                $scope.isInputDisable = function () {
                    return isInputDisable;
                };
                $scope.isUserCanBeRemoved = function (item) {
                    if (!$scope.excludeEmails) {
                        return true;
                    }
                    return shouldNotBeExcluded($scope, $scope.userEmail(item));
                };
                $scope.getUserDescription = function (item) {
                    return $scope.userDescription && $scope.userDescription[$scope.userEmail(item)] || "";
                };
                $scope.invitedUsers = [];
                $scope.inviteUser = function (item) {
                    item.invited = true;
                    $scope.inviteHandler(item, function (invited) {
                        $scope.removeItem(item);
                        invited.name = invited.firstName + " " + invited.lastName;
                        $scope.users.push(invited);
                        $scope.selectedUsers.push(invited);
                    });
                };
                $scope.showAllowWrite = arg.showAllowWrite || false;
            },
            selectedFn: function ($scope) {
                return function () {
                    return groupSelectionAvailable ?
                        $scope.selectedUsers.concat($scope.selectedGroups) :
                        $scope.selectedUsers;
                };
            },
            selectedToCreate: function ($scope) {
                return function () {
                    return $scope.invitedUsers;
                };
            },
            getAllItems: function ($scope) {
                if ($scope.excludeEmails) {
                    filterUserSelection($scope);
                }
                return $scope.users.concat(groupSelectionAvailable ? $scope.groups : []);

            },
            showInAutoCompleteFn: function ($scope) {
                return function (item) {
                    return $scope.isUser(item) ?
                        $scope.userName(item) + "<" + $scope.userEmail(item) + ">" :
                        $scope.groupName(item);
                };
            },
            identifyFn: function ($scope) {
                return function (item) {
                    var res = $scope.isUser(item) ? $scope.userEmail(item) : $scope.groupName(item);
                    if (!res && item.email) {
                        return item.email;
                    }
                    return res;

                };
            },
            addSelectedItem: function ($scope, item) {
                if ($scope.isUser(item)) {
                    $scope.selectedUsers.push(item);
                } else if ($scope.isInvited(item)) {
                    item.invited = false;
                    $scope.invitedUsers.push(item);
                } else if ($scope.isGroup(item) && groupSelectionAvailable) {
                    $scope.selectedGroups.push(item);
                } else {
                    throw "could not dispatch on " + item;
                }
            },
            removeSelectedItem: function ($scope, item, id) {
                if ($scope.isUser(item)) {
                    $scope.selectedUsers = jQuery.grep($scope.selectedUsers, function (elem, index) {
                        return $scope.identify(elem) != id;
                    });
                } else if ($scope.isInvited(item)) {
                    $scope.invitedUsers = jQuery.grep($scope.invitedUsers, function (elem, index) {
                        return $scope.identify(elem) != id;
                    });
                } else if ($scope.isGroup(item) && groupSelectionAvailable) {
                    $scope.selectedGroups = jQuery.grep($scope.selectedGroups, function (elem, index) {
                        return $scope.identify(elem) != id;
                    });
                } else {
                    throw "could not dispatch on " + item;
                }
            },
            templateUrl: "../pages/component/user-group-selection.html"
        });
    };
}


var Selections = {};
Selections.selectAll = function (items) {
    var nonSelected = $.grep(items, function (item) {
        return !item.selected;
    });
    if (nonSelected.length > 0) {
        $(items).each(function () {
            this.selected = true;
        });
    } else {
        $(items).each(function () {
            this.selected = false;
        });
    }
};

function placeCaretAtEnd(el) {
    el.focus();
    if (typeof window.getSelection != "undefined"
        && typeof document.createRange != "undefined") {
        var range = document.createRange();
        range.selectNodeContents(el);
        range.collapse(false);
        var sel = window.getSelection();
        sel.removeAllRanges();
        sel.addRange(range);
    } else if (typeof document.body.createTextRange != "undefined") {
        var textRange = document.body.createTextRange();
        textRange.moveToElementText(el);
        textRange.collapse(false);
        textRange.select();
    }
}

function isNumber(stringValue) {
    return !isNaN(parseFloat(stringValue)) && isFinite(stringValue);
}

function showAlerts(elem, fadeInTime, $scope) {
    elem.fadeIn(fadeInTime).delay(1000).fadeOut(2000, function () {
        $scope.alerts = [];
    });
}

function setupAttachments(dialogSelector,
                          $scope,
                          Attachment,
                          attachmentTypeFromName,
                          allowedExtensionsFn,
                          maxAttachmentSize,
                          fileChooserId,
                          isSingleFileUpload,
                          displayDragAndDropAreaOnlyIfAreaPresented,
                          allowAllFileTypesOption) {
    $scope.alerts = [];
    var allowAllFileTypes = allowAllFileTypesOption === undefined ? true : allowAllFileTypesOption;
    var dragNDropHelper = new DragNDropHelper({
            containerSelector: dialogSelector,
            dropOverlaySelector: ".attachment-drop-area",
            displayDragAndDropAreaOnlyIfAreaPresented: displayDragAndDropAreaOnlyIfAreaPresented,
            previewAreaSelector: ".attachments-holder",
            leaveDropSelector: dialogSelector,
            maxAttachmentSize: maxAttachmentSize,
            fileChooserId: fileChooserId,
            isSingleFileUpload: isSingleFileUpload,
            getAllFilesFunction: function () {
                return $scope.uploadingAttachments;
            },
            setAllFilesFunction: function (files) {
                $scope.uploadingAttachments = files;
            },
            wrapFileFunction: function (name, date, size, type, originalFile, ext) {
                return new Attachment(name, date, size, attachmentTypeFromName(name), originalFile, null, ext);
            },
            allowedExtensionsFn: allowedExtensionsFn,
            onNotAllowedExtensionCallback: function (changedFiles) {
                $scope.alerts.push(DragNDropMessages.UNSUPPORTED_FILES_FILTERED);
                CommonLogger.log("These files have been dropped: " + JSON.stringify(changedFiles));
            },
            onNotAllowedSizeCallback: function (changedFiles) {
                $scope.alerts.push("The attachment size exceeds " + maxAttachmentSize / 1048576 + " MB");
                CommonLogger.log("These files have been dropped: " + JSON.stringify(changedFiles));
            },
            onEmptyFolderCallback: function () {
                $scope.alerts.push(DragNDropMessages.EMPTY_FOLDERS_FILTERED);
            },
            onFilesDroppedCallback: function (files) {
                var showDuplicated = false;
                $.each(files, function (i, file) {
                    if (DroppedFile.hasDuplicate(file.name, $scope.existingAttachments)) {
                        showDuplicated = true;
                        $scope.uploadingAttachments = $.grep($scope.uploadingAttachments, function (item) {
                            return item.name != file.name;
                        });
                    }
                });
                if (showDuplicated) {
                    $scope.alerts.push(DragNDropMessages.DUPLICATES_REMOVED);
                }

                setTimeout(function () {
                    showAlerts($(".unsupported-files-alert"), 500, $scope);
                });
                $scope.$apply();
            },
            allowAllFileTypes: allowAllFileTypes
        }
    );
    dragNDropHelper.init(true);
    return dragNDropHelper;
}


