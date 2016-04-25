'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
  .controller('MainCtrl', function ($scope, $http, URLs, MAPS, lodash ) {
 
        $scope.CATEGORIES=[];
        $scope.LOCATIONS=[];
    
        $scope.hotEvents=null;
        $http.post(URLs.ddns +'rest/events/hotEvents' ).then(function(res){
            $scope.hotEvents=res.data;
            $scope.CATEGORIES = MAPS.getMap('CATEGORIES');
            $scope.LOCATIONS = MAPS.getMap('LOCATIONS');
        }, function(err){})
      
       
     $scope.getCategory=function(catId){
         
         return lodash.find($scope.CATEGORIES, ['categoryId', catId]).description;
     }
     
      
      
  });
