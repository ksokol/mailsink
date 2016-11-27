module.exports = function(config) {
    config.set({
        files: [
            'target/dependency/META-INF/resources/webjars/jquery/**/jquery.js',
            'target/dependency/META-INF/resources/webjars/angular/**/angular.js',
            'target/dependency/META-INF/resources/webjars/angular-sanitize/**/angular-sanitize.js',
            'target/dependency/META-INF/resources/webjars/angular-mocks/**/angular-mocks.js',
            'target/dependency/META-INF/resources/webjars/angular-ui-bootstrap-bower/2.2.0/ui-bootstrap-tpls.min.js',
            'target/dependency/META-INF/resources/webjars/angular-ui-bootstrap-bower/2.2.0/ui-bootstrap.min.js',
            'target/dependency/META-INF/resources/webjars/ng-stomp/0.3.0/dist/ng-stomp.standalone.min.js',
            'target/dependency/META-INF/resources/webjars/angular-scroll-glue/2.0.6/src/scrollglue.js',
            'target/dependency/META-INF/resources/webjars/CBuffer/1.1.1/cbuffer.js',
            'src/main/resources/static/app.js',
            'src/test/javascript/*.js',
            'src/main/resources/static/*.html'
        ],
        preprocessors: {
            'src/main/resources/static/app.js': ['coverage'],
            '**/*.html': ['ng-html2js']
        },
        ngHtml2JsPreprocessor: {
            stripPrefix: 'src/main/resources/static/',
            moduleName: 'htmlTemplates'
        },
        coverageReporter: {
            type : 'html',
            dir : 'target/coverage/'
        },
        frameworks: ['jasmine'],
        reporters: ['progress', 'coverage'],
        port: 9876,
        colors: false,
        logLevel: config.LOG_WARN,
        autoWatch: true,
        browsers: ['PhantomJS2'],
        captureTimeout: 60000,
        singleRun: true
    });
};
