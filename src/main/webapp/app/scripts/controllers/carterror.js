'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:CarterrorCtrl
 * @description
 * # CarterrorCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
  .controller('CartErrorCtrl', function ($scope, $location) {
     $scope.responseCode=$location.search().responseCode;
  });
