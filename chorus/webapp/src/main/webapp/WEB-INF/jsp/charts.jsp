<%@ page language="java" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="datePattern"><fmt:message key="date.format"/></c:set>
<html>

<head>
    <title>Experiment Data Browser</title>
    <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>

    <script type="text/javascript" src="<c:url value="/lib/bootstrap/js/bootstrap.min.js"/>"></script>
    <link href="<c:url value="/lib/bootstrap/css/bootstrap.min.css"/>" rel="stylesheet" type="text/css" />
    <link href="<c:url value="/css/dialogs.css"/>" rel="stylesheet" type="text/css" />
    <!-- FavIcon -->
    <link rel="icon" href="<c:url value="/img/favicon.ico"/>" type="image/x-icon"/>
    <link rel="stylesheet" href="//ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/themes/smoothness/jquery-ui.css"/>
    <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jqueryui/1.8.18/jquery-ui.min.js"></script>
    <script type="text/javascript" src="<c:url value='/script/charts/jquery-plugins/layout/jquery.layout-latest.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/script/charts/jquery-plugins/drag/jquery.event.drag-2.0.min.js'/>"></script>
    <script type='text/javascript' src='//www.google.com/jsapi'></script>
    <link href="<c:url value="/script/charts/jquery-plugins/jquery-loadmask-0.4/jquery.loadmask.css"/>" rel="stylesheet" type="text/css" />
    <script type="text/javascript" src="<c:url value="/script/charts/jquery-plugins/jquery-loadmask-0.4/jquery.loadmask.min.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/script/charts/jquery-plugins/resize/jquery.ba-resize.min.js"/>"></script>

    <link href="<c:url value="/script/charts/jquery-plugins/svg/jquery.svg.css"/>" rel="stylesheet" type="text/css" />
    <script type="text/javascript" src="<c:url value="/script/charts/jquery-plugins/svg/jquery.svg.js"/>"></script>

    <%-- https://github.com/allmarkedup/jQuery-URL-Parser --%>
    <script type="text/javascript" src="<c:url value="/script/charts/jquery-plugins/purl.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/script/charts/jquery-plugins/jquery.ba-hashchange.min.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/script/charts/lodash.min.js"/>"></script>

    <script type="text/javascript" src="<c:url value="/script/charts/jquery-plugins/spectrum/spectrum.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/script/charts/jquery-plugins/jquery.dotdotdot-1.5.2.js"/>"></script>
    <link href="<c:url value="/script/charts/jquery-plugins/spectrum/spectrum.css"/>" rel="stylesheet" type="text/css" />

    <script type="text/javascript" src="<c:url value="/script/charts/js/chartEvents.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/script/charts/js/util.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/script/charts/js/urlStorage.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/script/charts/js/chartView.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/script/charts/js/chartController.js"/>"></script>

    <script type="text/javascript">
        $(function() {
            $(".jquery-button").button();
        });
    </script>

    <script type="text/javascript">

        function ChartManager(eventBus, storage){

            var manager = this;
            this._storage = storage;

            $(eventBus).on('ajaxError', function(event, errorResponse){
                if (errorResponse.status === 403){
                    $(eventBus).trigger(ChartEvent.MODEL_403_ERROR_RECEIVED, errorResponse);
                }
            });

            $(eventBus).on(ChartEvent.MODEL_REQUEST_FOR_FILES_AND_FUNCTIONS, function(event, data){
                var filesAndExperimentId = manager._storage.readFilesAndExperiment();
                getFiles(filesAndExperimentId, function(response){
                    var fileList = readFileList(response);
                    var chartStates = manager._storage.loadStatesFromHash(fileList);
                    chartStates.chartSize.width = data.width;
                    chartStates.chartSize.height = data.height;
                    var predefinedColors = Util.getPredefinedColors();
                    for (var i=0; i < chartStates.selectedFiles.length; i++){
                        chartStates.selectedColors[i] = i < predefinedColors.length ? predefinedColors[i] : Util.generateRandomColor();
                    }
                    manager._storage.updateHash(chartStates);
                    getMSFunctionsForSelectedFiles(chartStates.selectedFiles, function(response){
                        var msFunctions = readMSFunctionList(response);
                        if (msFunctions.length > 0 && chartStates.selectedFunction === null) {
                            chartStates.selectedFunction = msFunctions[0];
                        }
                        manager._storage.updateHash(chartStates);
                        $(eventBus).trigger(ChartEvent.MODEL_FILES_AND_FUNCTIONS_RECEIVED, {fileList: fileList, msFunctions: msFunctions, chartStates: chartStates});
                    });
                });
            });

            $(eventBus).on(ChartEvent.MODEL_GET_MS_FUNCTIONS_FOR_SELECTED_FILES, function (event, data) {
                var chartStates = manager._storage.loadStatesFromHash();
                chartStates.selectedFiles = data.selectedFiles;
                chartStates.selectedColors = data.selectedColors;
                getMSFunctionsForSelectedFiles(data.selectedFiles, function (response) {
                    var msFunctions = readMSFunctionList(response);
                    chartStates.selectedFunction = msFunctions[0];
                    manager._storage.updateHash(chartStates);
                    $(eventBus).trigger(ChartEvent.MODEL_MS_FUNCTIONS_FOR_SELECTED_FILES_RECEIVED, {
                        msFunctions: msFunctions,
                        chartStates: chartStates
                    });
                });
            });

            $(eventBus).on(ChartEvent.MODEL_RENDER_CHART, function(event, data){
                var chartStates = manager._storage.loadStatesFromHash();
                var chartState = chartStates[data.chartId];
                renderImage({selectedFiles: chartStates.selectedFiles,
                    selectedColors: chartStates.selectedColors,
                    width: chartStates.chartSize.width,
                    height: chartStates.chartSize.height,
                    chartType: chartState.t,
                    startRt: chartState.sRt,
                    endRt: chartState.eRt,
                    startMz: chartState.sMz,
                    endMz: chartState.eMz,
                    selectedFunction: chartStates.selectedFunction}, function(result){
                    $(eventBus).trigger(ChartEvent.MODEL_CHART_RENDERED, {chartId: data.chartId, chartInfo: result, chartStates: chartStates});
                });
            });

            $(eventBus).on(ChartEvent.MODEL_STORE_SELECTED_MS_FUNCTION, function(event, data){
                var chartStates = manager._storage.loadStatesFromHash();
                chartStates.selectedFunction = data.selectedFunction;
                manager._storage.updateHash(chartStates);
                $(eventBus).trigger(ChartEvent.MODEL_MS_FUNCTION_STORED, {forceRefresh: false});
            });

            $(eventBus).on(ChartEvent.MODEL_STORE_SELECTED_CHART_TYPE, function(event, data){
                var chartStates = manager._storage.loadStatesFromHash();
                var chartState = chartStates[data.chartId];
                if (data.chartType === chartState.t){
                    console.log("Chart with requested type is already here. Skipping.");
                    return;
                }
                chartState.t = data.chartType;
                manager._storage.updateHash(chartStates);
                $(eventBus).trigger(ChartEvent.MODEL_CHART_TYPE_STORED, {chartId: data.chartId});
            });

            $(eventBus).on(ChartEvent.MODEL_STORE_ZOOM_PARAMETERS, function(event, data){
                zoomInto(data.chartId, data.start, data.end, getZoomAxis(data.chartType));
                $(eventBus).trigger(ChartEvent.MODEL_ZOOM_PARAMETERS_STORED, {chartId: data.chartId});
            });

            $(eventBus).on(ChartEvent.MODEL_STORE_RESET_ALL_PARAMETERS, function(event, data){
                var chartStates = manager._storage.loadStatesFromHash();
                var chartState = chartStates[data.chartId];
                chartState.sRt = -1;
                chartState.eRt = -1;
                chartState.sMz = -1;
                chartState.eMz = -1;
                manager._storage.updateHash(chartStates);
                $(eventBus).trigger(ChartEvent.MODEL_RESET_ALL_PARAMETERS_STORED, {chartId: data.chartId});
            });

            $(eventBus).on(ChartEvent.MODEL_STORE_CHART_SIZE_PARAMETERS, function(event, data){
                var chartStates = manager._storage.loadStatesFromHash();
                var width = chartStates.chartSize.width;
                var height = chartStates.chartSize.height;
                if ( width != data.width || height != data.height) {
                    chartStates.chartSize.width = data.width;
                    chartStates.chartSize.height = data.height;
                    manager._storage.updateHash(chartStates);
                    $(eventBus).trigger(ChartEvent.MODEL_NEW_CHART_SIZE_STORED, {forceRefresh: true});
                }
            });

            $(eventBus).on(ChartEvent.MODEL_STORE_SELECTED_COLORS, function(event, data){
                var chartStates = manager._storage.loadStatesFromHash();
                chartStates.selectedColors = data.selectedColors;
                manager._storage.updateHash(chartStates);
                $(eventBus).trigger(ChartEvent.MODEL_SELECTED_COLORS_STORED, {forceRefresh: false});
            });

            $(eventBus).on(ChartEvent.MODEL_EXPORT_CHART, function (event, data) {
                var chartStates = manager._storage.loadStatesFromHash();
                var chartState = chartStates[data.chartId];
                exportChart({
                    selectedFiles: chartStates.selectedFiles,
                    selectedColors: chartStates.selectedColors,
                    width: chartStates.chartSize.width,
                    height: chartStates.chartSize.height,
                    chartType: chartState.t,
                    startRt: chartState.sRt,
                    endRt: chartState.eRt,
                    startMz: chartState.sMz,
                    endMz: chartState.eMz,
                    selectedFunction: chartStates.selectedFunction
                }, function (data) {
                    var blob = new Blob([new XMLSerializer().serializeToString(data)], {type: "xml"});

                    var currentDate = new Date();
                    var dateTime = +(currentDate.getMonth() + 1) + "/"
                            + currentDate.getDate() + "/"
                            + currentDate.getFullYear() + " "
                            + currentDate.getHours() + ":"
                            + currentDate.getMinutes() + ":"
                            + currentDate.getSeconds();

                    var filename = "export image " + dateTime + ".svg";
                    var URL = window.URL || window.webkitURL;
                    var downloadUrl = URL.createObjectURL(blob);

                    var a = document.createElement("a");
                    a.href = downloadUrl;
                    a.download = filename;
                    document.body.appendChild(a);
                    a.click();

                    setTimeout(function () {
                        URL.revokeObjectURL(downloadUrl);
                    }, 100);

                    console.log("Export image has received");
                });
            });

            var getFiles = function (data, successHandler) {
                $.ajax({
                    url: "<c:url value="/charts/filelist"/>",
                    dataType: 'json',
                    async: true,
                    data: {files: data.fileIds, experiment: data.experimentId},
                    traditional: true,
                    success: successHandler
                });
            };

            var getMSFunctionsForSelectedFiles = function(fileIds, successHandler){
                $.ajax({url: "<c:url value="/charts/functionlist"/>",
                    dataType: 'json',
                    async: true,
                    data: {files: fileIds},
                    traditional: true,
                    success: successHandler
                });
            };

            var renderImage = function(data, successHandler){
                $.ajax({url: "<c:url value="/charts/render"/>",
                            headers: {
                                'Accept': 'application/json',
                                'Content-Type': 'application/json'
                            },
                            dataType: 'json',
                            type: 'POST',
                            data: JSON.stringify({
                                files: data.selectedFiles,
                                colors: data.selectedColors,
                                width: data.width,
                                height: data.height,
                                chartType: data.chartType,
                                startRt: data.startRt,
                                endRt: data.endRt,
                                startMz: data.startMz,
                                endMz: data.endMz,
                                selectedFunction: data.selectedFunction
                            }),
                            traditional: true,
                            success: successHandler
                        }
                )};

            var exportChart = function(data, successHandler){
                $.ajax({
                            url: "<c:url value="/charts/export"/>",
                            headers: {
                                'Accept': 'image/svg+xml',
                                'Content-Type': 'application/json'
                            },
                            dataType: 'xml',
                            type: 'POST',
                            data: JSON.stringify({
                                files: data.selectedFiles,
                                colors: data.selectedColors,
                                width: data.width,
                                height: data.height,
                                chartType: data.chartType,
                                startRt: data.startRt,
                                endRt: data.endRt,
                                startMz: data.startMz,
                                endMz: data.endMz,
                                selectedFunction: data.selectedFunction
                            }),
                            traditional: true,
                            success: successHandler
                        }
                )};

            function readFileList(response){
                var fileList = [];
                for (var i = 0; i < response.length; i++) {
                    fileList.push({id: response[i].id.toString(), name: response[i].name, conditions: response[i].conditions});
                }
                return fileList;
            }

            function readMSFunctionList(response){
                var msFunctions = [];
                for (var i = 0; i < response.length; i++) {
                    msFunctions.push(response[i].name);
                }
                return msFunctions;
            }

            function getZoomAxis(chartType) {
                switch(chartType) {
                    case 'TIC_CHROMATOGRAM' : return 'RT';
                    case 'BPI_CHROMATOGRAM' : return 'RT';
                    case 'TIC_SPECTRUM' : return 'MZ';
                    case 'BPI_SPECTRUM' : return 'MZ';
                    default : throw "Unknown chart type " + chartType;
                }
            }

            function zoomInto(chartId, start, end, zoomAxis) {
                var chartStates = manager._storage.loadStatesFromHash();
                var chartState = chartStates[chartId];
                switch (zoomAxis) {
                    case 'RT' :
                        chartState.sRt = start;
                        chartState.eRt = end;
                        manager._storage.updateHash(chartStates);
                        break;
                    case 'MZ' :
                        chartState.sMz = start;
                        chartState.eMz = end;
                        manager._storage.updateHash(chartStates);
                        break;
                    default : console.log("Unknown axis " + zoomAxis)
                }
            }
        }

    </script>

    <style type="text/css">
        .toolbar {
            font-size: 10px;
            display: none;
            position: absolute;
            padding: 10px 10px;
            text-align: right;
        }
        .toolbar button {
            margin: 0 4px 0 0;
        }
        .ui-button{
            vertical-align: top;
            margin-bottom: 7px;
        }
        .buttonset,.toolbar-first-row {
            white-space:nowrap;
        }
        .canvas{
            shape-rendering:crispEdges
        position: absolute;
            left: 0;
            top: 0
        }
        .files-box h2, .functions-box h2 {
            font: bold 14px/18px Verdana, Tahoma, sans-serif;
            color: #222;
            margin: 0 0 10px;
        }
        .files-box #files {
            padding: 0;
            margin: 0;
            list-style: none;
            font: 13px/15px Verdana, Tahoma, sans-serif;
            color: #222;
        }
        .files-box #files li {
            width: 100%;
            margin-bottom: 5px;
            vertical-align: top;
        }
        .files-box #files li input {
            display: inline-block;
            width: 15px;
            height: 15px;
            padding: 0;
            margin: 0 5px 0 0;
        }

        .sp-dd {
            height: 8px;
            line-height: 8px;
        }
        .sp-preview
        {
            width:15px;
            height: 10px;
        }
        #select-function{
            width:100%;
        }
        #warning-message{
            display:inline-block;
            padding-top: 5px;
            color: red;
        }
        .functions-box{
            height: 66px;
        }
        .chart{
            width: 100%;
            height: 50%;
        }
        .color-by-condition, .refresh-now {
            margin-top:10px;
        }
    </style>

