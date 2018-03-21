/*** Utility operations for the so called tabular data. E.g. multiple Excel-like cells organized into rows and columns. ***/


//todo[tymchenko]: Discuss if we need to have object-based params (e.g. {getWidth: function() {...}, ...})
//and callback to process cell after its contents is changed (to highlight?)
var TableModel = function (getWidth, getHeight, getItemValueAtXY, setItemValueAtXY) {
    this.getWidth = getWidth;
    this.getHeight = getHeight;
    this.getItemValueAtXY = getItemValueAtXY;
    this.setItemValueAtXY = setItemValueAtXY;

};

TableModel.prototype.startWatchingModifications = function ($scope) {
    var tableModel = this;
    $scope.$watch(
        function() {
            var width = tableModel.getWidth();
            var height = tableModel.getHeight();
            var totalContent = "";
            for(var x = 0; x < width; x++) {
                for(var y = 0; y < height; y++) {
                    totalContent = totalContent + tableModel.getItemValueAtXY(x, y);
                }
            }
            return totalContent;
        },
        function() {
            CommonLogger.log("Table values have been modified.");

            //process changes out of current flow to be able to call $apply on our changes
            setTimeout(function() {tableModel._handlePastedContent($scope);}, 0);
        }
    )
};

TableModel.prototype._handlePastedContent = function($scope) {
    var tableModel = this;
    var width = tableModel.getWidth();
    var height = tableModel.getHeight();

    //explicitly call $apply to apply the changes to the potentially focused content of the cell (probably, it's a workaround)
    $scope.$apply(function() {
        var pastedContent = [];

        var originalPastedWidth = 0;
        var originalPastedHeight = 0;

        var pastedAtX = 0;
        var pastedAtY = 0;

        var lastColumn = width-1;

        for (var y = 0; y < height; y++) {
            for (var x = 0; x < width; x++) {
                if (pastedContent.length == 0) {
                    var cellValue = tableModel.getItemValueAtXY(x, y);
                    if (cellValue) {
                        //detect if this is a table-based content

                         function getRows(cellValue) {
                            var index = 0;
                            var escapes = ["\"\n", "\"\t"];
                            var newCellValue = "";
                            var newEscapeSymbol = " ";

                            while(index < cellValue.length){
                                if(cellValue[index] == "\"") {
                                    var multilineRow = "";
                                    while(index < cellValue.length-1 && ($.inArray(multilineRow[multilineRow.length-1] + cellValue[index + 1], escapes) == -1)){
                                        multilineRow += cellValue[index];
                                        index++;
                                        if(cellValue.length < (index + 1)){
                                            break;
                                        }
                                    }
                                    multilineRow += cellValue[index];
                                    newCellValue += multilineRow.slice(1, multilineRow.length-1).replace(/\r\n|\r|\n/g, newEscapeSymbol).replace(/""/g, "\'");
                                    index++;
                                } else {
                                    newCellValue += cellValue[index];
                                    index++;
                                }
                            }
                           return newCellValue.split(/\r\n|\r|\n/g, height);
                        }



                        var rows = getRows(cellValue);
                        $.each(rows, function (index, item) {
                            //row columns are split with tabs if pasted from Excel
                            var columns = item.split("\t", width);
                            pastedContent.push(columns);
                        });
                        originalPastedWidth = pastedContent[0].length;
                        originalPastedHeight = pastedContent.length;

                        pastedAtX = x;
                        pastedAtY = y;

                        CommonLogger.log("The pasted content is: " + JSON.stringify(pastedContent));
                    }
                }

                if (pastedContent.length != 0
                    && (x >= pastedAtX) && (y >= pastedAtY)
                    && ((x - pastedAtX) < originalPastedWidth) && ((y - pastedAtY) < originalPastedHeight)) {

                    //pick the value from the left top corner of pasted content and remove it from the pasted content data
                    var value = pastedContent[0][0];
                    tableModel.setItemValueAtXY(x, y, value);

                    pastedContent[0].splice(0, 1);
                    if (pastedContent[0].length == 0 || x==lastColumn) {
                        pastedContent.splice(0, 1);
                    }

                }
            }
        }
    });
};



function parsePaste(text){

    var rows = [];

    var STATE_HANDLER = {
        MULTICELL: function(text, symbol){
             text.substr()
        },
        NORMAL: function(text){

        }
    };


}
