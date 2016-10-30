module.exports = function(config) {
    config.set({
        files: [
            'target/dependency/META-INF/resources/webjars/angular/**/angular.js',
            'target/dependency/META-INF/resources/webjars/angular-sanitize/**/angular-sanitize.js',
            'target/dependency/META-INF/resources/webjars/angular-mocks/**/angular-mocks.js',
            'target/dependency/META-INF/resources/webjars/angular-ui-bootstrap-bower/2.2.0/ui-bootstrap.min.js',
            'src/main/resources/static/app.js',
            'src/test/javascript/*.js'
        ],
        preprocessors: {
            'src/main/resources/static/app.js': ['coverage']
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
