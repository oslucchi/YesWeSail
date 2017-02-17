'use strict';
/**
 * @ngdoc function
 * @name yeswesailApp.controller:EventsEventidCtrl
 * @description
 * # EventsEventidCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp').controller('EventidCtrl', function ($scope, $http, URLs, $stateParams, $state, AuthService, $rootScope, CartService, $anchorScroll, $location) {
    $scope.noEventFound = false;
    //    angular.element('.ui.anchor-menu').sticky({
    //        context: '#event-container'
    //        , offset: 60
    //    });
    $scope.goToProfile = function (userId) {
        AuthService.isAuthenticated().then(function (res) {
            if (!res) {
                $rootScope.$broadcast('LoginRequired', $state);
                return;
            }
            else {
                $state.go('userId.profile', {
                    userId: userId
                });
            }
        }, function (err) {})
    }
    $scope.getRating = function (userId) {
        $http.get(URLs.ddns + 'rest/reviews/' + userId + '/rating').then(function (res) {
            $scope.reputation = {
                rating: res.data.rating
                , populationSize: res.data.populationSize
            };
            $('.reputation.star.rating').rating({
                initialRating: Math.round($scope.reputation.rating)
                , maxRating: 5
            }).rating('disable');
        });
    };
    $http.post(URLs.ddns + 'rest/events/details', {
        eventId: $stateParams.eventId
    }).then(function (res) {
        angular.element('.tickets-sticky.ui.sticky').sticky({
            context: '#event-container'
            , offset: 60
            , observeChanges: true
        });
        $scope.noEventFound = false;
        $scope.groundEvents = res.data.groundEvents;
        $scope.event = res.data.event;
        $scope.event.title = res.data.event.title;
        $scope.shipOwner = res.data.shipOwner;
        $scope.event.images = res.data.images;
        $scope.imagesSmall = res.data.imagesSmall;
        $scope.imagesMedium = res.data.imagesMedium;
        $scope.imagesLarge = res.data.imagesLarge;
        $scope.event.tickets = res.data.tickets;
        angular.element('.cover-img').css('background-position-y', res.data.event.backgroundOffsetY + 'px');
        $scope.boat = res.data.boat;
        $scope.markers = [];
        if (!!res.data.participantMessage) {
            $scope.event.participantMessage = res.data.participantMessage;
        };
        if (!!res.data.description) {
            $scope.event.description = res.data.description.description;
        };
        $scope.event.participants = res.data.participants;
        $scope.event.logistics = res.data.logistics;
        $scope.event.includes = res.data.includes;
        $scope.event.excludes = res.data.excludes;
        $scope.event.route = res.data.route;
        angular.forEach(res.data.route, function (value, key) {
            if (key == res.data.route.length - 1) {
                $scope.markers.push({
                    id: value.seq
                    , coords: {
                        latitude: value.lat
                        , longitude: value.lng
                    }
                    , description: value.description
                    , options: {
                        draggable: false
                        , icon: 'images/spotlight-poi-green.png'
                    }
                })
            }
            else if (key > 0) {
                $scope.markers.push({
                    id: value.seq
                    , coords: {
                        latitude: value.lat
                        , longitude: value.lng
                    }
                    , description: value.description
                    , options: {
                        draggable: false
                    }
                })
            }
            else {
                $scope.markers.push({
                    id: value.seq
                    , coords: {
                        latitude: value.lat
                        , longitude: value.lng
                    }
                    , description: value.description
                    , options: {
                        draggable: false
                        , icon: 'images/spotlight-poi-blue.png'
                    }
                })
            }
        });
        $scope.windowOptions = {
            pixelOffset: {
                width: 0
                , height: -40
            }
        }
        if ($scope.event.route) {
            $scope.map = {
                center: {
                    latitude: $scope.event.route[0].lat
                    , longitude: $scope.event.route[0].lng
                }
                , zoom: 12
                , options: {
                    scrollwheel: false
                }
            };
        }
        $scope.getRating(res.data.shipOwner.idUsers);
        angular.element('#slick-demo').slick({
            slidesToShow: 3
            , slidesToScroll: 3
        });
        angular.element('#slick-demo').slickLightbox({
            src: 'src'
            , images: $scope.imagesLarge
            , itemSelector: '.item'
        });
        angular.element('#slick-boat').slick({
            slidesToShow: 1
            , slidesToScroll: 1
        });
        angular.element('#slick-boat').slickLightbox({
            src: 'src'
            , images: $scope.boat.images
            , itemSelector: '.item'
        });
        //                   angular.element('.tickets-sticky.ui.sticky').sticky('refresh');
    }, function (err) {
        $scope.noEventFound = true;
    });
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
    $scope.bookTicket = function () {};
    $scope.goToSection = function (id) {
        // set the location.hash to the id of
        // the element you wish to scroll to.
        $location.hash(id);
        // call $anchorScroll()
        $anchorScroll();
    };
});