'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:EventsCtrl
 * @description
 * # EventsCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('EventsCtrl', function ($scope, URLs, $http, $stateParams, MAPS) {
        var body = {};
        $scope.selectedCategory = $stateParams.categoryId;
        $scope.selectedLocation = $stateParams.location;
    
        $scope.CATEGORIES=[];
        $scope.LOCATIONS=[];
        
        MAPS.getMap('LOCATIONS').then(function(res){
                     $scope.LOCATIONS = res;   
        }   
        );
        MAPS.getMap('CATEGORIES').then(function(res){
                     $scope.CATEGORIES = res;   
        }   
        );
    
    
        if ($stateParams.location != '') {
            body.location = $stateParams.location;

        }
        if ($stateParams.categoryId != '') {
            body.categoryId = $stateParams.categoryId;

        }
    
        body.activeOnly=true;
        $http.post(URLs.ddns + 'rest/events/search/actives', body).then(function (res) {
            $scope.events = res.data;
            if (res.data[0] == null) {
//                angular.element('.dimmer').dimmer({
//                    closable: false
//                }).dimmer('show');
            }
        }, function (err) {});


        $scope.initializeSelect = function () {


            angular.element('.ui.dropdown.item').dropdown();



        };
    });