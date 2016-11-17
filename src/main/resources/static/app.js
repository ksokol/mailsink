var app = angular.module("mailsinkApp", ["ngSanitize", "ui.bootstrap.modal", "ngStomp"]);

app.factory("errorBroadcastingHttpInterceptor", ["$q", "$rootScope", function($q, $rootScope) {
    return {
        "responseError": function(rejection) {
            if(rejection.status >= 500) {
                if(typeof rejection.data === "string") {
                    $rootScope.$broadcast("error", rejection.data);
                } else {
                    $rootScope.$broadcast("error", rejection.data.message);
                }
            }
            return $q.reject(rejection);
        }
    };
}]);

app.config(["$httpProvider", function($httpProvider) {
    $httpProvider.interceptors.push("errorBroadcastingHttpInterceptor");
}]);

app.config(["$compileProvider", function($compileProvider) {
    $compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|data):/);
}]);

app.directive("alertMessage", ["$rootScope", function($rootScope) {

    return {
        restrict: "E",
        link: function ($scope, element) {
            $rootScope.$on("error", function(event, message) {
                $scope.message = message;
                element.removeClass("hidden");
            });

            $scope.close = function() {
                element.addClass("hidden");
                $scope.message = null;
            };
        }
    }
}]);

app.controller("MailModalCtrl", ["$scope", "$rootScope", "$uibModal", function ($scope, $rootScope, $modal) {

    $rootScope.$on("mail-modal", function (event, mail) {
        var modalInstance = $modal.open({
            templateUrl: "mail.html",
            controller: function($scope) {
                $scope.mail = mail;
                $scope.close = function() {
                    modalInstance.close();
                }
            }
        });
    });
}]);

app.controller("MailCtrl", ["$scope", "$rootScope", "$http", "$stomp", function($scope, $rootScope, $http, $stomp) {

    $scope.mails = [];

    var fetch = function() {
        $http({
            method: "GET",
            url: "mails/search/findAllOrderByCreatedAtDesc"
        }).then(function successCallback(response) {
            $scope.mails = response.data._embedded.mails;
        });
    };

    $stomp.connect("/incoming-mail")
    .then(function () {
        $stomp.subscribe("/topic/incoming-mail", function () {
            fetch();
        });
    });

    fetch();

    $rootScope.$on("refresh", function() {
        fetch();
    });

    $scope.click = function(mail) {
        $rootScope.$emit("mail-modal", mail);
    }
}]);

app.controller("NavigationCtrl", ["$scope", "$rootScope","$http", function($scope, $rootScope, $http) {

    $scope.createMail = function() {
        $http({
            method: "POST",
            url: "createMail"
        }).then(function successCallback() {
            $rootScope.$emit("refresh");
        });
    };

    $scope.refresh = function() {
        $rootScope.$emit("refresh");
    };

    $scope.purge = function() {
        $http({
            method: "POST",
            url: "purge"
        }).then(function successCallback() {
            $rootScope.$emit("refresh");
        });
    };
}]);

app.directive("messageText", function() {

    var formatPlain = function (text) {
        var split = text.split("\r\n");
        if(split.length === 1) {
            return text.split("\n");
        }
        return split;
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

app.filter("urlToLink", ["$sanitize", function($sanitize) {
    var HREF_REGEXP = /(?:http?)[^\s]+/gi;

    var addBlankTarget = function(text) {
        return '<a href="' + text + '" target="_blank">' + text + '</a>';
    };

    return function(text) {
        if (!text) {
            return "";
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

app.component("attachmentsPanel", {
    bindings: {
        attachments: "<"
    },
    templateUrl: "attachments-panel.html"
});
