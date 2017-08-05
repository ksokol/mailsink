module.exports = function(config) {
    config.set({
        files: [
            'target/dependency/META-INF/resources/webjars/jquery/**/jquery.js',
            'target/dependency/META-INF/resources/webjars/angular/**/angular.js',
            'target/dependency/META-INF/resources/webjars/angular-sanitize/**/angular-sanitize.js',
            'target/dependency/META-INF/resources/webjars/angular-mocks/**/angular-mocks.js',
            'target/dependency/META-INF/resources/webjars/angular-ui-bootstrap-bower/2.2.0/ui-bootstrap-tpls.min.js',
            'target/dependency/META-INF/resources/webjars/angular-ui-bootstrap-bower/2.2.0/ui-bootstrap.min.js',
            'target/dependency/META-INF/resources/webjars/stomp-websocket/**/lib/stomp.js',
            'target/dependency/META-INF/resources/webjars/angular-scroll-glue/**/src/scrollglue.js',
            'target/dependency/META-INF/resources/webjars/CBuffer/**/cbuffer.js',
            'target/dependency/META-INF/resources/webjars/angular-clipboard/**/angular-clipboard.js',
            'target/dependency/META-INF/resources/webjars/highlightjs/**/highlight.pack.min.js',
            'target/dependency/META-INF/resources/webjars/angular-highlightjs/**/build/angular-highlightjs.min.js',
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
            type : 'lcov',
            dir : 'target/karma-reports/coverage/'
        },
        sonarQubeUnitReporter: {
            sonarQubeVersion: 'LATEST',
            outputFile: 'target/karma-reports/TEST.xml',
            useBrowserName: false
        },
        frameworks: ['jasmine'],
        reporters: ['progress', 'coverage', 'sonarqubeUnit'],
        port: 9876,
        colors: false,
        logLevel: config.LOG_WARN,
        autoWatch: true,
        browsers: ['PhantomJS2'],
        captureTimeout: 60000,
        singleRun: true
    });
};
