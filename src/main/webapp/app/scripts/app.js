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
    '720kb.datepicker',
    'ngDialog'
  ])
    .constant('AUTH_EVENTS', {
        loginSuccess: 'auth-login-success',
        loginFailed: 'auth-login-failed',
        registerSuccess: 'auth-register-success',
        registerFailed: 'auth-register-failed',
        logoutSuccess: 'auth-logout-success',
        logoutFail: 'auth-logout-fail',
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
            .when('/events', {
              templateUrl: 'views/events.html',
              controller: 'EventsCtrl',
              controllerAs: 'events'
            })
            .when('/events/:eventId', {
              templateUrl: 'views/eventid.html',
              controller: 'EventidCtrl',
              controllerAs: 'eventId'
            })
            .otherwise({
                redirectTo: '/'
            });


    })
    .run(function($rootScope, $cookieStore, $http, $location, Session){
        angular.element('.ui.dropdown').dropdown({action: 'hide'});
        var token=$location.search().token;
        
        if(token!=null){
            $http.defaults.headers.common['Authorization'] = token;
            $http.defaults.headers.common['Language'] = 'IT';
            $http.post('YesWeSail/rest/users/basic').then(function(res){
                 
                Session.create(token, res.data);
                                                    }, function(err){});
        }
    
        if(token==null){
            
        $rootScope.globals = $cookieStore.get('globals') || {};
        if ($rootScope.globals.currentUser) {
            $http.defaults.headers.common['Authorization'] = $rootScope.globals.currentUser.token;
            $http.defaults.headers.common['Language'] = 'IT';
            $http.post('YesWeSail/rest/users/basic').then(function(res){
                
            Session.create($rootScope.globals.currentUser.token, res.data);                
               
                }, function(err){});
        }  
    }
        
      
    
});