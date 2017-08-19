var app = angular.module('mailsinkApp',
    [
        'ngSanitize',
        'ui.bootstrap.tpls',
        'ui.bootstrap.modal',
        'ui.bootstrap.tabs',
        'ui.bootstrap.dropdown',
        'luegg.directives',
        'angular-clipboard',
        'hljs'
    ]);

app.constant('TOPIC_PREFIX', '/topic');

app.factory('WEB_SOCKET_ENDPOINT', function($window) {
    return 'ws://' + $window.location.hostname + ':' + $window.location.port + '/ws/websocket';
});

app.factory('stompFactory', function() {
    return Stomp;
});

app.service('alertService', ['$rootScope', function($rootScope) {

    return {
        alert: function(response) {
            if(response.status >= 500) {
                if(typeof response.data === 'string') {
                    $rootScope.$broadcast('error', response.data);
                } else {
                    $rootScope.$broadcast('error', response.data.message);
                }
            }
            if(response.status <= 0) {
                $rootScope.$broadcast('error', 'Network error');
            }
        }
    };
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

app.controller('MailCtrl', ['$scope', '$rootScope', '$http', '$uibModal', 'stompService', 'alertService',
    function($scope, $rootScope, $http, $modal, stompService, alertService) {

    $scope.mails = [];

    var fetch = function() {
        $http({
            method: 'GET',
            url: 'mails/search/findAllOrderByCreatedAtDesc'
        }).then(function successCallback(response) {
            $scope.mails = response.data._embedded.mails;
        }).catch(alertService.alert);
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

app.controller('NavigationCtrl', ['$scope', '$rootScope','$http', 'alertService', function($scope, $rootScope, $http, alertService) {

    $scope.createMail = function() {
        $http({
            method: 'POST',
            url: 'smtpServer/createMail'
        }).catch(alertService.alert);
    };

    $scope.refresh = function() {
        $rootScope.$emit('refresh');
    };

    $scope.purge = function() {
        $http({
            method: 'POST',
            url: 'mails/purge'
        }).then(function successCallback() {
            $rootScope.$emit('refresh');
        }).catch(alertService.alert);
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
        mail: '<'
    },
    templateUrl: 'attachments-panel.html',
    controller: function ($http) {
        var ctrl = this;

        ctrl.$onInit = function () {
            $http({
                method: 'GET',
                url: ctrl.mail._links.attachments.href
            }).then(function(response) {
                ctrl.attachments = response.data._embedded.mailAttachments;
            }).catch(function() {
                ctrl.errorMessage = true;
            });
        }
    }
});

app.component('mailBodyPanel', {
    bindings: {
        mail: '<'
    },
    templateUrl: 'mail-body-panel.html',
    controller: function () {
        var ctrl = this;

        ctrl.openHtmlBodyQueryPanel = function () {
            ctrl.showHtmlBodyQueryPanel = !ctrl.showHtmlBodyQueryPanel;
        }
    }
});

app.component('messageHtml', {
    bindings: {
        mail: '<'
    },
    template: '',
    controller: function ($element) {
        this.$onInit = function () {
            var shadowRoot = $element[0].attachShadow({mode: 'open'});
            shadowRoot.innerHTML = this.mail.content.html;
        }
    }
});

app.component('messageSource', {
    bindings: {
        mail: '<'
    },
    template: '<a target="_blank" href="{{$ctrl.mail._links.source.href}}">Source</a>'
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

app.directive('toggleSmtpServer', ['$http', 'alertService', function($http, alertService) {
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

            $http.get('smtpServer/status')
                .then(toggle)
                .catch(alertService.alert);

            element.on('click', function() {
                $http.post('smtpServer/status/toggle', {})
                    .then(toggle)
                    .catch(alertService.alert);
            });
        }
    };
}]);

app.service('curlConverter', function () {
    var shellescape = function (cmdArray) {
        var ret = [];

        // https://github.com/xxorax/node-shell-escape
        cmdArray.forEach(function(cmdPart) {
            if (/[^A-Za-z0-9_\/:=-]/.test(cmdPart)) {
                cmdPart = "'"+cmdPart.replace(/'/g,"'\\''") + "'";
                cmdPart = cmdPart.replace(/^(?:'')+/g, '').replace(/\\'''/g, "\\'" );
            }
            ret.push(cmdPart);
        });

        return ret.join(' ');
    };

    return {
        toCommand: function (url, body) {
            return shellescape([
                'curl', '-X', 'POST', url,
                '-H', 'content-type: application/json',
                '-d', body
            ]);
        }
    }
});

app.component('messageHtmlQuery', {
    bindings: {
        mail: '<'
    },
    templateUrl: 'html-body-query.html',
    controller: function ($http, clipboard, curlConverter) {
        var ctrl = this;

        var queryUrl = function () {
            return ctrl.mail._links.query.href;
        };

        var xpathQuery = function () {
            return ctrl.xpath ? ctrl.xpath : '*';
        };

        var queryHtml = function (xpath) {
            ctrl.errorMessage = null;
            ctrl.showHtmlSource = false;
            $http.post(queryUrl(), {xpath: xpath}).then(function(response) {
                ctrl.queryResult = JSON.stringify(response.data, null, 1);
            }).catch(function (error) {
                ctrl.errorType = error.status >= 500 ? 'danger' : 'warning';
                ctrl.errorMessage = error.data.message;
            });
        };

        ctrl.$onInit = function () {
            ctrl.onQuery();
        };

        ctrl.onQuery = function () {
            queryHtml(xpathQuery());
        };

        ctrl.copyToClipboardAsCurl = function () {
            var body = JSON.stringify({xpath: xpathQuery()});
            var cmd = curlConverter.toCommand(queryUrl(), body);
            clipboard.copyText(cmd);
        };

        ctrl.toggleSource = function () {
            ctrl.showHtmlSource = !ctrl.showHtmlSource;
        }
    }
});

