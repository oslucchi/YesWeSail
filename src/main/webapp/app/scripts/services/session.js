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
            $cookieStore.put('globals', $rootScope.globals);
            
            
        };
        this.getCurrentUser= function(){
            return this.userProfile;
        };
        
        this.isAdmin=function(roleId){
            if(roleId==9){
              return true;  
            }else{
            return false;
        };
        };
    
        this.destroy = function () {
            this.token = null;
            this.userProfile = null;
            $rootScope.globals={};
            $cookieStore.remove('globals');
            $http.defaults.headers.common.Authorization = '';
        };
    });