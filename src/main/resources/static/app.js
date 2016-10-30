var app = angular.module('mailsinkApp', ['ngSanitize', 'ui.bootstrap.modal']);

app.controller('MailModalCtrl', ['$scope', '$rootScope', '$uibModal', function ($scope, $rootScope, $modal) {

    $rootScope.$on('mail-modal', function (event, mail) {
        var modalInstance = $modal.open({
            templateUrl: 'mail.html',
            controller: function($scope) {
                $scope.mail = mail;
                $scope.close = function() {
                    modalInstance.close();
                }
            }
        });
    });
}]);

app.controller('MailCtrl', ['$scope', '$rootScope', '$http', function($scope, $rootScope, $http) {

    $scope.mails = [];

    var fetch = function() {
        $http({
            method: 'GET',
            url: 'mails/search/findAllOrderByCreatedAtDesc'
        }).then(function successCallback(response) {
            $scope.mails = response.data._embedded.mails;
        }, function errorCallback(response) {
            $rootScope.$emit('error', response.data.message);
        });
    };

    fetch();

    $rootScope.$on('refresh', function() {
        fetch();
    });

    $scope.click = function(mail) {
        $rootScope.$emit('mail-modal', mail);
    }
}]);

app.controller('NavigationCtrl', ['$scope', '$rootScope','$http', function($scope, $rootScope, $http) {

    $scope.createMail = function() {
        $http({
            method: 'POST',
            url: 'createMail'
        }).then(function successCallback() {
            $rootScope.$emit('refresh');
        }, function errorCallback(response) {
            $rootScope.$emit('error', response.data.message);
        });
    };

    $scope.refresh = function() {
        $rootScope.$emit('refresh');
    };

    $scope.purge = function() {
        $http({
            method: 'POST',
            url: 'purge'
        }).then(function successCallback() {
            $rootScope.$emit('refresh');
        }, function errorCallback(response) {
            $rootScope.$emit('error', response.data.message);
        });
    };
}]);

app.directive("messageText", function() {

    var cleanLines = function(lines) {
        var cleanedLines = [];

        for(var i in lines) {
            var line = lines[i];
            if(line.length !== 0) {
                cleanedLines.push(line);
            }
        }
        return cleanedLines;
    };

    var formatPlain = function (text) {
        var split = text.split("\r\n");
        if(split.length === 1) {
            return cleanLines(text.split("\n"));
        }
        return cleanLines(split);
    };

    return {
        restrict: "E",
        link: function ($scope) {
            //TODO support html mails
            if($scope.mail.body) {
                $scope.messageText = formatPlain($scope.mail.body);
            } else {
                $scope.messageText = [];
            }
        }
    }
});

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
