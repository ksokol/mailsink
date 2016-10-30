describe("urlToLink filter", function() {
    var urlToLink;

    beforeEach(module('mailsinkApp'));

    beforeEach(inject(function ($filter) {
        urlToLink = $filter('urlToLink');
    }));

    it('should return empty string when text is null or undefined"', function () {
        expect(urlToLink(null)).toBe('');
        expect(urlToLink(undefined)).toBe('');
    });

    it('should return http URL as <a> tag with attribute target nd value "_blank"', function () {
        expect(urlToLink('http://example.com')).toBe('<a href="http://example.com" target="_blank">http://example.com</a>');
    });

    it('should return https URL as <a> tag with attribute target and value "_blank"', function () {
        expect(urlToLink('https://example.com')).toBe('<a href="https://example.com" target="_blank">https://example.com</a>');
    });

    it('should return URLs as <a> tags with attribute target and value "_blank"', function () {
        expect(urlToLink('first url http://example1.com and second url http://example2.com'))
            .toBe('first url <a href="http://example1.com" target="_blank">http://example1.com</a> and second url <a href="http://example2.com" target="_blank">http://example2.com</a>');
    });

    it('should not modify given text without URL"', function () {
        expect(urlToLink('text without an URL')).toBe('text without an URL');
    });
});
