'use strict';

/**
 * @ngdoc overview
 * @name yeswesailApp
 * @description
 * # yeswesailApp
 *
 * Main module of the application.
 */
angular
    .module('yeswesailApp', [
    'ngAnimate'

        
        , 'ngCookies'

        
        , 'ngResource'

        
        , 'ngRoute'

        
        , 'ngSanitize'

        
        , 'ngTouch'

        
        , '720kb.datepicker'

        
        , 'ngDialog'
    , 'angular-carousel'
  ])
    .constant('AUTH_EVENTS', {
        loginSuccess: 'auth-login-success'
        , loginFailed: 'auth-login-failed'
        , registerSuccess: 'auth-register-success'
        , registerFailed: 'auth-register-failed'
        , logoutSuccess: 'auth-logout-success'
        , logoutFail: 'auth-logout-fail'
        , sessionTimeout: 'auth-session-timeout'
        , notAuthenticated: 'auth-not-authenticated'
        , notAuthorized: 'auth-not-authorized'
    })
    .constant('USER_ROLES', {
        all: '*'
        , user: 'user'
        , admin: 'admin'
        , sailor: 'sailor'
    })
    .constant('URLs', {
        ddns: 'http://yeswesail.ddns.net:8080/YesWeSail/'
    })
    .config(function ($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: 'views/main.html'
                , controller: 'MainCtrl'
                , controllerAs: 'main'
            })
            .when('/about', {
                templateUrl: 'views/about.html'
                , controller: 'AboutCtrl'
                , controllerAs: 'about'
            })
            .when('/comeFunziona', {
                templateUrl: 'views/comefunziona.html'
                , controller: 'ComefunzionaCtrl'
                , controllerAs: 'comeFunziona'
            })
            .when('/search/:place?/:style?', {
                templateUrl: 'views/search.html'
                , controller: 'SearchCtrl'
                , controllerAs: 'search'
            })
            .when('/events', {
                templateUrl: 'views/events.html'
                , controller: 'EventsCtrl'
                , controllerAs: 'events'
            })
            .when('/events/:eventId', {
                templateUrl: 'views/eventid.html'
                , controller: 'EventidCtrl'
                , controllerAs: 'eventId'
            })
            .otherwise({
                redirectTo: '/'
            });


    })
    .run(function ($rootScope, $cookieStore, $http, $location, Session, URLs) {
        angular.element('.ui.dropdown').dropdown({
            action: 'hide'
        });




    });