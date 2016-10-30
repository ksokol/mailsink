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
