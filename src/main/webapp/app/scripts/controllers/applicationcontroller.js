'use strict';
/**
 * @ngdoc function
 * @name yeswesailApp.controller:ApplicationCtrl
 * @description
 * # ApplicationCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp').controller('ApplicationCtrl', function ($translate, $scope, $timeout, USER_ROLES, AUTH_EVENTS, AuthService, $location, $rootScope, $cookieStore, ngDialog, Session, URLs, $http, $log, CartService, LocaleService, $state, LOCALES) {
    //        $rootScope.$on("$locationChangeStart", function(event, next, current) { 
    //            if(!AuthService.isAuthenticated()){
    //                 event.preventDefault()
    //            }
    //      });
    $('.ui.selection.language.dropdown').dropdown({
        action: 'activate'
    });
    $scope.language = LOCALES.preferredLocale; // "it_IT "; // $translate.proposedLanguage();
    $scope.cartQty = null;
    $scope.$watch(function () {
        return CartService.cartQty;
    }, function (data) {
        $scope.cartQty = data;
    });
    
    $scope.url=URLs.ddns;
	$scope.cartExpires = null;
    $scope.$watch(function () {
    	return CartService.cartExpires;
    }, function (data) {
        $scope.cartExpires = data;
    });
    
    $scope.callbackTimer={
        finished: function(){
             $timeout(CartService.getAllItems, 5000);
             $scope.cartExpires = null;
        }
    }
      $scope.preventD=function(e){
        e.stopPropagation();
    }
    $scope.email=function(obj, e){
        e.stopPropagation();
        location.href= 'mailto:?subject='+obj.title+'&body='+$translate.instant('global.emailMessage')+'%0D%0A'+URLs.ddns+'events/'+obj.idEvents;
    }
    $scope.currentUser = null;
    $scope.setCurrentUser = function (user) {
        $rootScope.currentUser = user;
        $scope.currentUser = user;
    }
    var token = $location.search().token;
    var invalidEmail = $location.search().invalidEmail;
    if (token != null) {
        $http.defaults.headers.common['Authorization'] = token;
        $http.defaults.headers.common['Language'] = $scope.language;
        $http.post(URLs.ddns + 'rest/users/basic').then(function (res) {
            Session.create(token, res.data);
            $scope.setCurrentUser(res.data);
        }, function (err) {});
    }
    if (token == null) {
        $rootScope.globals = $cookieStore.get('globals') || {};
        if ($rootScope.globals.currentUser) {
            $http.defaults.headers.common['Authorization'] = $rootScope.globals.currentUser.token;
            $http.defaults.headers.common['Language'] = $scope.language;
            $http.post(URLs.ddns + 'rest/users/basic').then(function (res) {
                Session.create($rootScope.globals.currentUser.token, res.data);
                $scope.setCurrentUser(res.data);
            }, function (err) {
                if (err.status == 401) {
                    Session.destroy();
                }
            });
        }
    }
    $scope.credentials = {
        username: ''
        , password: ''
    }
    $scope.isAuthorized = AuthService.isAuthorized;
    $scope.isAuthenticated = AuthService.isAuthenticated;
    $scope.invalidMail = function () {
        if ($scope.currentUser == null) {
            return false;
        }
        if ($scope.currentUser.email.startsWith('fake.') && $scope.currentUser.email.endsWith('yeswesail.com')) {
            return true;
        }
    }
    
    $rootScope.$on("$stateChangeSuccess", function(){
     window.scrollTo(0,0);
    })
    
    CartService.getAllItems();
    $scope.login = AuthService.login;
    $scope.logout = function () {
        AuthService.logout().then(function () {
            $rootScope.$broadcast(AUTH_EVENTS.logoutSuccess);
            $scope.setCurrentUser(null);
            $location.path('/');
        }, function () {
            $rootScope.$broadcast(AUTH_EVENTS.logoutFail);
        });
    };
    var loginDialog = ngDialog;
    var registerDialog = ngDialog;
    var invalidEmailDialog = ngDialog;
    $scope.popupLogin = function (data) {
        $scope.previousState = data;
        ngDialog.closeAll();
        loginDialog.open({
            template: 'views/login.html'
            , className: 'ngdialog-theme-default'
            , controller: 'LoginCtrl'
            , scope: $scope
        });
    };
    $scope.popupRegister = function () {
        ngDialog.closeAll();
        registerDialog.open({
            template: 'views/register.html'
            , className: 'ngdialog-theme-default'
            , controller: 'RegisterCtrl'
            , scope: $scope
        });
    };
    $scope.popupCreateEventDialog = function () {
        ngDialog.closeAll();
        registerDialog.open({
            template: 'views/createEvent.html'
            , className: 'ngdialog-theme-default'
            , controller: 'CreateEventCtrl'
        });
    };
    $scope.changeLanguage = function (lang) {
        $http.defaults.headers.common['Language'] = lang;
        LocaleService.setLocale(lang);
        $state.go($state.current, {}, {
            reload: true
        });
    }
    if (invalidEmail == 'true') {
        invalidEmailDialog.open({
            template: 'views/invalidemail.html'
            , className: 'ngdialog-theme-default'
            , controller: 'InvalidEmailCtrl'
            , scope: $scope
            , closeByEscape: false
            , showClose: false
            , closeByNavigation: false
            , closeByDocument: false
        });
    }
    
    
    $scope.$on('LoginRequired', function (event, data) {
        $scope.popupLogin(data);
    });
    //       $rootScope.$on("$stateChangeStart", 
    //    function (event, toState, toParams, 
    //              fromState, fromParams) {
    //    if (!AuthService.isAuthorized(toState.data.accessLevel)) {
    //        $rootScope.error = "Access denied";
    //        event.preventDefault();
    //
    //    }
    //}); 
});