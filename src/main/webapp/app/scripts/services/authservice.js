'use strict';

/**
 * @ngdoc service
 * @name yeswesailApp.AuthService
 * @description
 * # AuthService
 * Service in the yeswesailApp.
 */
angular.module('yeswesailApp')
    .factory('AuthService', function ($http, Session) {
        var authService = {};

        authService.login = function (credentials) {
            return $http

                .post('http://localhost:1337/yeswesail.ddns.net:8080/YesWeSail/rest/auth/login', credentials)

                .post('http://yeswesail.ddns.net:8080/YesWeSail/rest/auth/login', credentials)

                .then(function (res) {
                    var jsonResData = JSON.parse(res.data);
                    Session.create(jsonResData.token, jsonResData.user.idUsers, jsonResData.user.roleId);
                    return jsonResData.user;
                });
        };

        authService.logout = function () {
            var token = Session.getSessionToken();

            return $http
                .post('http://localhost:1337/yeswesail.ddns.net:8080/YesWeSail/rest/auth/logout', {
                    token: token
                })
                .then(function (res) {
                    Session.destroy();
                    return null;
                });
        };

        authService.register = function (credentials) {
            return $http
                .post('http://localhost:1337/yeswesail.ddns.net:8080/YesWeSail/rest/auth/register', credentials)
                .then(function (res) {
                    var jsonResData = JSON.parse(res.data);
                    return jsonResData;
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