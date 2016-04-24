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
 
        $scope.cat=[];
    
        $scope.hotEvents=null;
        $http.post(URLs.ddns +'rest/events/hotEvents' ).then(function(res){
            $scope.hotEvents=res.data;
            $scope.cat = MAPS.getCategories();
        }, function(err){})
      
     $scope.getCategory=function(catId){
         
         return lodash.find($scope.cat, ['categoryId', catId]).description;
     }
     
      
      
  });
