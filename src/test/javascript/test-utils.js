var app = angular.module('mockNgStomp', []);

app.service('$stomp', function() {
    var stomp = {};

    return {
        connect: function(broker) {
            stomp['broker'] = broker;
            return {
                then: function(fn) {
                    fn();
                }
            };
        },
        subscribe: function(url, fn) {
            stomp['topic'] = url;
            stomp['callback'] = fn;
        },
        push: function(data) {
            stomp.callback(data);
        },
        broker: function() {
            return stomp['broker'];
        },
        topic: function() {
            return stomp['topic'];
        }
    };
});
