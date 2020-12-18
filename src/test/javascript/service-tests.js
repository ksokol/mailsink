describe('src/test/javascript/service-tests.js', function () {
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
      const cmd = curlConverter.toCommand('expected url', "expected 'body'");

      expect(cmd).toEqual("curl -X POST 'expected url' -H 'content-type: application/json' -d 'expected '\\''body'\\'");
    });
  });
});
