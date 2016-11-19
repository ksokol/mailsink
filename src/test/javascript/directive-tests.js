describe('messageText directive', function() {

    var scope;

    beforeEach(module('mailsinkApp'));

    beforeEach(inject(function ($compile, $rootScope) {
        scope = $rootScope.$new();
    }));

    it('should set empty array when body is null', inject(function ($compile) {
        scope.text = null;
        var element = $compile('<message-text text="{{text}}"></message-text>')(scope);
        scope.$digest();

        expect(element.isolateScope().messageText).toEqual([]);
    }));

    it('should return array with element "line"', inject(function ($compile) {
        scope.text = 'line';
        var element = $compile('<message-text text="{{text}}"></message-text>')(scope);
        scope.$digest();

        expect(element.isolateScope().messageText).toEqual(['line']);
    }));

    it('should return array with elements "first line" and "second line"', inject(function ($compile) {
        scope.text = 'first line\r\nsecond line\r\n';
        var element = $compile('<message-text text="{{text}}"></message-text>')(scope);
        scope.$digest();

        expect(element.isolateScope().messageText).toEqual(['first line', 'second line', '']);
    }));

    it('should respect whitespaces in line', inject(function ($compile) {
        scope.text = ' a line with whitespaces ';
        var element = $compile('<message-text text="{{text}}"></message-text>')(scope);
        scope.$digest();

        expect(element.isolateScope().messageText).toEqual([' a line with whitespaces ']);
    }));

    it('should render text lines in right order', inject(function ($compile) {
        scope.text = 'first line\r\nsecond line\r\n';
        var element = $compile('<message-text text="{{text}}"></message-text>')(scope);
        scope.$digest();

        var selector = 'span';

        expect(element.find(selector)[0].innerText).toBe('first line');
        expect(element.find(selector)[1].innerText).toBe('second line');
        expect(element.find(selector)[2].innerText).toBe('');
    }));
});

describe('alertMessage directive', function() {

    beforeEach(module('mailsinkApp'));

    var scope, rootScope;

    beforeEach(inject(function ($compile, $rootScope) {
        scope = $rootScope.$new();
        rootScope = $rootScope;
    }));

    it('should set error as value on scope attribute message', inject(function ($compile) {
        $compile('<alert-message>{{message}}</alert-message>')(scope);

        rootScope.$emit('error', 'error');

        expect(scope.message).toEqual('error');
    }));

    it('should hide alert message when no error event triggered', inject(function ($compile) {
        var el = $compile('<alert-message class="hidden">{{message}}</alert-message>')(scope);

        scope.$digest();

        expect(el.text()).toBe('');
    }));

    it('should show alert message when error event triggered', inject(function ($compile) {
        var el = $compile('<alert-message class="hidden">{{message}}</alert-message>')(scope);

        rootScope.$emit('error', 'error');
        scope.$digest();

        expect(el.text()).toBe('error');
    }));

    it('should hide alert message when dismissed', inject(function ($compile) {
        var el = $compile('<alert-message class="hidden">{{message}}</alert-message>')(scope);

        rootScope.$emit('error', 'èrror');
        scope.close();
        scope.$digest();

        expect(el.text()).toBe('');
    }));
});
