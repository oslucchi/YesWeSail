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
        this.create = function (token, userProfile) {
            this.token = token;
            this.userProfile = userProfile;
            
            $rootScope.globals={
                currentUser: {
                    user:this.userProfile,
                    token: this.token
                }
            };
            $rootScope.currentUser=this.userProfile;
            $cookieStore.put('globals', $rootScope.globals);
            
        };
        this.getCurrentUser= function(){
            console.log(this.userProfile);
            return this.userProfile;
        };
        this.destroy = function () {
            this.token = null;
            this.userProfile = null;
            $rootScope.globals={};
            $cookieStore.remove('globals');
            $http.defaults.headers.common.Authorization = '';
        };
    });