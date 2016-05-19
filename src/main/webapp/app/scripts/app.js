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
        , 'ngSanitize'
        , 'ngTouch'
        , '720kb.datepicker'
        , 'ngDialog'
        , 'angular-carousel'
    , 'ngLodash'
    , 'uiGmapgoogle-maps',
    'ui.router',
    'textAngular',
    'angucomplete-alt',
    'ngAutocomplete',
    'toastr'
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



.constant('URLs', {
    ddns: 'http://yeswesail.ddns.net:8080/YesWeSail/'
})



.config(function ($stateProvider, $urlRouterProvider) {
    
    // For any unmatched url, redirect to /
  $urlRouterProvider.otherwise("/");
  //
  // Now set up the states
  $stateProvider
    .state('main', {
      url: "/",
      templateUrl: 'views/main.html',
      controller:'MainCtrl'
    })
    .state('howitworks', {
      url: "/howitworks",
      templateUrl: 'views/comefunziona.html',
      controller:  'ComefunzionaCtrl'
    })
    .state('events', {
      url: "/events?location&categoryId",
      templateUrl: 'views/events.html',
      controller:  'EventsCtrl'
    })
    .state('event', {
      url: "/events/:eventId",
      templateUrl:  'views/eventid.html',
      controller: 'EventidCtrl'
    })
    .state('editEvent', {
      url: "/edit-event/:eventId",
      templateUrl:  'views/create.eventid.html',
      controller: 'EditEventCtrl'
    })
      .state('admin', {
      url: "/admin",
      templateUrl:  'views/admin.html',
      controller: 'AdminCtrl'
    }) 
      .state('admin.events', {
      url: "/events",
      templateUrl:  'views/admin.events.html',
      controller: 'AdminCtrl'
    })
      .state('admin.mail', {
      url: "/mail",
      templateUrl:  'views/admin.events.html',
      controller: 'AdminCtrl'
    });
    
    
    
    
    
    
    
    
    
       
           


    })
    .run(function ($rootScope, $cookieStore, $http, $location, Session, URLs) {
        $http.defaults.headers.common['Language'] = 'IT';
//        angular.element('.ui.usermenu.dropdown').dropdown();




    });
