describe('NavigationCtrl controller', function() {

    beforeEach(module('mailsinkApp'));

    var scope, rootScope, httpBackend, modal;

    beforeEach(inject(function ($rootScope, $controller, _$httpBackend_) {
        scope = $rootScope.$new();
        httpBackend = _$httpBackend_;

        rootScope = {
            $emit: jasmine.createSpy('mock')
        };

        $controller('NavigationCtrl', {
            $scope: scope,
            $rootScope: rootScope,
            $uibModal: modal
        });
    }));

    afterEach(function() {
        httpBackend.verifyNoOutstandingExpectation();
        httpBackend.verifyNoOutstandingRequest();
    });

    it('should emit refresh event when mail creation was successful', function () {
        httpBackend.when('POST', 'createMail').respond(204);

        scope.createMail();
        httpBackend.flush();

        expect(rootScope.$emit).toHaveBeenCalledWith('refresh');
    });

    it('should emit refresh event when refresh() has been called on controller', function () {
        scope.refresh();

        expect(rootScope.$emit).toHaveBeenCalledWith('refresh');
    });

    it('should emit refresh event when purge was successful', function () {
        httpBackend.when('POST', 'purge').respond(204);

        scope.purge();
        httpBackend.flush();

        expect(rootScope.$emit).toHaveBeenCalledWith('refresh');
    });
});

describe('MailCtrl controller', function() {

    var scope, rootScope, httpBackend, modal;
    var stomp = {};

    var aMail = {
        'messageId' : '<68508964.31.1477845062277@localhost>',
        'sender' : 'root@localhost',
        'recipient' : 'root@localhost',
        'subject' : 'Subject',
        'body' : 'mail body',
        'attachments' : [ {
            'filename' : 'example.pdf',
            'mimeType' : 'application/pdf',
            'data' : 'data',
            '_links' : {
                'mail' : {
                    'href' : 'http://localhost:2525/mails/2'
                }
            }
        } ],
        'createdAt' : '2016-10-30T16:31:02.000+0000'
    };

    var aResponse = {
        '_embedded' : {
            'mails' : [ aMail ]
        }
    };

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
                            stomp['callback'] = fn;
                        }
                    };
                }
            }
        });
    }));

    beforeEach(inject(function ($rootScope, $controller, _$httpBackend_) {
        scope = $rootScope.$new();
        httpBackend = _$httpBackend_;

        rootScope = $rootScope;

        spyOn($rootScope, '$emit');

        $controller('MailCtrl', {
            $scope: scope,
            $rootScope: $rootScope,
            $uibModal: modal
        });
    }));

    afterEach(function() {
        httpBackend.verifyNoOutstandingExpectation();
        httpBackend.verifyNoOutstandingRequest();
    });

    it('should fetch mails from backend when initialized', function () {
        httpBackend.when('GET', 'mails/search/findAllOrderByCreatedAtDesc').respond(200, aResponse);

        httpBackend.flush();

        expect(scope.mails).toEqual([ aMail ]);
    });

    it('should refresh mails when event refresh was fired', function () {
        httpBackend.when('GET', 'mails/search/findAllOrderByCreatedAtDesc').respond(200, { _embedded: { mails:  'refreshed mails' }} );

        httpBackend.flush();
        rootScope.$emit('refresh');

        expect(scope.mails).toBe('refreshed mails');
    });

    it('should emit mail-modal event when click event occurred on controller', function () {
        httpBackend.when('GET', 'mails/search/findAllOrderByCreatedAtDesc').respond(200, aResponse);

        httpBackend.flush();
        scope.click(aMail);

        expect(rootScope.$emit).toHaveBeenCalledWith('mail-modal', aMail);
    });

    it('should refresh mails when websocket message received', function() {
        expect(scope.mails).toEqual([]);

        stomp.callback();

        httpBackend.when('GET', 'mails/search/findAllOrderByCreatedAtDesc').respond(200, { _embedded: { mails:  'triggered by websocket message' }});
        httpBackend.flush();

        expect(scope.mails).toBe('triggered by websocket message');
    });

    it('should connect to proper broker and subscribe to proper topic', function() {
        httpBackend.when('GET', 'mails/search/findAllOrderByCreatedAtDesc').respond(200, { _embedded: { mails:  'triggered by websocket message' }});
        httpBackend.flush();

        expect(stomp.broker).toBe('/incoming-mail');
        expect(stomp.topic).toBe('/topic/incoming-mail');
    });
});

describe('MailModalCtrl controller', function() {

    beforeEach(module('mailsinkApp'));

    var scope, rootScope, modal;

    beforeEach(inject(function ($rootScope, $controller, $uibModal) {
        scope = $rootScope.$new();

        rootScope = $rootScope;
        modal = $uibModal;

        spyOn($uibModal, 'open');

        $controller('MailModalCtrl', {
            $scope: scope,
            $rootScope: $rootScope,
            $uibModal: $uibModal
        });
    }));

    it('should open modal with email when event mail-modal has been fired', function () {
        rootScope.$emit('mail-modal', {});
        expect(modal.open).toHaveBeenCalled();
    });
});
