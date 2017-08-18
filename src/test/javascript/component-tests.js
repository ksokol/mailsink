/*
 * https://velesin.io/2016/08/23/unit-testing-angular-1-5-components/
 */
function componentMock(name) {
    function _componentMock($provide) {
        _componentMock.bindings = {};

        $provide.decorator(name + 'Directive', function ($delegate) {
            var component = $delegate[0];
            component.template = '';
            component.templateUrl = '';
            component.controller = function () {
                _componentMock.bindings = this;
            };

            return $delegate;
        });
    }

    return _componentMock;
}

function mock(name) {
    function _mock($provide) {
        $provide.value(name, {});
    }

    return _mock;
}

describe('src/test/javascript/component-tests.js', function () {
    describe('Component: Attachments', function () {

        var response = {
            _embedded: {
                mailAttachments: [{
                    content: {
                        filename: 'some-filename1',
                        mimeType: 'some/mimeType1',
                        data: 'some-data1'
                    },
                    _links: {
                        download: {
                            href: 'http://localhost/mailAttachments/0/download'
                        }
                    }
                }, {
                    content: {
                        filename: 'some-filename2',
                        mimeType: 'some/mimeType2',
                        data: 'some-data2'
                    },
                    _links: {
                        download: {
                            href: 'http://localhost/mailAttachments/1/download'
                        }
                    }
                }]
            }
        };

        var scope, element, httpBackend;

        beforeEach(module('mailsinkApp', 'htmlTemplates'));

        beforeEach(inject(function ($compile, $rootScope, $httpBackend) {
            scope = $rootScope.$new();
            httpBackend = $httpBackend;

            scope.mail = {
                _links: {
                    attachments: {
                        href: 'http://localhost/mailAttachments/42'
                    }
                }
            };

            httpBackend.when('GET', 'http://localhost/mailAttachments/42').respond(200, response);
            element = $compile('<attachments-panel mail="mail"></attachments-panel>')(scope);
            httpBackend.flush();
            scope.$digest();
        }));

        it('should hide panel when no attachments available', function () {
            scope.mail = {
                attachments: []
            };
            scope.$apply();

            expect(element.find('panel').length).toBe(0);
        });

        it('should show panel with attachments when initialized', function () {
            var aTags = element.find('a');
            expect(aTags.length).toBe(2);

            var attachment1 = angular.element(element.find('a')[0]);
            var attachment2 = angular.element(element.find('a')[1]);

            expect(attachment1.attr('target')).toBe('_blank');
            expect(attachment1.attr('title')).toBe('some-filename1 (some/mimeType1)');
            expect(attachment1.attr('href')).toBe('http://localhost/mailAttachments/0/download');
            expect(attachment1.text()).toBe(' some-filename1');

            expect(attachment2.attr('target')).toBe('_blank');
            expect(attachment2.attr('title')).toBe('some-filename2 (some/mimeType2)');
            expect(attachment2.attr('href')).toBe('http://localhost/mailAttachments/1/download');
            expect(attachment2.text()).toBe(' some-filename2');
        });

        it('should show error message when could not fetch attachments', inject(function ($compile) {
            scope.mail = {
                _links: {
                    attachments: {
                        href: 'irrelevant'
                    }
                }
            };

            httpBackend.when('GET', 'irrelevant').respond(500, {});
            element = $compile('<attachments-panel mail="mail"></attachments-panel>')(scope);
            httpBackend.flush();
            scope.$digest();

            expect(element.find('table')[0]).toBeUndefined();
            expect(element.find('div').text()).toContain('Warning! Could not fetch attachments.');
        }));
    });

    describe('Component: mailBodyPanel', function () {

        var scope, element;
        var messageHtml= componentMock('messageHtml');
        var messageHtmlQuery = componentMock('messageHtmlQuery');

        beforeEach(angular.mock.module('mailsinkApp', 'htmlTemplates', messageHtml, messageHtmlQuery));

        beforeEach(inject(function ($compile, $rootScope, $httpBackend) {
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
                content: {
                    text: 'text'
                }
            };
            scope.$digest();

            expect(element.find('li').attr('class')).toContain('plain');
        });

        it('should have attachments', function () {
            scope.mail = {
                content: {
                    attachments: true
                },
                _links: {
                    attachments: {
                        href: ''
                    }
                }
            };
            scope.$digest();

            expect(element.find('li').attr('class')).toContain('attachments');
        });

        it('should have html content', function () {
            scope.mail = {
                content: {
                    html: 'html'
                }
            };
            scope.$digest();

            expect(element.find('li').attr('class')).toContain('html');
        });

        it('should have text content, html content and attachments', function () {
            scope.mail = {
                content: {
                    text: 'text',
                    html: 'html',
                    attachments: true
                },
                _links: {
                    attachments: {
                        href: ''
                    }
                }
            };
            scope.$digest();

            expect(element.find('li').length).toBe(3);
            expect(element.find('li')[0].classList).toContain('plain');
            expect(element.find('li')[1].classList).toContain('html');
            expect(element.find('li')[2].classList).toContain('attachments');
        });

        it('should show html body and "Query HTML" button', function () {
            scope.mail = {content: {html: 'html'}};
            scope.$digest();

            expect(element.find('button').length).toEqual(1);
            expect(element.find('button')[0].innerText).toEqual('Query HTML');
            expect(element.find('message-html').length).toEqual(1);
            expect(element.find('message-html-query').length).toEqual(0);
        });

        it('should show query panel and "back to HTML" button', function () {
            scope.mail = {content: {html: 'html'}};
            scope.$digest();
            element.find('button').triggerHandler('click');
            scope.$digest();

            expect(element.find('button').length).toEqual(1);
            expect(element.find('button')[0].innerText).toEqual('back to HTML');
            expect(element.find('message-html').length).toEqual(0);
            expect(element.find('message-html-query').length).toEqual(1);
        });

        it('should pass mail to child components', function () {
            scope.mail = {content: {html: 'html'}};
            scope.$digest();
            element.find('button').triggerHandler('click');
            scope.$digest();

            expect(messageHtml.bindings.mail).toEqual(scope.mail);
            expect(messageHtmlQuery.bindings.mail).toEqual(scope.mail);
        });
    });

    describe('Component: messageHtml', function () {

        beforeEach(module('mailsinkApp'));

        it('should add HTML Body into shadow root element', inject(function ($componentController) {
            var el = jasmine.createSpyObj('element', ['attachShadow']);

            var shadowDOM = {innerHTML: null};
            el.attachShadow.and.returnValue(shadowDOM);
            $componentController('messageHtml', {$element: [el]}, {mail: {content: {html: 'expected html body'}}}).$onInit();

            expect(el.attachShadow).toHaveBeenCalledWith({mode: 'open'});
            expect(shadowDOM.innerHTML).toEqual('expected html body');
        }));
    });

    describe('Component: messageSource', function () {

        var scope, element;

        beforeEach(module('mailsinkApp'));

        beforeEach(inject(function ($compile, $rootScope) {
            scope = $rootScope.$new();
            scope.mail = {_links: {source: {href: 'http://localhost/mails/42/source'}}};

            element = $compile('<message-source mail="mail"></message-source>')(scope);
            scope.$digest();
        }));

        it('should build href attribute for mail source', function () {
            expect(element.find('a').attr('href')).toBe('http://localhost/mails/42/source');
        });

        it('should open mail source in new tab', function () {
            expect(element.find('a').attr('target')).toBe('_blank');
        });
    });

    describe('Component: smtpLog', function () {

        var scope, element, httpBackend;
        var stompService = {};

        var push = function (data) {
            stompService.subscribe.calls.argsFor(0)[1](data);
        };

        beforeEach(module('mailsinkApp', 'htmlTemplates'));

        beforeEach(inject(function ($compile, $rootScope, $httpBackend, _stompService_) {
            scope = $rootScope.$new();
            httpBackend = $httpBackend;

            stompService = _stompService_;
            spyOn(stompService, 'subscribe');

            httpBackend.whenGET('smtp-log.html').respond('smtp-log.html');
            element = $compile('<smtp-log></smtp-log>')(scope);
            scope.$digest();
        }));

        it('should initialize empty buffer', function () {
            expect(element.isolateScope().emptyLogs()).toEqual(true);
            expect(element.isolateScope().logs()).toEqual([]);
        });

        it('should show smtp log event', function () {
            push({number: 1, line: 'line1', time: 'time1'});
            scope.$digest();

            expect(element.find('.intellij-default-line-number-text')[0].innerText).toEqual('1');
            expect(element.find('.intellij-default-todo-text')[0].innerText).toEqual('time1');
            expect(element.find('.intellij-default-text')[0].innerText).toEqual('line1');
        });

        it('should show multiple log events', function () {
            push('expected data1');
            push('expected data2');

            expect(element.isolateScope().emptyLogs()).toEqual(false);
            expect(element.isolateScope().logs()).toEqual(['expected data1', 'expected data2']);
        });

        it('should recycle buffer when more than fifty log events received', function () {
            for (var i = 0; i <= 51; i++) {
                push(i);
            }

            var expected = [];
            for (var j = 2; j <= 51; j++) {
                expected.push(j);
            }

            expect(element.isolateScope().logs()).toEqual(expected);
        });

        it('should connect to proper broker and subscribe to proper topic', function () {
            expect(stompService.subscribe).toHaveBeenCalledWith('smtp-log', jasmine.any(Function));
        });

        it('should scroll to bottom when new smtp log event received', function () {
            expect(element.find('.smtp-log')[0].hasAttribute('scroll-glue')).toBe(true);
        });
    });

    describe('Component: messageHtmlQuery', function () {

        var scope, element, httpBackend, compile, clipboard, curlConverter;

        var mail = {
            content: {
                id: 1,
                html: 'html body'
            },
            _links: {
                query: {
                    href: 'http://localhost/mails/1/html/query'
                }
            }
        };

        beforeEach(angular.mock.module('mailsinkApp', 'htmlTemplates', mock('curlConverter')));

        beforeEach(inject(function ($compile, $rootScope, $httpBackend, _clipboard_, _curlConverter_) {
            scope = $rootScope.$new();
            httpBackend = $httpBackend;
            compile = $compile;
            curlConverter = _curlConverter_;
            clipboard = _clipboard_;

            curlConverter['toCommand'] = jasmine.createSpy('curlConverter.toCommand()');
            scope.mail = mail;
        }));

        afterEach(function () {
            httpBackend.verifyNoOutstandingExpectation();
            httpBackend.verifyNoOutstandingRequest();
        });

        describe('failing request on initialization', function () {

            beforeEach(function () {
                httpBackend.when('POST', 'http://localhost/mails/1/html/query', {xpath: '*'}).respond(502, {message: 'expected error'});
                element = compile('<message-html-query mail="mail"></message-html-query>')(scope);
                httpBackend.flush();
                scope.$digest();
            });

            it('should show error message', function () {
                expect(element.children()[2].classList).toContain('alert-danger');
            });
        });

        describe('passing request on initialization', function () {

            beforeEach(function () {
                httpBackend.when('POST', 'http://localhost/mails/1/html/query', {xpath: '*'}).respond(200, {value: 'expected response'});
                element = compile('<message-html-query mail="mail"></message-html-query>')(scope);
                httpBackend.flush();
                scope.$digest();
            });

            it('should pretty print JSON response with hljs', function (done) {
                setTimeout(function () {
                    expect(element.find('code')[0].classList[0]).toEqual('hljs');
                    expect(element.find('code')[0].classList[1]).toEqual('json');
                    expect(element.find('code')[0].innerText).toEqual('{\n "value": "expected response"\n}');
                    done()
                }, 20);
            });

            it('should pretty print HTML body with hljs when clicked on button', function (done) {
                element.find('a')[1].click();

                setTimeout(function () {
                    expect(element.find('code')[0].classList[0]).toEqual('hljs');
                    expect(element.find('code')[0].classList[1]).toEqual('stylus');
                    expect(element.find('code')[0].innerText).toEqual('html body');
                    done()
                }, 20);
            });

            it('should change label of button when switching to HTML source view', function () {
                expect(element.find('a')[1].innerText).toEqual('show HTML source');
                element.find('a')[1].click();
                expect(element.find('a')[1].innerText).toEqual('show xpath result');
                element.find('a')[1].click();
                expect(element.find('a')[1].innerText).toEqual('show HTML source');
            });

            it('should query html body with given xpath expression when query button pressed', function (done) {
                httpBackend.when('POST', 'http://localhost/mails/1/html/query', {xpath: '//a'}).respond(200, {value: 'expected response2'});

                element.find('input').val('//a').triggerHandler('input');
                element.find('button').triggerHandler('click');
                httpBackend.flush();

                setTimeout(function () {
                    expect(element.find('code')[0].innerText).toEqual('{\n "value": "expected response2"\n}');
                    done();
                }, 20);
            });

            it('should query html body with given wildcard xpath expression when query button pressed', function (done) {
                element.find('input').val('').triggerHandler('input');
                element.find('button').triggerHandler('click');
                httpBackend.flush();

                setTimeout(function () {
                    expect(element.find('code')[0].innerText).toEqual('{\n "value": "expected response"\n}');
                    done();
                }, 20);
            });

            it('should show warning message when xpath expression is invalid', function () {
                httpBackend.when('POST', 'http://localhost/mails/1/html/query', {xpath: 'invalid'}).respond(400, {message: 'expected error'});
                element.find('input').val('invalid').triggerHandler('input');
                element.find('button').triggerHandler('click');
                httpBackend.flush();

                expect(element.children()[2].classList).toContain('alert-warning');
                expect(element.children()[2].innerText).toContain('Warning! expected error');
            });

            it('should show error message when request failed', function () {
                httpBackend.when('POST', 'http://localhost/mails/1/html/query', {xpath: 'invalid'}).respond(500, {message: 'expected error'});
                element.find('input').val('invalid').triggerHandler('input');
                element.find('button').triggerHandler('click');
                httpBackend.flush();

                expect(element.children()[2].classList).toContain('alert-danger');
                expect(element.children()[2].innerText).toContain('Warning! expected error');
            });

            it('should switch from HTML source view to result view when query request finished', function (done) {
                element.find('a')[1].click();

                setTimeout(function () {
                    expect(element.find('code')[0].classList[1]).toEqual('stylus');

                    element.find('button').triggerHandler('click');
                    httpBackend.flush();

                    setTimeout(function () {
                        expect(element.find('code')[0].classList[1]).toEqual('json');
                        done();
                    }, 20);
                }, 20);
            });

            it('should convert query request to cURL command', function () {
                spyOn(clipboard, 'copyText');

                element.find('a')[0].click();
                var url = 'http://localhost/mails/1/html/query';

                expect(curlConverter.toCommand).toHaveBeenCalledWith(url, '{"xpath":"*"}');
            });

            it('should copy cURL command to clipboard', function () {
                spyOn(document, 'execCommand').and.returnValue(true);
                spyOn(clipboard, 'copyText');
                curlConverter.toCommand.and.returnValue('expected curl command');

                element.find('a')[0].click();

                expect(clipboard.copyText).toHaveBeenCalledWith('expected curl command');
            });

            it('should open dropdown menu with ui.bootstrap.dropdown', function () {
                expect(element.find('ul').parent()[0].classList).not.toContain('open');

                element.find('button')[1].click();
                expect(element.find('ul').parent()[0].classList).toContain('open');
            });
        });
    });
});
