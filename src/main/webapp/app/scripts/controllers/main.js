'use strict';
/**
 * @ngdoc function
 * @name yeswesailApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp').controller('MainCtrl', function ($scope, $http, URLs, MAPS, lodash, $state, ngDialog) {
    $scope.CATEGORIES = [];
    $scope.LOCATIONS = [];
    MAPS.getMap('LOCATIONS').then(function (res) {
        if (!!res.status) {
            $scope.LOCATIONS = [];
        }
        else {
            $scope.LOCATIONS = res;
            $scope.locationsLoaded = true;
        }
    });
    MAPS.getMap('CATEGORIES').then(function (res) {
        if (!!res.status) {
            $scope.CATEGORIES = [];
        }
        else {
            $scope.CATEGORIES = res;
            $scope.categoriesLoaded = true;
        }
    });
    $scope.hotEvents = null;
    $http.post(URLs.ddns + 'rest/events/hotEvents').then(function (res) {
            $scope.hotEvents = res.data;
        }, function (err) {})

    $scope.showAvailableDatesForEvents = function (events) {
        $scope.aggregatedEvents = events;
        ngDialog.open({
            template: 'views/aggregatedEvents.html'
            , className: 'ngdialog-theme-default'
            , controller: 'MainCtrl'
            , scope: $scope
        });
    };
    $scope.closeModals = function () {
        ngDialog.closeAll();
    };
    $scope.processEventClick = function (events) {
        if (events.length > 1) {
            $scope.showAvailableDatesForEvents(events);
        }
        else if (events.length < 2) {
            $state.go('event', {
                eventId: events[0].idEvents
            });
        }
    }
});