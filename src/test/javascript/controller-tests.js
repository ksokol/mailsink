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

    it('should create new mail', function () {
        httpBackend.when('POST', 'createMail').respond(204);

        scope.createMail();
        httpBackend.flush();
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

    var scope, rootScope;

    describe('mail modal', function() {

        var modalScope = {};

        var modalInstance = {
            configuration: {},
            closeFn: {
                close: jasmine.createSpy('modalInstance.close')
            },
            open: function(configuration) {
                this.configuration = configuration;
                return this.closeFn;
            }
        };

        beforeEach(module('mailsinkApp', function($provide) {
            $provide.provider('$uibModal', function() {
                return {
                    $get: function() {
                        return modalInstance;
                    }
                };
            });
        }));

        beforeEach(inject(function ($rootScope, $controller, _$httpBackend_) {
            scope = $rootScope.$new();

            $controller('MailCtrl', {
                $scope: scope
            });

            _$httpBackend_.when('GET', 'mails/search/findAllOrderByCreatedAtDesc').respond(200, { _embedded: { mails:  'irrelevant' }});
            _$httpBackend_.flush();

            scope.click('the mail');
        }));

        it('should open modal with given templateUrl', function () {
            expect(modalInstance.configuration.templateUrl).toBe('mail-modal.html');
        });

        it('should pass mail to modal', function () {
            modalInstance.configuration.controller(modalScope);

            expect(modalScope.mail).toBe('the mail');
        });

        it('should close modal when close button clicked', function () {
            modalInstance.configuration.controller(modalScope);
            modalScope.close();

            expect(modalInstance.closeFn.close).toHaveBeenCalled();
        });
    });

    describe('on interaction', function() {

        var httpBackend;

        var stomp = {};

        var aMail = {
            'messageId' : '<68508964.31.1477845062277@localhost>',
            'sender' : 'root@localhost',
            'recipient' : 'root@localhost',
            'subject' : 'Subject',
            'text' : 'mail body',
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
                };
            });
        }));

        beforeEach(inject(function ($rootScope, $controller, _$httpBackend_) {
            scope = $rootScope.$new();
            httpBackend = _$httpBackend_;
            rootScope = $rootScope;

            $controller('MailCtrl', {
                $scope: scope,
                $rootScope: rootScope
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

            rootScope.$emit('refresh');
            httpBackend.flush();

            expect(scope.mails).toBe('refreshed mails');
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
});
