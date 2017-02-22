'use strict';
/**
 * @ngdoc function
 * @name yeswesailApp.controller:EventsCtrl
 * @description
 * # EventsCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp').controller('EventsCtrl', function ($scope, URLs, $http, $stateParams, MAPS, ngDialog, $state) {
    var body = {};
    $scope.selectedCategory = $stateParams.categoryId;
    $scope.selectedLocation = $stateParams.location;
    $scope.CATEGORIES = [];
    $scope.LOCATIONS = [];
        $scope.locationsLoaded = false;
    $scope.categoriesLoaded=false;
    
    MAPS.getMap('LOCATIONS').then(function (res) {
        $scope.LOCATIONS = res;
        $scope.locationsLoaded = true;
    });
    
    MAPS.getMap('CATEGORIES').then(function (res) {
        $scope.CATEGORIES = res;
        $scope.categoriesLoaded=true;
    });
    
    if ($stateParams.location != '') {
        body.location = $stateParams.location;
    }
    if ($stateParams.categoryId != '') {
        body.categoryId = $stateParams.categoryId;
    }
    body.activeOnly = true;
    var getEvents = function () {
        $http.post(URLs.ddns + 'rest/events/search/actives', body).then(function (res) {
            $scope.events = res.data.events;
            $scope.shipOwners = res.data.shipowners;
            if (res.data[0] == null) {
             
            }
        }, function (err) {});
    };
    getEvents();
    
    $scope.search = function () {
        if ($scope.selectedLocation) {
            body.location = $scope.selectedLocation;
        }
        
        else {
            body.location = null;
        }
        
        if ($scope.selectedCategory) {
            body.categoryId = $scope.selectedCategory;
        }
        else {
            body.categoryId = null;
        }
        
        $state.go($state.current, {categoryId: body.categoryId, location: body.location}, {reload: true});
        
    };
 
    $scope.showAvailableDatesForEvents = function (events) {
        $scope.aggregatedEvents = events;
        ngDialog.open({
            template: 'views/aggregatedEvents.html'
            , className: 'ngdialog-theme-default'
            , controller: 'EventsCtrl'
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