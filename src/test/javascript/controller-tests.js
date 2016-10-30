describe("NavigationCtrl controller", function() {

    beforeEach(module('mailsinkApp'));

    var scope, rootScope, httpBackend, modal;

    beforeEach(inject(function ($rootScope, $controller, _$httpBackend_) {
        scope = $rootScope.$new();
        httpBackend = _$httpBackend_;

        rootScope = {
            $emit: jasmine.createSpy("mock")
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

    it("should emit 'refresh' event when mail creation was successful", function () {
        httpBackend.when('POST', 'createMail').respond(204);

        scope.createMail();
        httpBackend.flush();

        expect(rootScope.$emit).toHaveBeenCalledWith('refresh');
    });

    it("should show modal dialog when mail creation was unsuccessful", function () {
        httpBackend.when('POST', 'createMail').respond(500, { message: 'expected error' });

        scope.createMail();
        httpBackend.flush();

        expect(rootScope.$emit).toHaveBeenCalledWith('error', 'expected error');
    });

    it("should emit 'refresh' event when refresh() has been called on controller", function () {
        scope.refresh();

        expect(rootScope.$emit).toHaveBeenCalledWith('refresh');
    });

    it("should emit 'refresh' event when purge was successful", function () {
        httpBackend.when('POST', 'purge').respond(204);

        scope.purge();
        httpBackend.flush();

        expect(rootScope.$emit).toHaveBeenCalledWith('refresh');
    });

    it("should show modal dialog when purge unsuccessful", function () {
        httpBackend.when('POST', 'purge').respond(500, { message: 'expected error' });

        scope.purge();
        httpBackend.flush();

        expect(rootScope.$emit).toHaveBeenCalledWith('error', 'expected error');
    });
});

describe("MailCtrl controller", function() {

    beforeEach(module('mailsinkApp'));

    var scope, rootScope, httpBackend, modal;

    var aMail = {
        "messageId" : "<68508964.31.1477845062277@localhost>",
        "sender" : "root@localhost",
        "recipient" : "root@localhost",
        "subject" : "Subject",
        "body" : "mail body",
        "createdAt" : "2016-10-30T16:31:02.000+0000"
    };

    var aResponse = {
        "_embedded" : {
            "mails" : [ aMail ]
        }
    };

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

    it("should fetch mails from backend when initialized", function () {
        httpBackend.when('GET', 'mails/search/findAllOrderByCreatedAtDesc').respond(200, aResponse);

        httpBackend.flush();

        expect(scope.mails).toEqual([ aMail ]);
    });

    it("should emit 'error' event when mails can not be fetched from backend", function () {
        httpBackend.when('GET', 'mails/search/findAllOrderByCreatedAtDesc').respond(500, { message: 'expected error' });

        httpBackend.flush();

        expect(rootScope.$emit).toHaveBeenCalledWith('error', 'expected error');
    });

    it("should refresh mails when event 'refresh' was fired", function () {
        httpBackend.when('GET', 'mails/search/findAllOrderByCreatedAtDesc').respond(200, { _embedded: { mails:  'refreshed mails' }} );

        httpBackend.flush();
        rootScope.$emit('refresh');

        expect(scope.mails).toBe('refreshed mails');
    });

    it("should emit 'mail-modal' event when 'click' event occured on controller", function () {
        httpBackend.when('GET', 'mails/search/findAllOrderByCreatedAtDesc').respond(200, aResponse);

        httpBackend.flush();
        scope.click(aMail);

        expect(rootScope.$emit).toHaveBeenCalledWith('mail-modal', aMail);
    });
});

describe("MailModalCtrl controller", function() {

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

    it("should open modal with email when event 'mail-modal' has been fired", function () {
        rootScope.$emit('mail-modal', {});
        expect(modal.open).toHaveBeenCalled();
    });
});
