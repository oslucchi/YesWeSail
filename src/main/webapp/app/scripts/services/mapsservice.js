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
    $http.post('http://yeswesail.ddns.net:8080/YesWeSail/rest/maps', {mapName: 'all'}).then(function(res){
            
            MAPS=res.data;
        
        },function(err){});
    
    MAPSService.getMap=function(map){
        return MAPS[map];
    };
    
    return MAPSService;
    
  });
