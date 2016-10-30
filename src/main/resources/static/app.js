var app = angular.module('mailsinkApp', ['ngSanitize']);

app.filter('urlToLink', ['$sanitize', function($sanitize) {
    var HREF_REGEXP = /(?:http?)[^\s]+/gi;

    var addBlankTarget = function(text) {
        return '<a href="' + text + '" target="_blank">' + text + '</a>';
    };

    return function(text) {
        if (!text) {
            return '';
        }

        var match, raw = text;
        var matches = [];

        while ((match = raw.match(HREF_REGEXP))) {
            matches.push(match[0]);
            var indexOf = raw.indexOf(match[0]);
            raw = raw.substr(indexOf + match[0].length);
        }

        for(var j=0;j<matches.length;j++) {
            var regexMatch = matches[j];
            var blank = addBlankTarget(regexMatch);
            text = text.replace(regexMatch, blank);
        }

        return $sanitize(text);
    };
}]);
