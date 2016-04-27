'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:LoginCtrl
 * @description
 * # LoginCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('LoginCtrl', function ($scope, $rootScope, AUTH_EVENTS, AuthService, $location, ngDialog) {
        $scope.credentials = {
            username: '',
            password: ''
        };
        $scope.login = function (credentials) {
            
            AuthService.login(credentials).then(function (token) {
                
                $location.path('/#/?token='+token);
                $rootScope.$broadcast(AUTH_EVENTS.loginSuccess);
                 ngDialog.closeAll();
                
                
                
            }, function () {
                $rootScope.$broadcast(AUTH_EVENTS.loginFailed);
            });
        };
    });