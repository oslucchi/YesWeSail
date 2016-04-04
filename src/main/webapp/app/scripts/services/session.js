'use strict';

/**
 * @ngdoc service
 * @name yeswesailApp.Session
 * @description
 * # Session
 * Service in the yeswesailApp.
 */
angular.module('yeswesailApp')
    .service('Session', function ($cookieStore, $http, $rootScope) {
        this.create = function (token, userId, userRole) {
            this.token = token;
            this.userId = userId;
            this.userRole = userRole;
            $rootScope.globals={
                currentUser: {
                    userId: this.userId,
                    token: this.token
                }
            };
            
            $http.defaults.headers.common['Authorization'] = 'Basic ' + this.token;
            $cookieStore.put('globals', $rootScope.globals);
            
        };
        this.destroy = function () {
            this.token = null;
            this.userId = null;
            this.userRole = null;
            $rootScope.globals={};
            $cookieStore.remove('globals');
            $http.defaults.headers.common.Authorization = 'Basic';
        };
        this.getSessionToken = function(){
            return this.token;
        };
    });