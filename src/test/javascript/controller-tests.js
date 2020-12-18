describe('src/test/javascript/controller-tests.js', function () {
  describe('NavigationCtrl controller', function () {

    beforeEach(module('mailsinkApp'));

    var scope, rootScope, httpBackend, modal, alertService;

    beforeEach(inject(function ($rootScope, $controller, _$httpBackend_, _alertService_) {
      scope = $rootScope.$new();
      httpBackend = _$httpBackend_;

      alertService = _alertService_;
      spyOn(alertService, 'alert');

      rootScope = {
        $emit: jasmine.createSpy('mock')
      };

      $controller('NavigationCtrl', {
        $scope: scope,
        $rootScope: rootScope,
        $uibModal: modal
      });
    }));

    afterEach(function () {
      httpBackend.verifyNoOutstandingExpectation();
      httpBackend.verifyNoOutstandingRequest();
    });

    it('should create new mail', function () {
      httpBackend.when('POST', 'smtp').respond(204);

      scope.createMail();
      httpBackend.flush();
    });

    it('should emit refresh event when refresh() has been called on controller', function () {
      scope.refresh();

      expect(rootScope.$emit).toHaveBeenCalledWith('refresh');
    });

    it('should emit refresh event when purge was successful', function () {
      httpBackend.when('POST', 'mails/purge').respond(204);

      scope.purge();
      httpBackend.flush();

      expect(rootScope.$emit).toHaveBeenCalledWith('refresh');
    });

    it('should forward error response to alertService when mail submission failed', function () {
      httpBackend.when('POST', 'smtp').respond(500, 'expected error');

      scope.createMail();
      httpBackend.flush();

      expect(alertService.alert).toHaveBeenCalledWith(jasmine.objectContaining({
        status: 500,
        data: 'expected error'
      }));
    });

    it('should forward error response to alertService when purge request failed', function () {
      httpBackend.when('POST', 'mails/purge').respond(500, 'expected error');

      scope.purge();
      httpBackend.flush();

      expect(alertService.alert).toHaveBeenCalledWith(jasmine.objectContaining({
        status: 500,
        data: 'expected error'
      }));
    });
  });

  describe('MailCtrl controller', function () {

    var scope, rootScope, alertService;

    describe('mail modal', function () {

      var modalScope = {};

      var modalInstance = {
        configuration: {},
        closeFn: {
          close: jasmine.createSpy('modalInstance.close')
        },
        open: function (configuration) {
          this.configuration = configuration;
          return this.closeFn;
        }
      };

      beforeEach(module('mailsinkApp', function ($provide) {
        $provide.provider('$uibModal', function () {
          return {
            $get: function () {
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

        _$httpBackend_.when('GET', 'mails/search/findAllOrderByCreatedAtDesc').respond(200, {_embedded: {mails: 'irrelevant'}});
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

    describe('on interaction', function () {

      var httpBackend, httpCallChain;

      var aMail = {
        'content': {
          'messageId': '<68508964.31.1477845062277@localhost>',
          'sender': 'root@localhost',
          'recipient': 'root@localhost',
          'subject': 'Subject',
          'text': 'mail body',
          'attachments': [{
            'filename': 'example.pdf',
            'mimeType': 'application/pdf',
            'data': 'data',
            '_links': {
              'mail': {
                'href': 'http://localhost:2525/mails/2'
              }
            }
          }],
          'createdAt': '2016-10-30T16:31:02.000+0000'
        },
        '_links': {
          'self': {
            'href': 'http://localhost:2525/mails/1'
          }
        }
      };

      var aResponse = {
        '_embedded': {
          'mails': [aMail]
        }
      };

      var sourceUrl;
      var sourceInstance;

      beforeEach(function () {
        sourceInstance = {
          onmessage: null
        };

        window.ReconnectingEventSource = function (url) {
          sourceUrl = url;
          return sourceInstance;
        }
      });

      beforeEach(module('mailsinkApp'));

      beforeEach(inject(function ($rootScope, $controller, _$httpBackend_, _alertService_) {
        scope = $rootScope.$new();
        httpBackend = _$httpBackend_;
        rootScope = $rootScope;

        alertService = _alertService_;
        spyOn(alertService, 'alert');

        $controller('MailCtrl', {
          $scope: scope,
          $rootScope: rootScope
        });

        httpCallChain = httpBackend.when('GET', 'mails/search/findAllOrderByCreatedAtDesc').respond(200, aResponse);
      }));

      it('should fetch mails from backend when initialized', function () {
        httpBackend.flush();

        expect(scope.mails).toEqual([aMail]);
      });

      it('should refresh mails when event refresh was fired', function () {
        httpCallChain.respond(200, {_embedded: {mails: [{attr: 'refreshed mails'}]}});

        rootScope.$emit('refresh');
        httpBackend.flush();

        expect(scope.mails).toEqual([{attr: 'refreshed mails'}]);
      });

      it('should refresh mails when message arrived', function () {
        httpCallChain.respond(200, {_embedded: {mails: [{attr: 'triggered by websocket message'}]}});
        sourceInstance.onmessage();

        scope.$digest();
        httpBackend.flush();

        expect(scope.mails).toEqual([{attr: 'triggered by websocket message'}]);
      });

      it('should subscribe to proper stream url', function () {
        httpBackend.flush();

        expect(sourceUrl).toEqual('mails/stream');
      });

      it('should forward error response to alertService', function () {
        httpCallChain.respond(500, 'expected error');
        httpBackend.flush();

        expect(alertService.alert).toHaveBeenCalledWith(jasmine.objectContaining({
          status: 500,
          data: 'expected error'
        }));
      });

      it('should delete mail', function () {
        httpBackend.flush();

        scope.removeMail(jasmine.createSpyObj('', ['stopPropagation']), aMail);

        httpBackend.expectDELETE('http://localhost:2525/mails/1')
      });

      it('should forward error response to alertService when delete failed', function () {
        httpCallChain.respond(500, 'expected delete error');
        httpBackend.flush();

        scope.removeMail(jasmine.createSpyObj('', ['stopPropagation']), aMail);

        expect(alertService.alert).toHaveBeenCalledWith(jasmine.objectContaining({
          status: 500,
          data: 'expected delete error'
        }));
      });
    });
  });
});