</head>

<body>
<div class="ui-layout-center">
    <div id="main-area">
        <div id="charts">
            <div id="top-chart" class="chart">
                <img class="chart-image" src="<c:url value="/img/charts/empty.gif"/>" alt="Top Chart"/>
                <div class="canvas"></div>
                <div class="toolbar">
                    <div class="toolbar-first-row">
                            <span class="buttonset zoom-mode">
                                <input id="top-zoom" type="radio" name="top-mode" checked="checked" value="zoom"/> <label for="top-zoom">Zoom</label>
                                <input id="top-range" type="radio" name="top-mode" value="range"/> <label for="top-range">Range</label>
                            </span>
                        <button class="reset-all">Reset All</button>
                        <button class="reset-zoom">Reset Zoom</button>
                        <button class="left">Step Left</button>
                        <button class="right">Step Right</button>
                    </div>
                    <button class="export">Export</button>
                        <span class="buttonset chart-type">
                            <input id="tic-chromatogram" type="radio" name="top-type" value="TIC_CHROMATOGRAM"/> <label for="tic-chromatogram">TIC</label>
                            <input id="bpi-chromatogram" type="radio" name="top-type" checked="checked" value="BPI_CHROMATOGRAM"/> <label for="bpi-chromatogram">BPI</label>
                        </span>
                    <button class="plus">Zoom in</button><br/>
                    <button class="minus">Zoom out</button>
                </div>
            </div>

            <div id="bottom-chart" class="chart">
                <img class="chart-image" src="<c:url value="/img/charts/empty.gif"/>" alt="Bottom Chart"/>
                <div class="canvas"></div>
                <div class="toolbar">
                    <div class="toolbar-first-row">
                            <span class="buttonset zoom-mode">
                                <input id="bottom-zoom" type="radio" name="bottom-mode" checked="checked" value="zoom"/> <label for="bottom-zoom">Zoom</label>
                                <input id="bottom-range" type="radio" name="bottom-mode" value="range"/> <label for="bottom-range">Range</label>
                            </span>
                        <button class="reset-all">Reset All</button>
                        <button class="reset-zoom">Reset Zoom</button>
                        <button class="left">Step Left</button>
                        <button class="right">Step Right</button>
                    </div>
                    <button class="export">Export</button>
                        <span class="buttonset chart-type">
                            <input id="tic-spectrum" type="radio" name="bottom-type" checked="checked" value="TIC_SPECTRUM"/> <label for="tic-spectrum">TIC</label>
                            <input id="bpi-spectrum" type="radio" name="bottom-type" value="BPI_SPECTRUM"/> <label for="bpi-spectrum">BPI</label>
                        </span>
                    <button class="plus">Zoom in</button><br/>
                    <button class="minus">Zoom out</button>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="ui-layout-west">
    <div class="functions-box">
        <h2>Selected Function</h2>

        <select id="select-function"></select>
        <span id="warning-message" style="display:none">The selected set of files does not have common functions.</span>
    </div>
    <div class="files-box">
        <h2 style="float:left">Selected Files</h2>

        <div style="float: right">
            <table><tr>
                <td> <input id="select-all" type="checkbox"/></td>
                <td> <label for="select-all" style="margin:0;">Select All</label></td>
            </tr></table>
        </div>
        <table id="files" style="width:100%" border="0" cellspacing="0" cellpadding="2">
            <colgroup>
                <col/>
                <col style="width: 10px"/>
                <col/>
                <col/>
            </colgroup>
        </table>
        <button class="color-by-condition">Color by condition</button>

        <button class="refresh-now"><span class="countdown">↻</span> Refresh now</button>
    </div>
</div>

<div id="accessDeniedMessage" class="modal hide" tabindex="-1" role="dialog">
    <div class="modal-holder">
        <div class="modal-frame">
            <div class="modal-header">
                <a data-dismiss="modal" class="close" id="accessDeniedMessageClose" href="">&nbsp;</a>

                <h3>Access Denied</h3>
            </div>
            <div class="modal-body">
                <div class="dialog-container">
                    <p>You do not have an access to the selected files.</p>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn btn-primary main-action" id="goToDashboard">
                    Go to Dashboard
                </button>
            </div>
        </div>
    </div>
</div>

<script>
    var eventBus = {};
    new ChartView(eventBus);
    new ChartController(eventBus);
    new ChartManager(eventBus, new URLStorage());
</script>
</body>
</html>
