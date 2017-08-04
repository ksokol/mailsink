describe('$sanitize', function() {

    var sanitize;

    beforeEach(module('mailsinkApp'));

    beforeEach(inject(function($sanitize) {
        sanitize = $sanitize;
    }));

    it('should allow data uris to render', function() {
        var element = sanitize('<a href="data:application/pdf;base64,"></a>');
        expect(element).toEqual('<a href="data:application/pdf;base64,"></a>');
    });

    it('should allow http urls to render', function() {
        var element = sanitize('<a href="http://localhost"></a>');
        expect(element).toEqual('<a href="http://localhost"></a>');
    });

    it('should allow https urls to render', function() {
        var element = sanitize('<a href="https://localhost"></a>');
        expect(element).toEqual('<a href="https://localhost"></a>');
    });
});

describe('TOPIC_PREFIX', function() {

    beforeEach(module('mailsinkApp'));

    it('should match backend topic', inject(function(TOPIC_PREFIX) {
        expect(TOPIC_PREFIX).toEqual('/topic');
    }));
});

describe('WEB_SOCKET_ENDPOINT', function() {

    var hostname = 'expectedHost';
    var port = 99999;

    beforeEach(module('mailsinkApp', function($provide) {
        $provide.provider('$window', function() {
            return {
                $get: function() {
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

    it('should match backend websocket endpoint', inject(function(WEB_SOCKET_ENDPOINT) {
        expect(WEB_SOCKET_ENDPOINT).toEqual('ws://' + hostname + ':' + port + '/ws/websocket');
    }));
});

describe('stompProvider', function() {

    beforeEach(module('mailsinkApp'));

    it('should return Stomp websocket client', inject(function(stompFactory) {
        expect(stompFactory).toEqual(Stomp);
    }));
});

describe('BASE_URL', function() {

    beforeEach(module('mailsinkApp'));

    it('should match base url', inject(function(BASE_URL) {
        var expectedBaseUrl = window.location.protocol + '//' + window.location.hostname + ':' + window.location.port;
        expect(BASE_URL).toEqual(expectedBaseUrl);
    }));
});
