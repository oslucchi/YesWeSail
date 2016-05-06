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
        
        MAPS.getMap('LOCATIONS').then(function(res){
                     $scope.LOCATIONS = res;   
        }   
        );
        MAPS.getMap('CATEGORIES').then(function(res){
                     $scope.CATEGORIES = res;   
        }   
        );
    
    
        $scope.hotEvents=null;
        
        $http.post(URLs.ddns +'rest/events/hotEvents' ).then(function(res){
            $scope.hotEvents=res.data;
        }, function(err){})
      
  
//  $scope.getCategory=function(catId){
//      return lodash.find($scope.CATEGORIES, ['categoryId', catId]).description;
//  }
  
      
  
   $scope.initializeSelect = function () {


            angular.element('.ui.dropdown').dropdown();



        };
  });
