// Karma configuration
// http://karma-runner.github.io/0.12/config/configuration-file.html
// Generated on 2016-03-09 using
// generator-karma 1.0.0

module.exports = function(config) {
  'use strict';

  config.set({
    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,

    // base path, that will be used to resolve files and exclude
    basePath: '../',

    // testing framework to use (jasmine/mocha/qunit/...)
    // as well as any additional frameworks (requirejs/chai/sinon/...)
    frameworks: [
      "jasmine"
    ],

    // list of files / patterns to load in the browser
    files: [
      // bower:js
      'bower_components/jquery/dist/jquery.js',
      'bower_components/angular/angular.js',
      'bower_components/angular-animate/angular-animate.js',
      'bower_components/angular-cookies/angular-cookies.js',
      'bower_components/angular-resource/angular-resource.js',
      'bower_components/angular-route/angular-route.js',
      'bower_components/angular-sanitize/angular-sanitize.js',
      'bower_components/angular-touch/angular-touch.js',
      'bower_components/semantic/dist/semantic.js',
      'bower_components/angularjs-datepicker/dist/angular-datepicker.min.js',
      'bower_components/ng-dialog/js/ngDialog.js',
      'bower_components/angular-carousel/dist/angular-carousel.js',
      'bower_components/angular-deferred-bootstrap/angular-deferred-bootstrap.js',
      'bower_components/ng-lodash/build/ng-lodash.js',
      'bower_components/angular-simple-logger/dist/angular-simple-logger.js',
      'bower_components/lodash/lodash.js',
      'bower_components/angular-google-maps/dist/angular-google-maps.js',
      'bower_components/angular-ui-router/release/angular-ui-router.js',
      'bower_components/rangy/rangy-core.js',
      'bower_components/rangy/rangy-classapplier.js',
      'bower_components/rangy/rangy-highlighter.js',
      'bower_components/rangy/rangy-selectionsaverestore.js',
      'bower_components/rangy/rangy-serializer.js',
      'bower_components/rangy/rangy-textrange.js',
      'bower_components/textAngular/dist/textAngular.js',
      'bower_components/textAngular/dist/textAngular-sanitize.js',
      'bower_components/textAngular/dist/textAngularSetup.js',
      'bower_components/angucomplete-alt/angucomplete-alt.js',
      'bower_components/trix/dist/trix.js',
      'bower_components/angular-trix/dist/angular-trix.js',
      'bower_components/ngAutocomplete/src/ngAutocomplete.js',
      'bower_components/AngularJS-Toaster/toaster.js',
      'bower_components/angular-toastr/dist/angular-toastr.tpls.js',
      'bower_components/braintree-web/dist/braintree.js',
      'bower_components/ng-file-upload/ng-file-upload.js',
      'bower_components/ng-file-upload-shim/ng-file-upload-shim.js',
      'bower_components/angular-translate/angular-translate.js',
      'bower_components/angular-translate-loader-static-files/angular-translate-loader-static-files.js',
      'bower_components/angular-translate-storage-cookie/angular-translate-storage-cookie.js',
      'bower_components/angular-translate-storage-local/angular-translate-storage-local.js',
      'bower_components/angular-translate-handler-log/angular-translate-handler-log.js',
      'bower_components/angular-dynamic-locale/src/tmhDynamicLocale.js',
      'bower_components/javascript-detect-element-resize/detect-element-resize.js',
      'bower_components/angular-gridster/src/angular-gridster.js',
      'bower_components/moment/moment.js',
      'bower_components/moment-timezone/builds/moment-timezone-with-data-2010-2020.js',
      'bower_components/angular-datepicker/dist/angular-datepicker.js',
      'bower_components/humanize-duration/humanize-duration.js',
      'bower_components/angular-timer/dist/angular-timer.js',
      'bower_components/slick-carousel/slick/slick.min.js',
      'bower_components/slick-lightbox/dist/slick-lightbox.js',
      'bower_components/slick-lightbox/dist/slick-lightbox.min.js',
      'bower_components/owl.carousel/dist/owl.carousel.js',
      'bower_components/jquery-tablesort/jquery.tablesort.js',
      'bower_components/angular-moment/angular-moment.js',
      'bower_components/angular-socialshare/dist/angular-socialshare.min.js',
      'bower_components/ng-disable-scroll/disable-scroll.js',
      'bower_components/angular-ui-grid/ui-grid.js',
      'bower_components/angular-mocks/angular-mocks.js',
      // endbower
      "app/scripts/**/*.js",
      "test/mock/**/*.js",
      "test/spec/**/*.js"
    ],

    // list of files / patterns to exclude
    exclude: [
    ],

    // web server port
    port: 8080,

    // Start these browsers, currently available:
    // - Chrome
    // - ChromeCanary
    // - Firefox
    // - Opera
    // - Safari (only Mac)
    // - PhantomJS
    // - IE (only Windows)
    browsers: [
      "PhantomJS"
    ],

    // Which plugins to enable
    plugins: [
      "karma-phantomjs-launcher",
      "karma-jasmine"
    ],

    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: false,

    colors: true,

    // level of logging
    // possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
    logLevel: config.LOG_INFO,

    // Uncomment the following lines if you are using grunt's server to run the tests
    // proxies: {
    //   '/': 'http://localhost:9000/'
    // },
    // URL root prevent conflicts with the site root
    // urlRoot: '_karma_'
  });
};
