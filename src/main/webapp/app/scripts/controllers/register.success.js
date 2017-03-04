'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:RegisterCtrl
 * @description
 * # RegisterCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('RegisterSuccessCtrl', function ($scope, $rootScope, $state) {
        $scope.responseMessage=$state.params.response;
    });