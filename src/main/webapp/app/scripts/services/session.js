'use strict';

/**
 * @ngdoc service
 * @name yeswesailApp.Session
 * @description
 * # Session
 * Service in the yeswesailApp.
 */
angular.module('yeswesailApp')
  .service('Session', function () {
    this.create = function(sessionId, userId, userRole){
        this.id = sessionId;
        this.userId = userId;
        this.userRole = userRole;
    };
    this.destroy = function(){
        this.id = null;
        this.userId = null;
        this.userRole = null;
    };
  });
