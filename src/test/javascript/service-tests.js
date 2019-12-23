describe('src/test/javascript/service-tests.js', function () {
    describe('Service: stompService', function () {

        var stompService, rootScope;

        var stompMock = {
            listener: {}
        };

        var timeoutMock = {};

        beforeEach(module('mailsinkApp', function ($provide) {
            $provide.provider('stompFactory', function () {
                return {
                    $get: function () {
                        return {
                            endpoint: null,
                            client: function (endpoint) {
                                stompMock['endpoint'] = endpoint;
                                return {
                                    connect: function (login, password, onConnect, onError) {
                                        stompMock['onConnect'] = onConnect;
                                        stompMock['onError'] = onError;
                                    },
                                    subscribe: function (topic, fn) {
                                        stompMock.listener[topic] = fn;
                                    }
                                };
                            }
                        };
                    }
                };
            });

            $provide.provider('WEB_SOCKET_ENDPOINT', function () {
                return {
                    $get: function () {
                        return 'WEB_SOCKET_ENDPOINT';
                    }
                };
            });

            $provide.provider('TOPIC_PREFIX', function () {
                return {
                    $get: function () {
                        return 'TOPIC_PREFIX';
                    }
                };
            });

            $provide.provider('$timeout', function () {
                return {
                    $get: function () {
                        return function (fn, delay, invokeApply) {
                            timeoutMock['fn'] = fn;
                            timeoutMock['delay'] = delay;
                            timeoutMock['invokeApply'] = invokeApply;
                            fn(delay, invokeApply);
                        };
                    }
                };
            });
        }));

        beforeEach(inject(function ($rootScope, _stompService_) {
            stompService = _stompService_;
            rootScope = $rootScope;
        }));

        it('should connect to proper websocket endpoint', function () {
            expect(stompMock.endpoint).toEqual('WEB_SOCKET_ENDPOINT');
        });

        it('should connect with proper topic prefix', function () {
            stompService.subscribe('aTopic', function () {
            });
            stompMock.onConnect();
            rootScope.$digest();

            expect(stompMock.listener['/topic/aTopic']).toBeDefined();
        });

        it('should propagate message to subscribers', function (done) {
            var calls = 0;
            var triggerDone = function () {
                calls += 1;
                if (calls === 2) {
                    done();
                }
            }

            stompService.subscribe('firstTopic', function (data) {
                expect(data).toEqual(['expected firstTopic message']);
                triggerDone();
            });

            stompService.subscribe('secondTopic', function (data) {
                expect(data).toEqual(['expected secondTopic message']);
                triggerDone();
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

        it('should receive message after reconnect', function (done) {
            stompMock.onError();

            stompService.subscribe('firstTopic', function (data) {
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

        it('should try to reconnect after a predefined amount of milliseconds', function () {
            stompMock.onError();

            expect(timeoutMock.delay).toEqual(1000);
        });

        it('should not call apply', function () {
            stompMock.onError();

            expect(timeoutMock.invokeApply).toEqual(false);
        });
    });

    describe('alertService', function () {

        var rootScope, alertService;

        beforeEach(module('mailsinkApp'));

        beforeEach(inject(function ($rootScope, _alertService_) {
            rootScope = $rootScope;
            spyOn($rootScope, '$broadcast');

            alertService = _alertService_;
        }));

        it('should broadcast error', function () {
            alertService.alert({status: 500, data: 'expected error'});
            expect(rootScope.$broadcast).toHaveBeenCalledWith('error', 'expected error');
        });

        it('should broadcast error message from error message object', function () {
            alertService.alert({status: 500, data: {message: 'expected error'}});
            expect(rootScope.$broadcast).toHaveBeenCalledWith('error', 'expected error');
        });

        it('should not broadcast error message when status code is lower than 500', function () {
            alertService.alert({status: 200});
            expect(rootScope.$broadcast).not.toHaveBeenCalled();

            alertService.alert({status: 400});
            expect(rootScope.$broadcast).not.toHaveBeenCalled();
        });

        it('should broadcast error message when status code is lower than or equal to 0', function () {
            alertService.alert({status: 0});
            expect(rootScope.$broadcast).toHaveBeenCalledWith('error', 'Network error');

            alertService.alert({status: -1});
            expect(rootScope.$broadcast).toHaveBeenCalledWith('error', 'Network error');
        });
    });

    describe('curlConverter', function () {

        var curlConverter;

        beforeEach(module('mailsinkApp'));

        beforeEach(inject(function (_curlConverter_) {
            curlConverter = _curlConverter_;
        }));

        it('should shell escape command', function () {
            var cmd = curlConverter.toCommand('expected url', "expected 'body'");

            expect(cmd).toEqual("curl -X POST 'expected url' -H 'content-type: application/json' -d 'expected '\\''body'\\'");
        });
    });
});
