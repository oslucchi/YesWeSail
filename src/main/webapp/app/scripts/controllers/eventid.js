'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:EventsEventidCtrl
 * @description
 * # EventsEventidCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('EventidCtrl', function ($scope, $http, URLs, $stateParams, CartService, $anchorScroll, $location) {
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

     $scope.getRating=function(userId){
            $http.get(URLs.ddns + 'rest/reviews/'+userId+'/rating').then(function(res){

                $scope.reputation={
                    rating: res.data.rating,
                    populationSize: res.data.populationSize
                };


                     $('.reputation.star.rating').rating({
                        initialRating: Math.round($scope.reputation.rating),
                        maxRating: 5
                }).rating('disable');
            });

        };
    
    
        
    
        $http.post(URLs.ddns + 'rest/events/details', {
            eventId: $stateParams.eventId
        }).then(function (res) {
            $scope.event = res.data.event;
            $scope.event.title=res.data.event.title;
            $scope.shipOwner = res.data.shipOwner;
            $scope.event.images = res.data.images;
            $scope.event.tickets = res.data.tickets;
            $scope.boat=res.data.boat;
            if(!!res.data.participantMessage){
                $scope.event.participantMessage=res.data.participantMessage;
            };
            if(!!res.data.description){
                $scope.event.description=res.data.description.description;
            };
            $scope.event.participants=res.data.participants;
            $scope.event.logistics= res.data.logistics;
            $scope.event.includes= res.data.includes;
            $scope.event.excludes= res.data.excludes;
            $scope.event.route=res.data.route;
            
              $scope.map = {
            center: {
                latitude: $scope.event.route[0].lat,
                longitude: $scope.event.route[0].lng
            }
            , zoom: 12
            , options: {
                scrollwheel: false
            }
            
        };
            
        

        $scope.getRating(res.data.shipOwner.idUsers);
            
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


      
    
    $scope.bookTicket=function(){
        
    };
    
    
     $scope.goToSection = function(id) {
      // set the location.hash to the id of
      // the element you wish to scroll to.
      $location.hash(id);

      // call $anchorScroll()
      $anchorScroll();
    };
    
    
    });