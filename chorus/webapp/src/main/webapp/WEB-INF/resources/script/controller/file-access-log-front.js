angular.module("file-access-log", ["ngResource", "front-end"])
    .controller("file-access-log-controller", function ($scope, fileAccessLog, contentRequestParameters) {
        if ($scope.pathError) return;
        $scope.fileAccessLog = [];

        $scope.page.title = "File Access Log";
        $scope.total = 0;
        var isTableEmpty = true;

        var init = function () {
            var requestParameters = contentRequestParameters.getParameters("file-access-log");
            fileAccessLog.get(requestParameters, function (data) {
                isTableEmpty = !(data.items && data.items.length > 0);
                $scope.fileAccessLog = data.items;
                $scope.total = data.itemsCount;
            });
        }

        $scope.isTableEmpty = function () {
            return isTableEmpty;
        };

        $scope.getEmptyTableMessage = function () {
            return "Table is empty.";
        };

        init();
    })
    .filter("operationTypeFormatter", function(){
        var types = {
            "FILE_UPLOAD_STARTED"       : "Upload Started",
            "FILE_UPLOAD_CONFIRMED"     : "Upload Confirmed",
            "FILE_DELETED"              : "Deleted",
            "FILE_DELETED_PERMANENTLY"  : "Deleted Permanently",
            "FILE_ARCHIVE_STARTED"      : "Archive Started",
            "FILE_ARCHIVE_CONFIRMED"    : "Archive Confirmed",
            "FILE_DOWNLOAD_STARTED"     : "Download Started"
        };

        return function(operation){
            if(!types[operation]){
                return "Unknown operation";
            }
            return types[operation];
        }
    });