'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:ApplicationCtrl
 * @description
 * # ApplicationCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('ApplicationCtrl', function ($scope, USER_ROLES, AUTH_EVENTS, AuthService, $location, $rootScope, $cookieStore) {
        $rootScope.globals = $cookieStore.get('globals') || {};
        $scope.currentUser = $rootScope.globals.currentUser || {};
        $scope.userRoles = USER_ROLES;
        $scope.isAuthorized = AuthService.isAuthorized;
        $scope.isAuthenticated = AuthService.isAuthenticated;
        $scope.setCurrentUser = function (user) {
            $scope.currentUser = user;
        };

        $scope.logout = function () {
            AuthService.logout().then(function () {
                $rootScope.$broadcast(AUTH_EVENTS.logoutSuccess);
                $scope.setCurrentUser(null);
                $location.path('/#');
            }, function () {
                $rootScope.$broadcast(AUTH_EVENTS.logoutFail);
            });
        };

    });