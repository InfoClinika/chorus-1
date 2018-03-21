
var urlUtils = {};

urlUtils.getUrlVars = function(url){
    var vars = {};
    url = url || window.location.href;
    var parts = url.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value.replace(/[+]/g, " ");
    });
    return vars;
};


