'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:CartsuccessCtrl
 * @description
 * # CartsuccessCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
  .controller('CartSuccessCtrl', function ($scope, $location, $cookieStore) {
    $scope.transactionId=$location.search().transactionId;
    $cookieStore.remove('bookedTickets');
  });
