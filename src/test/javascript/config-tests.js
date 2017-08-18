describe('src/test/javascript/config-tests.js', function () {
    describe('TOPIC_PREFIX', function () {

        beforeEach(module('mailsinkApp'));

        it('should match backend topic', inject(function (TOPIC_PREFIX) {
            expect(TOPIC_PREFIX).toEqual('/topic');
        }));
    });

    describe('WEB_SOCKET_ENDPOINT', function () {

        var hostname = 'expectedHost';
        var port = 99999;

        beforeEach(module('mailsinkApp', function ($provide) {
            $provide.provider('$window', function () {
                return {
                    $get: function () {
                        return {
                            location: {
                                hostname: hostname,
                                port: port
                            }
                        };
                    }
                };
            });
        }));

        it('should match backend websocket endpoint', inject(function (WEB_SOCKET_ENDPOINT) {
            expect(WEB_SOCKET_ENDPOINT).toEqual('ws://' + hostname + ':' + port + '/ws/websocket');
        }));
    });

    describe('stompProvider', function () {

        beforeEach(module('mailsinkApp'));

        it('should return Stomp websocket client', inject(function (stompFactory) {
            expect(stompFactory).toEqual(Stomp);
        }));
    });
});
