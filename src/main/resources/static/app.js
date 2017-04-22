var app = angular.module('mailsinkApp', ['ngSanitize', 'ui.bootstrap.tpls', 'ui.bootstrap.modal', 'ui.bootstrap.tabs', 'luegg.directives']);

app.constant('TOPIC_PREFIX', '/topic');

app.factory('WEB_SOCKET_ENDPOINT', function($window) {
    return 'ws://' + $window.location.hostname + ':' + $window.location.port + '/ws/websocket';
});

app.factory('stompFactory', function() {
    return Stomp;
});

app.factory('errorBroadcastingHttpInterceptor', ['$q', '$rootScope', function($q, $rootScope) {
    return {
        'responseError': function(rejection) {
            if(rejection.status >= 500) {
                if(typeof rejection.data === 'string') {
                    $rootScope.$broadcast('error', rejection.data);
                } else {
                    $rootScope.$broadcast('error', rejection.data.message);
                }
            }
            return $q.reject(rejection);
        }
    };
}]);

app.config(['$httpProvider', function($httpProvider) {
    $httpProvider.interceptors.push('errorBroadcastingHttpInterceptor');
}]);

app.config(['$compileProvider', function($compileProvider) {
    $compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|data):/);
}]);

app.directive('alertMessage', ['$rootScope', function($rootScope) {

    return {
        restrict: 'E',
        link: function ($scope, element) {
            $rootScope.$on('error', function(event, message) {
                $scope.message = message;
                element.removeClass('hidden');
            });

            $scope.close = function() {
                element.addClass('hidden');
                $scope.message = null;
            };
        }
    };
}]);

app.controller('MailCtrl', ['$scope', '$rootScope', '$http', '$uibModal', 'stompService', function($scope, $rootScope, $http, $modal, stompService) {

    $scope.mails = [];

    var fetch = function() {
        $http({
            method: 'GET',
            url: 'mails/search/findAllOrderByCreatedAtDesc'
        }).then(function successCallback(response) {
            $scope.mails = response.data._embedded.mails;
        });
    };

    stompService.subscribe('incoming-mail', fetch);

    fetch();

    $rootScope.$on('refresh', function() {
        fetch();
    });

    $scope.click = function(mail) {
        var modalInstance = $modal.open({
            templateUrl: 'mail-modal.html',
            controller: function($scope) {
                $scope.mail = mail;
                $scope.close = function() {
                    modalInstance.close();
                };
            }
        });
    };
}]);

app.controller('NavigationCtrl', ['$scope', '$rootScope','$http', function($scope, $rootScope, $http) {

    $scope.createMail = function() {
        $http({
            method: 'POST',
            url: 'createMail'
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
        });
    };
}]);

app.directive('messageText', function() {

    var formatPlain = function (text) {
        var split = text.split('\r\n');
        if(split.length === 1) {
            return text.split('\n');
        }
        return split;
    };

    return {
        restrict: 'E',
        scope: {
            text: '@'
        },
        template: '<p ng-repeat="line in messageText track by $index"><span ng-bind-html="::line | urlToLink:90"></span></p>',
        link: function ($scope, element, attrs) {
            if(attrs.text) {
                $scope.messageText = formatPlain(attrs.text);
            } else {
                $scope.messageText = [];
            }
        }
    };
});

app.filter('urlToLink', ['$sanitize', function($sanitize) {
    var HREF_REGEXP = /(?:http?)[^\s]+/gi;

    var addBlankTarget = function(url, maxLength) {
        var displayText = url;

        if(url.length > maxLength) {
            displayText = url.substr(0, maxLength) + '...';
        }

        return '<a href="' + url + '" target="_blank">' + displayText + '</a>';
    };

    return function(text, maxLength) {
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
            var blank = addBlankTarget(regexMatch, maxLength);
            text = text.replace(regexMatch, blank);
        }

        return $sanitize(text);
    };
}]);

app.component('attachmentsPanel', {
    bindings: {
        attachments: '<'
    },
    templateUrl: 'attachments-panel.html'
});

app.component('mailBodyPanel', {
    bindings: {
        mail: '<'
    },
    templateUrl: 'mail-body-panel.html'
});

app.component('messageHtml', {
    bindings: {
        id: '<'
    },
    template: '<iframe class="hidden" frameborder="0" ng-src="{{url}}"></iframe>',
    controller: function($scope, $element) {
        $scope.url = 'mails/' + this.id + '/html';

        var iframe = $element.find('iframe');

        iframe.on('load', function() {
            var height = iframe[0].contentWindow.document.body.scrollHeight + 'px';
            iframe.css('width', '100%');
            iframe.css('height', height);
            iframe.removeClass('hidden');
        });
    }
});

app.component('messageSource', {
    bindings: {
        id: '<'
    },
    template: '<a target="_blank" href="{{url}}">Source</a>',
    controller: function($scope) {
        $scope.url = 'mails/' + this.id + '/source';
    }
});

app.component('smtpLog', {
    templateUrl: 'smtp-log.html',
    controller: function($scope, $element, stompService) {
        var buffer = new CBuffer(50);

        stompService.subscribe('smtp-log', function(logLine) {
            buffer.push(logLine);
        });

        $scope.emptyLogs = function() {
            return buffer.toArray().length === 0;
        };

        $scope.logs = function() {
            return buffer.toArray();
        };
    }
});

app.service('stompService', function(WEB_SOCKET_ENDPOINT, TOPIC_PREFIX, $q, $timeout, $log, stompFactory) {
    var deferred = $q.defer();
    var listeners = [];
    var stompClient;

    var connect = function() {
        deferred = $q.defer();
        stompClient =  stompFactory.client(WEB_SOCKET_ENDPOINT);
        stompClient.debug = function() { /* suppress debug logs */ };

        $log.log('connecting....');
        stompClient.connect('', '', onConnect, onError);

        angular.forEach(listeners, function(listener) {
            subscribeInternal(listener);
        });
    };

    var onConnect = function() {
        $log.log('connected');
        deferred.resolve();
    };

    var onError =  function() {
        var timeout = 1000;
        $log.log('connection lost. Trying to reconnect in ' + timeout + ' milliseconds');
        $timeout(connect, timeout, false);
    };

    var subscribeInternal = function(listener) {
        deferred.promise.then(function() {
            stompClient.subscribe(listener.destination, function (msg) {
                angular.forEach(listeners, function (listener) {
                    if (listener.destination === msg.headers.destination) {
                        listener.listenFunction(JSON.parse(msg.body));
                    }
                });
            });
        });
    };

    var addListener = function(topicName, fn) {
        var listener = {
            destination : TOPIC_PREFIX + '/' + topicName,
            listenFunction : fn
        };
        listeners.push(listener);
        return listener;
    };

    connect();

    return {
        subscribe: function(topicName, fn) {
            subscribeInternal(addListener(topicName, fn));
        }
    };
});

app.directive('toggleSmtpServer', function($http) {
    return {
        restrict: 'A',
        link: function ($scope, element) {
            var toggle = function(response) {
                if(response.data.isRunning) {
                    element.removeClass('glyphicon glyphicon-stop');
                    element.addClass('glyphicon glyphicon-play');
                } else {
                    element.removeClass('glyphicon glyphicon-play');
                    element.addClass('glyphicon glyphicon-stop');
                }
            };

            $http.get('smtpServer/status').then(toggle);

            element.on('click', function() {
                $http.post('smtpServer/status/toggle', {}).then(toggle);
            });
        }
    };
});
