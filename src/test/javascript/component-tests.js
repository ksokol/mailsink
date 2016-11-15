describe("Component: Attachments", function() {

    var attachments = [{
        "filename" : "some-filename",
        "mimeType" : "some/mimeType",
        "data" : "some-data"
    }];

    var scope, element;

    beforeEach(module('mailsinkApp', 'htmlTemplates'));

    beforeEach(inject(function($compile, $rootScope, $httpBackend) {
        scope = $rootScope.$new();

        scope.mail = {
            attachments: attachments
        };

        $httpBackend.whenGET('attachments-panel.html').respond("attachments-panel.html")
        element = $compile('<attachments-panel attachments="mail.attachments"></attachments-panel>')(scope);
        scope.$digest();
    }));

    it("should hide panel when no attachments available", function () {
        scope.mail = {
            attachments: []
        };
        scope.$apply();

        expect(element.find('panel').length).toBe(0);
    });

    it("should show panel when attachment", function () {
        var a = element.find('a');
        expect(a.attr('target')).toBe('_blank');
        expect(a.attr('title')).toBe('some-filename (some/mimeType)');
        expect(a.attr('href')).toBe('data:some/mimeType;base64,some-data');
        expect(a.text()).toBe(' some-filename');
    });

    it("should show panel with to attachments when attachments available", function () {
        scope.mail = {
            attachments: [{},{}]
        };
        scope.$apply();

        expect(element.find('a').length).toBe(2);
    });
});
