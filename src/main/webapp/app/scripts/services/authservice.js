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
            var promise = $http

                

                .post(URLs.ddns + 'rest/auth/login', credentials)

                .then(function (res) {
                    var jsonResData = res.data;
                    Session.create(jsonResData.token, jsonResData.user);
                    
                    return jsonResData;
                });
            return promise;
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
                    return res;
                });
        };

        authService.isAuthenticated = function () {
            return $http.get(URLs.ddns+'rest/auth/isAuthenticated').then(function(res){
                return res.data.authorized;
            }, function(res){
                return res.data.authorized;
            });
        };

        authService.isAuthorized = function (role) {
            if(!!Session.userProfile){
                if(role<=Session.userProfile.roleId){
                    return true;
                }   
            }
        };
    
        
    
        return authService;
    });
