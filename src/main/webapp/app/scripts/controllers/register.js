'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:RegisterCtrl
 * @description
 * # RegisterCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('RegisterCtrl', function ($scope, $rootScope, AUTH_EVENTS, AuthService, ngDialog) {
        $scope.credentials = {
            username: '',
            password: '',
            firstName: '',
            lastName: ''
        };
        $scope.register = function (credentials) {

            AuthService.register(credentials).then(function (responseMessage) {
                $rootScope.$broadcast(AUTH_EVENTS.registerSuccess);
                ngDialog.closeAll();
                $scope.responseMessage=responseMessage;
                 $window.location.href = '/#/'; 
            }, function () {
                $rootScope.$broadcast(AUTH_EVENTS.registerFailed);
            });
        };
    });