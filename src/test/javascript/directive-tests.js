describe("messageText directive", function() {

    beforeEach(module("mailsinkApp"));

    var scope;

    beforeEach(inject(function ($compile, $rootScope) {
        scope = $rootScope.$new();
        scope.mail = {
            body: null
        }
    }));

    it("should set empty array when body is null", inject(function ($compile) {
        $compile("<message-text></message-text>")(scope);
        expect(scope.messageText).toEqual([]);
    }));

    it('should return array with element "line"', inject(function ($compile) {
        scope.mail.body = "line";
        $compile("<message-text></message-text>")(scope);
        expect(scope.messageText).toEqual(["line"]);
    }));

    it('should return array with elements "first line" and "second line"', inject(function ($compile) {
        scope.mail.body = "first line\r\nsecond line\r\n";
        $compile("<message-text></message-text>")(scope);
        expect(scope.messageText).toEqual(["first line", "second line", ""]);
    }));

    it("should respect whitespaces in line", inject(function ($compile) {
        scope.mail.body = " a line with whitespaces ";
        $compile("<message-text></message-text>")(scope);
        expect(scope.messageText).toEqual([" a line with whitespaces "]);
    }));
});

describe("alertMessage directive", function() {

    beforeEach(module("mailsinkApp"));

    var scope, rootScope;

    beforeEach(inject(function ($compile, $rootScope) {
        scope = $rootScope.$new();
        rootScope = $rootScope;
    }));

    it('should set "error" as value on scope attribute "message"', inject(function ($compile) {
        $compile("<alert-message>{{message}}</alert-message>")(scope);

        rootScope.$emit("error", "èrror");

        expect(scope.message).toEqual("èrror");
    }));

    it('should hide alert message when no "error" event triggered', inject(function ($compile) {
        var el = $compile('<alert-message class="hidden">{{message}}</alert-message>')(scope);

        scope.$digest();

        expect(el.text()).toBe("");
    }));

    it('should show alert message when "error" event triggered', inject(function ($compile) {
        var el = $compile('<alert-message class="hidden">{{message}}</alert-message>')(scope);

        rootScope.$emit("error", "èrror");
        scope.$digest();

        expect(el.text()).toBe("èrror");
    }));

    it("should hide alert message when dismissed", inject(function ($compile) {
        var el = $compile('<alert-message class="hidden">{{message}}</alert-message>')(scope);

        rootScope.$emit("error", "èrror");
        scope.close();
        scope.$digest();

        expect(el.text()).toBe("");
    }));
});
