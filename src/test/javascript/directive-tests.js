describe("messageText directive", function() {

    beforeEach(module('mailsinkApp'));

    var scope;

    beforeEach(inject(function ($compile, $rootScope) {
        scope = $rootScope.$new();
        scope.mail = {
            body: null
        }
    }));

    it("should set empty array when body is null", inject(function ($compile) {
        $compile('<message-text></message-text>')(scope);
        expect(scope.messageText).toEqual([]);
    }));

    it("should return array with element 'line'", inject(function ($compile) {
        scope.mail.body = "line";
        $compile('<message-text></message-text>')(scope);
        expect(scope.messageText).toEqual(['line']);
    }));

    it("should return array with elements 'first line' and 'second line'", inject(function ($compile) {
        scope.mail.body = "first line\r\nsecond line\r\n";
        $compile('<message-text></message-text>')(scope);
        expect(scope.messageText).toEqual(['first line', 'second line']);
    }));

    it("should respect whitespaces in line", inject(function ($compile) {
        scope.mail.body = " a line with whitespaces ";
        $compile('<message-text></message-text>')(scope);
        expect(scope.messageText).toEqual([' a line with whitespaces ']);
    }));
});
