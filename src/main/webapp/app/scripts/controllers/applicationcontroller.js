'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:ApplicationCtrl
 * @description
 * # ApplicationCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('ApplicationCtrl', function ($scope, USER_ROLES, AUTH_EVENTS, AuthService, $location, $rootScope, $cookieStore, ngDialog, Session) {
    angular.element('.ui.dropdown').dropdown();    
    $scope.currentUser=null;
    $scope.setCurrentUser= function(user){
        
    };

    
        $scope.isAuthorized = AuthService.isAuthorized;
        $scope.isAuthenticated = AuthService.isAuthenticated;
        
    

        $scope.logout = function () {
            AuthService.logout().then(function () {
                $rootScope.$broadcast(AUTH_EVENTS.logoutSuccess);
                $scope.setCurrentUser(null);
                $location.path('/#');
            }, function () {
                $rootScope.$broadcast(AUTH_EVENTS.logoutFail);
            });
        };
    
    var loginDialog = ngDialog;
     var registerDialog = ngDialog;
    
    
    $scope.popupLogin = function(){
        
        ngDialog.closeAll();
        loginDialog.open({ template: 'views/login.html', className: 'ngdialog-theme-default', controller: 'LoginCtrl' });    
    };
    
    $scope.popupRegister = function(){
       ngDialog.closeAll();
        registerDialog.open({ template: 'views/register.html', className: 'ngdialog-theme-default', controller: 'RegisterCtrl' });    
    };  

    });