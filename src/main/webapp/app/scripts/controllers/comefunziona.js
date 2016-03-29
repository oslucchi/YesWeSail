'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:ComefunzionaCtrl
 * @description
 * # ComefunzionaCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
  .controller('ComefunzionaCtrl',['$scope', '$http', '$window', function ($scope, $http, $window) {
      $scope.login=function(){
              $http.post('http://localhost:3000/test', {'username': $scope.username, 'password': $scope.password}).then(function(response){
                  console.log(response.data.token);
                    $window.location.href='http://localhost:3000/'+response.data.token;
                }, function(response){});
      };
      
      

  }]);
