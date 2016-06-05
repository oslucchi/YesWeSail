'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:UseridCtrl
 * @description
 * # UseridCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('UseridCtrl', function ($scope, $stateParams, $http, URLs) {
        $scope.user = {};
        $http.get(URLs.ddns + 'rest/users/' + $stateParams.userId).then(function (res) {
             $scope.user=res.data;                                                                  
        }, function (err) {});
    });
