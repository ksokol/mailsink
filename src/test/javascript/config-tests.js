describe("errorBroadcastingHttpInterceptor", function() {

    var aUrl = "http://example.com";
    var rootScope, httpBackend, http;

    beforeEach(module("mailsinkApp"));

    beforeEach(inject(function ($rootScope, _$httpBackend_, $http) {
        httpBackend = _$httpBackend_;
        http = $http;
        rootScope = $rootScope;
        spyOn($rootScope, "$broadcast")
    }));

    it("should broadcast error from plain error message", function() {
        httpBackend.when("GET", aUrl).respond(500, "plain string error message");
        http.get(aUrl);
        httpBackend.flush();

        expect(rootScope.$broadcast).toHaveBeenCalledWith("error", "plain string error message");
    });

    it("should broadcast error message from error message object", function() {
        httpBackend.when("GET", aUrl).respond(500, { message: "error message object" });
        http.get("http://example.com");
        httpBackend.flush();

        expect(rootScope.$broadcast).toHaveBeenCalledWith("error", "error message object");
    });

    it("should not broadcast error message when status code is lower than 500", function() {
        httpBackend.when("GET", aUrl).respond(401);
        http.get("http://example.com");
        httpBackend.flush();

        expect(rootScope.$broadcast).not.toHaveBeenCalled();
    });
});

describe("$sanitize", function() {

    var sanitize;

    beforeEach(module("mailsinkApp"));

    beforeEach(inject(function($sanitize) {
        sanitize = $sanitize;
    }));

    it("should allow data uris to render", function() {
        var element = sanitize('<a href="data:application/pdf;base64,"></a>');
        expect(element).toEqual('<a href="data:application/pdf;base64,"></a>');
    });

    it("should allow http urls to render", function() {
        var element = sanitize('<a href="http://localhost"></a>');
        expect(element).toEqual('<a href="http://localhost"></a>');
    });

    it("should allow https urls to render", function() {
        var element = sanitize('<a href="https://localhost"></a>');
        expect(element).toEqual('<a href="https://localhost"></a>');
    });
});
