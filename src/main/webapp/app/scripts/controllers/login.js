'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:LoginCtrl
 * @description
 * # LoginCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('LoginCtrl', function ($scope, $rootScope, AUTH_EVENTS, AuthService, $location, ngDialog, $window) {
        $scope.credentials = {
            username: '',
            password: ''
        };
        $scope.login = function (credentials) {
            
            AuthService.login(credentials).then(function (token) {
                
                $window.location.href = '/#/?token='+token;
                $window.location.reload();
                $rootScope.$broadcast(AUTH_EVENTS.loginSuccess);
                 ngDialog.closeAll();
                
                
                
            }, function () {
                $rootScope.$broadcast(AUTH_EVENTS.loginFailed);
            });
        };
    });