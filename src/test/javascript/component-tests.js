describe('Component: Attachments', function() {

    var attachments = [{
        'filename' : 'some-filename',
        'mimeType' : 'some/mimeType',
        'data' : 'some-data'
    }];

    var scope, element;

    beforeEach(module('mailsinkApp', 'htmlTemplates'));

    beforeEach(inject(function($compile, $rootScope, $httpBackend) {
        scope = $rootScope.$new();

        scope.mail = {
            attachments: attachments
        };

        $httpBackend.whenGET('attachments-panel.html').respond('attachments-panel.html');
        element = $compile('<attachments-panel attachments="mail.attachments"></attachments-panel>')(scope);
        scope.$digest();
    }));

    it('should hide panel when no attachments available', function () {
        scope.mail = {
            attachments: []
        };
        scope.$apply();

        expect(element.find('panel').length).toBe(0);
    });

    it('should show panel when attachment', function () {
        var a = element.find('a');
        expect(a.attr('target')).toBe('_blank');
        expect(a.attr('title')).toBe('some-filename (some/mimeType)');
        expect(a.attr('href')).toBe('data:some/mimeType;base64,some-data');
        expect(a.text()).toBe(' some-filename');
    });

    it('should show panel with to attachments when attachments available', function () {
        scope.mail = {
            attachments: [{},{}]
        };
        scope.$apply();

        expect(element.find('a').length).toBe(2);
    });
});

describe('Component: mailBodyPanel', function() {

    var scope, element;

    beforeEach(module('mailsinkApp', 'htmlTemplates'));

    beforeEach(inject(function($compile, $rootScope, $httpBackend) {
        scope = $rootScope.$new();
        scope.mail = {};

        $httpBackend.whenGET('mail-body-panel.html').respond('mail-body-panel.html');
        element = $compile('<mail-body-panel mail="mail"></mail-body-panel>')(scope);
    }));

    it('should have no mail content', function () {
        expect(element.find('li').length).toBe(0);
    });

    it('should have text content', function () {
        scope.mail = {
            text: 'text'
        };
        scope.$digest();

        expect(element.find('li').attr('class')).toContain('plain');
    });

    it('should have attachments', function () {
        scope.mail = {
            attachments: [{}]
        };
        scope.$digest();

        expect(element.find('li').attr('class')).toContain('attachments');
    });

    it('should have text content and attachments', function () {
        scope.mail = {
            text: 'text',
            attachments: [{}]
        };
        scope.$digest();

        expect(element.find('li').length).toBe(2);
        expect(element.find('li')[0].classList).toContain('plain');
        expect(element.find('li')[1].classList).toContain('attachments');
    });
});
