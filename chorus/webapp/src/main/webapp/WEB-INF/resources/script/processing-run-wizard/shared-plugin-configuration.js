angular.module("experiments-front").factory("SharedPluginConfiguration", function () {
  return {
      init: function (scope) {
          var sharedPluginConfiguration = {};

          scope.$on("pluginConfigurationChanged", function (e, newPluginConfiguration) {
              Object.assign(sharedPluginConfiguration, newPluginConfiguration);
          });

          scope.applySharedPluginConfigForActivePlugin = function () {
              Object.keys(sharedPluginConfiguration).forEach(function (paramName, index) {
                  $("[name=\"" + paramName +"\"]").each(function (index, item) {
                      var $item = $(item);
                      if(sharedPluginConfiguration[paramName] !== "" && sharedPluginConfiguration[paramName] !== null) {
                          $item.prop("disabled", false);
                      }

                      $item.val(sharedPluginConfiguration[paramName]);

                      if ($item.prop("type") === "checkbox") {
                          sharedPluginConfiguration[paramName] ? $item.prop("checked", "checked") : $item.prop("checked", "");
                      }
                  })
              })
          }
      }
  }
});