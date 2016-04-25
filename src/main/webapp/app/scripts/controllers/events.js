'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:EventsCtrl
 * @description
 * # EventsCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('EventsCtrl', function ($scope, URLs, $http, $routeParams) {
        $scope.events = [];
        $http.post(URLs.ddns + 'rest/events/search', {
            location: $routeParams.location,
            categoryId: $routeParams.categoryId
        }).then(function (res) {
            $scope.events=res.data;

        }, function (err) {});
    });
