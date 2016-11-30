describe('Service: stompService', function() {

    var stompService, rootScope;

    var stompMock = {
        listener: {}
    };

    var timeoutMock = {};

    beforeEach(module('mailsinkApp', function($provide) {
        $provide.provider('stompFactory', function() {
            return {
                $get: function() {
                    return {
                        endpoint: null,
                        client: function(endpoint) {
                            stompMock['endpoint'] = endpoint;
                            return {
                                connect: function(login,password, onConnect, onError) {
                                    stompMock['onConnect'] = onConnect;
                                    stompMock['onError'] = onError;
                                },
                                subscribe: function(topic, fn) {
                                    stompMock.listener[topic] = fn;
                                }
                            };
                        }
                    };
                }
            };
        });

        $provide.provider('WEB_SOCKET_ENDPOINT', function() {
            return {
                $get: function() {
                    return 'WEB_SOCKET_ENDPOINT';
                }
            };
        });

        $provide.provider('TOPIC_PREFIX', function() {
            return {
                $get: function() {
                    return 'TOPIC_PREFIX';
                }
            };
        });

        $provide.provider('$timeout', function() {
            return {
                $get: function() {
                    return function(fn, delay, invokeApply) {
                        timeoutMock['fn'] = fn;
                        timeoutMock['delay'] = delay;
                        timeoutMock['invokeApply'] = invokeApply;
                        fn(delay, invokeApply);
                    }
                }
            };
        });
    }));

    beforeEach(inject(function ($rootScope, _stompService_) {
        stompService = _stompService_;
        rootScope = $rootScope;
    }));

    xit('should connect to proper websocket endpoint', function() {
        expect(stompMock.endpoint).toEqual('WEB_SOCKET_ENDPOINT');
    });

    it('should connect with proper topic prefix', function() {
        stompService.subscribe('aTopic', function() {});
        stompMock.onConnect();
        rootScope.$digest();

        expect(stompMock.listener['/topic/aTopic']).toBeDefined();
    });

    it('should propagate message to subscribers', function(done) {
        stompService.subscribe('firstTopic', function(data) {
            expect(data).toEqual(['expected firstTopic message']);
            done();
        });

        stompService.subscribe('secondTopic', function(data) {
            expect(data).toEqual(['expected secondTopic message']);
            done();
        });

        stompMock.onConnect();
        rootScope.$digest();

        stompMock.listener['/topic/firstTopic']({
            headers: {
                destination: '/topic/firstTopic'
            },
            body: '["expected firstTopic message"]'
        });

        stompMock.listener['/topic/secondTopic']({
            headers: {
                destination: '/topic/secondTopic'
            },
            body: '["expected secondTopic message"]'
        });
    });

    it('should not propagate message to unrelated topic', function() {
        stompService.subscribe('firstTopic', function() {
            fail('unexpected message');
        });

        stompMock.onConnect();
        rootScope.$digest();

        stompMock.listener['/topic/secondTopic']({
            headers: {
                destination: '/topic/secondTopic'
            },
            body: '["expected secondTopic message"]'
        });
    });

    it('should receive message after reconnect', function(done) {
        stompMock.onError();

        stompService.subscribe('firstTopic', function(data) {
            expect(data).toEqual(['expected firstTopic message']);
            done();
        });

        timeoutMock.fn();
        stompMock.onConnect();
        rootScope.$digest();

        stompMock.listener['/topic/firstTopic']({
            headers: {
                destination: '/topic/firstTopic'
            },
            body: '["expected firstTopic message"]'
        });
    });

    it('should try to reconnect after a predefined amount of milliseconds', function() {
        stompMock.onError();

        expect(timeoutMock.delay).toEqual(1000);
    });

    it('should not call apply', function() {
        stompMock.onError();

        expect(timeoutMock.invokeApply).toEqual(false);
    });
});
