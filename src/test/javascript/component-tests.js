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

    it('should have html content', function () {
        scope.mail = {
            html: 'html'
        };
        scope.$digest();

        expect(element.find('li').attr('class')).toContain('html');
    });

    it('should have text content, html content and attachments', function () {
        scope.mail = {
            text: 'text',
            html: 'html',
            attachments: [{}]
        };
        scope.$digest();

        expect(element.find('li').length).toBe(3);
        expect(element.find('li')[0].classList).toContain('plain');
        expect(element.find('li')[1].classList).toContain('html');
        expect(element.find('li')[2].classList).toContain('attachments');
    });
});

describe('Component: messageHtml', function() {

    var scope, element;

    beforeEach(module('mailsinkApp', 'htmlTemplates'));

    beforeEach(inject(function($compile, $rootScope, $httpBackend) {
        scope = $rootScope.$new();

        $httpBackend.whenGET('message-html.html').respond('message-html.html');
        element = $compile('<message-html id="42"></message-html>')(scope);
    }));

    it('should build src attribute for iframe', function () {
        scope.$digest();

        expect(element.find('iframe').attr('src')).toBe('mails/42/html');
    });

    it('should hide frame border', function () {
        expect(element.find('iframe').attr('frameborder')).toBe('0');
    });

});

describe('Component: messageHtml', function () {

    var $componentController;

    beforeEach(module('mailsinkApp'));

    beforeEach(inject(function(_$componentController_) {
        $componentController = _$componentController_;
    }));

    it('should subscribe to load function on iframe', function() {
        var el = {
            find: {}
        };

        var iframe = {
            on: jasmine.createSpy('iframe on function')
        };

        spyOn(el, 'find').and.returnValue(iframe);
        $componentController('messageHtml', {$element: el}, null);

        expect(el.find).toHaveBeenCalledWith('iframe');
        expect(iframe.on).toHaveBeenCalledWith('load', jasmine.any(Function));
    });

    it('should resize and show iframe after load', function() {
        var loadCallback;

        var iframe = {
            on: function(type, fn) {
                loadCallback = fn;
            },
            0: {
                contentWindow: {
                    document: {
                        body: {
                            scrollHeight: 200
                        }
                    }
                }
            },
            css: jasmine.createSpy(),
            removeClass: jasmine.createSpy()
        };

        var el = {
            find: function() {
                return iframe;
            }
        };

        $componentController('messageHtml', {$element: el}, null);
        loadCallback();

        expect(iframe.css.calls.allArgs()).toEqual([['width', '100%'], ['height', '200px']]);
        expect(iframe.removeClass).toHaveBeenCalledWith('hidden');
    });
});

describe('Component: messageSource', function() {

    var scope, element;

    beforeEach(module('mailsinkApp'));

    beforeEach(inject(function($compile, $rootScope) {
        scope = $rootScope.$new();
        element = $compile('<message-source id="42"></message-source>')(scope);
        scope.$digest();
    }));

    it('should build href attribute for mail source', function () {
        expect(element.find('a').attr('href')).toBe('mails/42/source');
    });

    it('should open mail source in new tab', function () {
        expect(element.find('a').attr('target')).toBe('_blank');
    });
});

describe('Component: smtpLog', function() {

    var scope, element, stomp, httpBackend;

    beforeEach(module('mailsinkApp', 'htmlTemplates', 'mockNgStomp'));

    beforeEach(inject(function($compile, $rootScope, $httpBackend, $stomp) {
        scope = $rootScope.$new();
        stomp = $stomp;
        httpBackend = $httpBackend;

        httpBackend.whenGET('smtp-log.html').respond('smtp-log.html');
        element = $compile('<smtp-log></smtp-log>')(scope);
        scope.$digest();
    }));

    it('should initialize empty buffer', function () {
        expect(element.isolateScope().emptyLogs()).toEqual(true);
        expect(element.isolateScope().logs()).toEqual([]);
    });

    it('should show smtp log event', function () {
        stomp.push({ number: 1, line: 'line1', time: 'time1'});
        scope.$digest();

        expect(element.find('.intellij-default-line-number-text')[0].innerText).toEqual('1');
        expect(element.find('.intellij-default-todo-text')[0].innerText).toEqual('time1');
        expect(element.find('.intellij-default-text')[0].innerText).toEqual('line1');
    });

    it('should show multiple log events', function () {
        stomp.push('expected data1');
        stomp.push('expected data2');

        expect(element.isolateScope().emptyLogs()).toEqual(false);
        expect(element.isolateScope().logs()).toEqual(['expected data1', 'expected data2']);
    });

    it('should recycle buffer when more than fifty log events received', function () {
        for(var i=0;i<=51;i++) {
            stomp.push(i);
        }

        var expected = [];
        for(var j=2;j<=51;j++) {
            expected.push(j);
        }

        expect(element.isolateScope().logs()).toEqual(expected);
    });

    it('should connect to proper broker and subscribe to proper topic', function() {
        expect(stomp.broker()).toBe('/ws');
        expect(stomp.topic()).toBe('/topic/smtp-log');
    });

    it('should scroll to bottom when new smtp log event received', function() {
        expect(element.find('.smtp-log')[0].hasAttribute('scroll-glue')).toBe(true);
    });
});
