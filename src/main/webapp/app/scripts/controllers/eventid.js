'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:EventsEventidCtrl
 * @description
 * # EventsEventidCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('EventidCtrl', function ($scope, $http, URLs, $routeParams) {
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


        $http.post(URLs.ddns + 'rest/events/details', {
            eventId: $routeParams.eventId
        }).then(function (res) {
            $scope.event = res.data.event;
            $scope.event.title=res.data.event.description;
            $scope.shipOwner = res.data.shipOwner;
            $scope.event.images = res.data.images;
            $scope.event.tickets = res.data.tickets;
            $scope.event.description=res.data.description.description;
            $scope.event.participants=res.data.participants;
            $scope.event.logistics= res.data.logistics;
            $scope.event.includes= res.data.includes;
            $scope.event.excludes= res.data.excludes;
            
        }, function (err) {});


        $scope.testTickets = [[{
                "available": 4
                , "booked": 0
                , "cabinRef": 0
                , "description": "Posto in cuccetta - bagno in comune"
                , "eventId": 5
                , "idEventTickets": 10
                , "price": 400
                , "ticketType": 1
    }]
    , [
                {
                    "available": 1
                    , "booked": 0
                    , "cabinRef": 1
                    , "description": "Posto in cabina con bagno"
                    , "eventId": 5
                    , "idEventTickets": 11
                    , "price": 450
                    , "ticketType": 2
    }
                , {
                    "available": 1
                    , "booked": 0
                    , "cabinRef": 1
                    , "description": "Posto in cabina con bagno"
                    , "eventId": 5
                    , "idEventTickets": 11
                    , "price": 480
                    , "ticketType": 2
    }
    ]
  ];


        $scope.map = {
            center: {
                latitude: 45
                , longitude: -73
            }
            , zoom: 8
            , options: {
                scrollwheel: false
            }
        };
    });