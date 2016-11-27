describe('Service: stompService', function() {

    var stomp = {};

    var stompService;

    beforeEach(module('mailsinkApp', function($provide) {
        $provide.provider('$stomp', function() {
            return {
                $get: function() {
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
                            stomp['push'] = fn;
                        }
                    };
                }
            };
        });
    }));

    beforeEach(inject(function ($stomp, _stompService_) {
        stomp = $stomp;
        stompService = _stompService_;
    }));

    it('should push data to subscriber', function(done) {
        stompService.subscribe('test', function(data) {
            expect(data).toEqual('data');
            done()
        });

        stomp.push('data');
    });

    it('should subscribe to proper broker and topic', function() {
        stompService.subscribe('test');

        expect(stomp.broker).toEqual('/ws');
        expect(stomp.topic).toEqual('/topic/test');
    });
});
