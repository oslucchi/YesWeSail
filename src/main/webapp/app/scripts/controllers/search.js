'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:SearchCtrl
 * @description
 * # SearchCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
  .controller('SearchCtrl', function ($scope, $http, $routeParams) {
    angular.element('.ui.dropdown').dropdown();
    
    $scope.place=$routeParams.place;
    $scope.style=$routeParams.style;
    
    
    $scope.getResults = function(){
          $http.get('http://localhost:3000/api/v1/search/'+$scope.place+'/'+$scope.style).then(function successCallback(response) {
            console.log(response.data);
            $scope.results=response.data.results;  
      }, function errorCallback(response) {
        // called asynchronously if an error occurs
        // or server returns response with an error status.
      });
        
    };
    
      $scope.getResults();
    
    
    
    
  });
