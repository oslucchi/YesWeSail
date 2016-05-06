'use strict';

/**
 * @ngdoc service
 * @name yeswesailApp.CATEGORIES
 * @description
 * # CATEGORIES
 * Service in the yeswesailApp.
 */
angular.module('yeswesailApp')
  .factory('MAPS', function ($http) {
    var MAPS={};
    var MAPSService={};
    
    
  
    
    MAPSService.getMap=function(map){
        var promise =   $http.post('http://yeswesail.ddns.net:8080/YesWeSail/rest/maps', {mapName: 'all'}).then(function(res){
            
            return res.data[map];
        
        },function(err){
            return err;
        });
        
        return promise;
    };
    
    return MAPSService;
    
  });
