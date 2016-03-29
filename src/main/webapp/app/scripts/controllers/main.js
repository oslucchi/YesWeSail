'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
  .controller('MainCtrl', ['$scope', '$http', function ($scope, $http ) {
    angular.element('.ui.dropdown').dropdown();
    
    
    
;
    $http.get('http://localhost:3000/api/v1/events/places').then(function successCallback(response) {
        console.log(response.data.regioni);
    $scope.places=response.data.regioni;
      }, function errorCallback(response) {
        // called asynchronously if an error occurs
        // or server returns response with an error status.
      });
      
      
      $http.get('http://localhost:3000/api/v1/events/styles').then(function successCallback(response) {
        console.log(response.data.styles);
        $scope.styles=response.data.styles;
      }, function errorCallback(response) {
        // called asynchronously if an error occurs
        // or server returns response with an error status.
      });
         
      
      
    $http.get('http://localhost:3000/api/v1/events/proposals').then(function successCallback(response) {
       
    $scope.proposals=response.data;
  }, function errorCallback(response) {
    // called asynchronously if an error occurs
    // or server returns response with an error status.
  });
          
        
      
     
      
  }]);
