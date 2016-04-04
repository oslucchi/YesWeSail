'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:RegisterCtrl
 * @description
 * # RegisterCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('RegisterCtrl', function ($scope, $rootScope, AUTH_EVENTS, AuthService) {
        $scope.credentials = {
            username: '',
            password: '',
            firstName: '',
            lastName: ''
        };
        $scope.register = function (credentials) {

            AuthService.register(credentials).then(function (responseMessage) {
                $rootScope.$broadcast(AUTH_EVENTS.registerSuccess);
                $scope.responseMessage=responseMessage;
            }, function () {
                $rootScope.$broadcast(AUTH_EVENTS.registerFailed);
            });
        };
    });