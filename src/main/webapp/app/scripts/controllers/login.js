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
                $rootScope.$broadcast(AUTH_EVENTS.loginSuccess);
                 ngDialog.closeAll();
                $location.path('/#/?token='+token);
                
                
            }, function () {
                $rootScope.$broadcast(AUTH_EVENTS.loginFailed);
            });
        };
    });