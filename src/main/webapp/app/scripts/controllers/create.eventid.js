'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:EventsEventidCtrl
 * @description
 * # EventsEventidCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('CreateEventidCtrl', function ($scope, $http, URLs, $stateParams) {
        angular.element('.ui.anchor-menu')
            .sticky({
                context: '#event-container',
            offset: 60
            });
    angular.element('.ui.book')
            .sticky({
                context: '#event-container',
            offset: 105
            });

            $scope.event={
                title: 'Title',
                shipOwner:{
                    imageURL:'https://conferencecloud-assets.s3.amazonaws.com/default_avatar.png',
                    name:'No',
                    surname: 'One'
                    },
                description: 'Description',
                logistics: 'Logistics',
                includes: 'Includes',
                location: 'Location',
                dateStart:'1460671200000',
                dateEnd: '1460844000000'
                     };  
                
            
//        $http.post(URLs.ddns + 'rest/events/details', {
//            eventId: $stateParams.eventId
//        }).then(function (res) {
//            $scope.event = res.data.event;
//            $scope.event.title=res.data.event.description;
//            $scope.shipOwner = res.data.shipOwner;
//            $scope.event.images = res.data.images;
//            $scope.event.tickets = res.data.tickets;
//            $scope.event.description=res.data.description.description;
//            $scope.event.participants=res.data.participants;
//            $scope.event.logistics= res.data.logistics;
//            $scope.event.includes= res.data.includes;
//            $scope.event.excludes= res.data.excludes;
//            
//        }, function (err) {});


     
    });