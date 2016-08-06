'use strict';
/**
 * @ngdoc function
 * @name yeswesailApp.controller:EventsEventidCtrl
 * @description
 * # EventsEventidCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp').controller('EditEventCtrl', function ($scope, $http, URLs, $stateParams, Upload, $timeout, $filter, toastr, $translate, uiGmapIsReady) {
    angular.element('.ui.anchor-menu').sticky({
        context: '#event-container'
        , offset: 60
    });
    angular.element('.ui.book').sticky({
        context: '#event-container'
        , offset: 105
    });
    var bDisabled = false;
    $scope.tDisabled=false;

    
    
    $scope.selectedLanguage = $translate.proposedLanguage();
    $scope.tempEvent = {};
    $scope.markers = [];
    $scope.selectedBoat = null;
    $scope.getBoats = function (userId) {
        $http.get(URLs.ddns + 'rest/users/shipowners/' + userId + '/boats').then(function (res) {
            $scope.boats = res.data.boats;
        });
    };
    $scope.setSelectedBoat = function (boat) {
//        if (bDisabled) {
//            alert($translate.instant('global.boatEditDisabled'));
//        }
//        else {
            $scope.selectedBoat = boat;
            $scope.maxTickets = maxTicketsForBoat(boat)
//        }
    }
    $scope.getEvent = function () {
        $http.post(URLs.ddns + 'rest/events/details', {
            eventId: $stateParams.eventId
        }, {
            headers: {
                'Edit-Mode': 'true'
                , 'Language': $scope.selectedLanguage
            }
        }).then(function (res) {
            $scope.event = res.data.event;
            $scope.event.dateStart = $filter('date')(res.data.event.dateStart, 'yyyy-MM-dd');
            $scope.event.dateEnd = $filter('date')(res.data.event.dateEnd, 'yyyy-MM-dd');
            $scope.shipOwner = res.data.shipOwner;
            $scope.images = res.data.images;
            $scope.tickets = res.data.tickets;
            
            $scope.participants = res.data.participants;
            $scope.logistics = res.data.logistics;
            $scope.includes = res.data.includes;
            $scope.route=res.data.route;
            $scope.excludes = res.data.excludes;
            $scope.setSelectedBoat(res.data.boat);
            $scope.description = res.data.description;
            $scope.setSelectedBoat(res.data.boat);
            if (res.data.boat.boatId != 1) {
                bDisabled = true;
            }
            else {
                bDisabled = false;
            }
            if (res.data.tickets>0) {
                $scope.tDisabled = true;
            }
            else {
                    $scope.tickets=[
        [{
            "available": 1
            , "booked": 0
            , "bookedTo": null
            , "cabinRef": 0
            , "description": ""
            , "eventId": $stateParams.eventId
            , "idEventTickets": 0
            , "price": 100
            , "ticketType": 1
        }]
    ]
                $scope.tDisabled = false;
            }
            $scope.newLocation = {
                latitude: res.data.route[0].lat
                , longitude: res.data.route[0].lng
            };
            $scope.maxTickets = maxTicketsForBoat(res.data.boat);
            $scope.map = {
                center: {
                    latitude: $scope.newLocation.latitude
                    , longitude: $scope.newLocation.longitude
                }
                , zoom: 5
                , options: {
                    scrollwheel: false
                }
                , control: {}
                , events: {
                    //            click: function (map, eventName, originalEventArgs) {
                    //                var e = originalEventArgs[0];
                    //                var lat = e.latLng.lat(),lon = e.latLng.lng();
                    //                var marker = {
                    //                    id: $scope.markers.length,
                    //                    coords: {
                    //                        latitude: lat,
                    //                        longitude: lon
                    //                    }
                    //                };
                    //                $scope.markers.push(marker);
                    //                $scope.$apply();
                    //            }
                }
            };
            angular.forEach(res.data.route, function (value, key) {
                $scope.markers.push({
                    coords: {
                        latitude: value.lat
                        , longitude: value.lng
                    }
                    , id: value.seq
                })
            });
            $scope.getBoats($scope.shipOwner.idUsers);
            angular.element('.cover-img').css({
                'background-image': 'url(\'' + $scope.event.imageURL + '\')'
            });
        }, function (err) {});
    };
    $scope.getEvent();
    $scope.getNumber = function (num) {
        return new Array(num);
    }

    function maxTicketsForBoat(boat) {
        var nBunks, nCabinsWBathroom, nCabinsNBathroom, max;
        nBunks = boat.bunks;
        nCabinsWBathroom = boat.cabinsWithBathroom * 2;
        nCabinsNBathroom = boat.cabinsNoBathroom * 2;
        max = Math.max(nBunks, nCabinsNBathroom, nCabinsWBathroom);
        return max;
    }
    $scope.addTicket = function (row, col) {
        
        $scope.tickets[row][col] = {
            "available": 1
            , "booked": 0
            , "bookedTo": null
            , "cabinRef": 0
            , "description": ""
            , "eventId": $stateParams.eventId
            , "idEventTickets": 0
            , "price": 100
            , "ticketType": row + 1
        }
    }
    $scope.tempTickets = [
                          [
            {
                "eventId": 2
                , "price": 70
                , "ticketType": 1
                            }
            , {
                "eventId": 2
                , "price": 150
                , "ticketType": 1
                            }
                          ]
                          , [
            {
                "eventId": 2
                , "price": 120
                , "ticketType": 2
                            }
                          ]
                        ];
    $scope.tempTickets[0][0]
    $scope.updateTickets = function (ticketTypeIndex, priceIndex, value) {
        $scope.tempTickets[ticketTypeIndex][priceIndex].price = value;
    };
    $scope.deleteImage = function (image) {
        $http.delete(URLs.ddns + 'rest/events/' + $scope.event.idEvents + '/' + image.substring(image.lastIndexOf("ev"))).then(function (res) {
            $scope.images.splice($scope.images.indexOf(image), 1);
        }, function (err) {})
    };
    $scope.saveEvent = function () {
        $scope.tempEvent.categoryId = $scope.event.categoryId;
        $scope.tempEvent.idEvents = $scope.event.idEvents;
        $scope.tempEvent.shipOwnerId = $scope.shipOwner.idUsers;
        $scope.tempEvent.eventType = $scope.event.eventType;
        $scope.tempEvent.dateStart = $scope.event.dateStart;
        $scope.tempEvent.dateEnd = $scope.event.dateEnd;
        $scope.tempEvent.title = $scope.event.title;
        $scope.tempEvent.description = $scope.description;
        $scope.tempEvent.logistics = $scope.logistics;
        $scope.tempEvent.includes = $scope.includes;
        $scope.tempEvent.excludes = $scope.excludes;
        $scope.tempEvent.location = $scope.event.location;
        $scope.tempEvent.imageURL = $scope.event.imageURL;
        $scope.tempEvent.route=$scope.route;
        $scope.tempEvent.participants=$scope.participants;
        $scope.tempEvent.labels = [];
        $scope.tempEvent.tickets = $scope.tickets;
        $scope.tempEvent.boatId = $scope.selectedBoat.idBoats;
        $http.put(URLs.ddns + 'rest/events/' + $scope.event.idEvents, $scope.tempEvent, {
            headers: {
                'Language': $scope.selectedLanguage
            }
        }).then(function (res) {
            toastr.success($translate.instant('edit.events.success.save'));
        }, function (err) {
            toastr.error(err.data.error);
        })
    }
    $scope.uploadFiles = function (files) {
        $scope.files = files;
        if (files && files.length) {
            Upload.upload({
                url: URLs.ddns + 'rest/events/' + $scope.event.idEvents + '/upload'
                , data: {
                    files: files
                }
            }).then(function (response) {
                $timeout(function () {
                    $scope.images = response.data.images;
                    $scope.progress = null;
                });
            }, function (response) {
                if (response.status > 0) {
                    $scope.errorMsg = response.status + ': ' + response.data;
                }
            }, function (evt) {
                $scope.progress = Math.min(100, parseInt(100.0 * evt.loaded / evt.total));
                $('#file-upload-progress').progress({
                    percent: $scope.progress
                });
            });
        }
    };
    $scope.searchLocation = function () {
        $scope.newLocation = {
            latitude: $scope.mapDetails.geometry.location.lat()
            , longitude: $scope.mapDetails.geometry.location.lng()
        };
        $scope.markers[0].coords = $scope.newLocation;
        $scope.map.control.refresh($scope.newLocation);
        $scope.tempEvent.route = [{
            seq: 0
            , description: 'No Description'
            , lat: $scope.newLocation.latitude
            , lng: $scope.newLocation.longitude
            }];
    };
    $scope.setLanguage = function (lang) {
        $scope.selectedLanguage = lang;
        $scope.getEvent();
    };
    angular.element('.ui.language.dropdown').dropdown({
        action: 'activate'
    });
});