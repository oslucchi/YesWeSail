'use strict';

/**
 * @ngdoc service
 * @name yeswesailApp.AuthService
 * @description
 * # AuthService
 * Service in the yeswesailApp.
 */
angular.module('yeswesailApp')
    .factory('AuthService', function ($http, Session, URLs, $rootScope) {
        var authService = {};

        authService.login = function (credentials) {
            return $http

                

                .post(URLs.ddns + 'rest/auth/login', credentials)

                .then(function (res) {
                    var jsonResData = res.data;
                    Session.create(jsonResData.token, jsonResData.user);
                    return jsonResData.token;
                });
        };

        authService.logout = function () {
           

            return $http
                .post(URLs.ddns + 'rest/auth/logout', {
                    token: $rootScope.globals.currentUser.token
                })
                .then(function (res) {
                    Session.destroy();
                    return null;
                });
        };

        authService.register = function (credentials) {
            return $http
                .post(URLs.ddns + 'rest/auth/register', credentials)
                .then(function (res) {
                    return res.data;
                });
        };

        authService.isAuthenticated = function () {
            return !!Session.userId;
        };

        authService.isAuthorized = function (authorizedRoles) {
            if (!angular.isArray(authorizedRoles)) {
                authorizedRoles = [authorizedRoles];
            }
            return (authService.isAuthenticated() && authorizedRoles.indexOf(Session.userRole) !== 1);
        };
    
        
    
        return authService;
    });
