'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:EventsCtrl
 * @description
 * # EventsCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('EventsCtrl', function ($scope, URLs, $http, $routeParams, MAPS) {
        var body = {};
      $scope.selectedCategory=$routeParams.categoryId;
     $scope.selectedLocation=$routeParams.location;
    if($routeParams.location!=''){
        body.location=$routeParams.location;
       
    }
    if($routeParams.categoryId!=''){
        body.categoryId=$routeParams.categoryId;
      
    }
    $http.post(URLs.ddns + 'rest/events/search', body
        ).then(function (res) {
        
    
            $scope.events=res.data;
   $scope.CATEGORIES = MAPS.getMap('CATEGORIES');
            $scope.LOCATIONS = MAPS.getMap('LOCATIONS');
            if(res.data[0]==null){
                angular.element('.dimmer').dimmer({closable: false}).dimmer('show');
            }
        }, function (err) {});
    
    
   $scope.initializeSelect = function () {


            angular.element('.ui.dropdown').dropdown();



        };
    });
