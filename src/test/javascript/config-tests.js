describe("errorBroadcastingHttpInterceptor", function() {

    var aUrl = 'http://example.com';
    var rootScope, httpBackend, http;

    beforeEach(module('mailsinkApp', function($httpProvider) {
        httpProviderIt = $httpProvider;
    }));

    beforeEach(inject(function ($rootScope, _$httpBackend_, $http) {
        httpBackend = _$httpBackend_;
        http = $http;
        rootScope = $rootScope;
        spyOn($rootScope, '$broadcast')
    }));

    it('should broadcast error from plain error message', function() {
        httpBackend.when('GET', aUrl).respond(500, 'plain string error message');
        http.get(aUrl);
        httpBackend.flush();

        expect(rootScope.$broadcast).toHaveBeenCalledWith('error', 'plain string error message')
    });

    it('should broadcast error message from error message object', function() {
        httpBackend.when('GET', aUrl).respond(500, { message: 'error message object' });
        http.get('http://example.com');
        httpBackend.flush();

        expect(rootScope.$broadcast).toHaveBeenCalledWith('error', 'error message object')
    });

    it('should not broadcast error message when status code is lower than 500', function() {
        httpBackend.when('GET', aUrl).respond(401);
        http.get('http://example.com');
        httpBackend.flush();

        expect(rootScope.$broadcast).not.toHaveBeenCalled();
    });
});
