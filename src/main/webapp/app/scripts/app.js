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
    'ngAnimate',
    'ngCookies',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'ngTouch',
    '720kb.datepicker'
  ])
    .constant('AUTH_EVENTS', {
        loginSuccess: 'auth-login-success',
        loginFailed: 'auth-login-failed',
        logoutSuccess: 'auth-logout-success',
        sessionTimeout: 'auth-session-timeout',
        notAuthenticated: 'auth-not-authenticated',
        notAuthorized: 'auth-not-authorized'
    })
    .constant('USER_ROLES', {
        all: '*',
        user: 'user',
        admin: 'admin',
        sailor: 'sailor'
    })
    .config(function ($routeProvider) {
        $routeProvider
            .when('/', {
                templateUrl: 'views/main.html',
                controller: 'MainCtrl',
                controllerAs: 'main'
            })
            .when('/about', {
                templateUrl: 'views/about.html',
                controller: 'AboutCtrl',
                controllerAs: 'about'
            })
            .when('/comeFunziona', {
                templateUrl: 'views/comefunziona.html',
                controller: 'ComefunzionaCtrl',
                controllerAs: 'comeFunziona'
            })
            .when('/search/:place?/:style?', {
                templateUrl: 'views/search.html',
                controller: 'SearchCtrl',
                controllerAs: 'search'
            })
            .when('/login', {
                templateUrl: 'views/login.html',
                controller: 'LoginCtrl',
                controllerAs: 'login'
            })
            .when('/register', {
                templateUrl: 'views/register.html',
                controller: 'RegisterCtrl',
                controllerAs: 'register'
            })
            .otherwise({
                redirectTo: '/'
            });


    })
    .run(function(){
        angular.element('.ui.dropdown').dropdown({action: 'hide'});
        
        
    
});