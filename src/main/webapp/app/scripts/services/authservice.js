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
                .post('http://yeswesail.ddns.net:8080/YesWeSail/rest/auth/login', credentials)
                .then(function (res) {
                    Session.create(res.data.id, res.data.user.id, res.data.user.role);
                    return res.data.user;
                });
        };
        authService.isAuthenticated = function(){
            return !!Session.userId;
        };
        
        authService.isAuthorized = function(authorizedRoles){
                if(!angular.isArray(authorizedRoles)){
                    authorizedRoles = [authorizedRoles];
                }
                return (authService.isAuthenticated()&&authorizedRoles.indexOf(Session.userRole) !== 1);
        };
        return authService;
    });