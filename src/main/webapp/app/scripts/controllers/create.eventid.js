'use strict';
/**
 * @ngdoc function
 * @name yeswesailApp.controller:EventsEventidCtrl
 * @description
 * # EventsEventidCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp').controller('EditEventCtrl', function ($scope, $http, $rootScope, $state, URLs, AuthService, $stateParams, Upload, $timeout, $filter, toastr, $translate, uiGmapIsReady, LocaleService, $q) {
    //    angular.element('.ui.anchor-menu').sticky({
    //        context: '#event-container'
    //        , offset: 60
    //    });
    //    angular.element('.ui.book').sticky({
    //        context: '#event-container'
    //        , offset: 105
    //    });
    var bDisabled = false;
    $scope.tDisabled = false;
    $scope.minDate = new Date();
    var unsavedWork = false;
    
      AuthService.isAuthenticated().then(function (res) {
                    if (!res) {
                        $rootScope.$broadcast('LoginRequired', $state);
                        $state.go('main');
                        return;
                    }
                }, function (err) {})
    
    $scope.$on('$stateChangeStart', function (event, next, current) {
        if (unsavedWork) {
            var answer = confirm($translate.instant('global.leaveMessage'));
            if (!answer) {
                event.preventDefault();
            }
            else {
                $scope.saveEvent()
            }
        }
    });
    $scope.selectedLanguage = LocaleService.getCurrentLocale();
    $scope.tempEvent = {};
    $scope.markers = [];
    $scope.selectedBoat = null;
    $scope.getBoats = function (userId) {
        $http.get(URLs.ddns + 'rest/users/shipowners/' + userId + '/boats').then(function (res) {
            $scope.boats = res.data.boats;
            if (!$scope.selectedBoat) {
                $scope.setSelectedBoat(res.data.boats[0]);
            }
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
    };
    $scope.getEvent = function () {
        $http.post(URLs.ddns + 'rest/events/details', {
            eventId: $stateParams.eventId
        }, {
            headers: {
                'Edit-Mode': 'true'
                , 'Language': $scope.selectedLanguage
            }
        }).then(function (res) {
            
            $("#cover-img").css('background-image', 'url("'+res.data.event.imageURL+'")')
            $scope.event = res.data.event;
            if (res.data.event.dateStart == -3600000) {
                $scope.event.dateStart = $scope.minDate;
            }
            if (res.data.event.dateEnd == -3600000) {
                $scope.event.dateEnd = $scope.minDate;
            }
            $scope.event.dateStart = $filter('date')(res.data.event.dateStart, 'yyyy-MM-dd');
            $scope.event.dateEnd = $filter('date')(res.data.event.dateEnd, 'yyyy-MM-dd');
            $scope.shipOwner = res.data.shipOwner;
            $scope.images = res.data.images;
            posY = res.data.event.backgroundOffsetY;
            angular.element('.cover-img').css('background-position-y', posY + 'px');
            $scope.imagesSmall = res.data.imagesSmall;
            $scope.imagesMedium = res.data.imagesMedium;
            $scope.imagesLarge = res.data.imagesLarge;
            $scope.tickets = res.data.tickets;
            $scope.selectedLanguage=res.data.event.locale;
            $scope.participants = res.data.participants;
            $scope.logistics = res.data.logistics;
            $scope.includes = res.data.includes;
            $scope.route = res.data.route;
            $scope.excludes = res.data.excludes;
            $scope.description = res.data.description;
            if (res.data.boat.idBoats !== 1) {
                $scope.setSelectedBoat(res.data.boat);
            }
            if (res.data.boat.boatId != 1) {
                bDisabled = true;
            }
            else {
                bDisabled = false;
            }
            
          
            
            if (res.data.tickets.length > 0) {
                $scope.tempTickets=res.data.tickets;
                $scope.tDisabled = true;
            }
            else {
                  $scope.tempTickets = [
//            [{
//                            "available": 1
//                            , "booked": 0
//                            , "bookedTo": null
//                            , "cabinRef": null
//                            , "description": ""
//                            , "eventId": $stateParams.eventId
//                            , "idEventTickets": 0
//                            , "price": 100
//                            , "ticketType": 1
//            }]
//                              , [{
//                            "available": 1
//                            , "booked": 0
//                            , "bookedTo": null
//                            , "cabinRef": null
//                            , "description": ""
//                            , "eventId": $stateParams.eventId
//                            , "idEventTickets": 0
//                            , "price": 100
//                            , "ticketType": 2
//            }]
//                              , [{
//                            "available": 1
//                            , "booked": 0
//                            , "bookedTo": null
//                            , "cabinRef": null
//                            , "description": ""
//                            , "eventId": $stateParams.eventId
//                            , "idEventTickets": 0
//                            , "price": 100
//                            , "ticketType": 3
//            }]
//                              , [{
//                            "available": 1
//                            , "booked": 0
//                            , "bookedTo": null
//                            , "cabinRef": null
//                            , "description": ""
//                            , "eventId": $stateParams.eventId
//                            , "idEventTickets": 0
//                            , "price": 100
//                            , "ticketType": 5
//            }]
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
                , zoom: 12
                , options: {
                    scrollwheel: false
                }
                , control: {}
                , events: {
                    click: function (map, eventName, originalEventArgs) {
                        var e = originalEventArgs[0];
                        var lat = e.latLng.lat()
                            , lon = e.latLng.lng();
                        var description = prompt($translate.instant('global.enterDescription'));
                        if (!!description) {
                            var marker = {
                                id: $scope.markers.length
                                , coords: {
                                    latitude: lat
                                    , longitude: lon
                                }
                                , description: description
                                , options: {
                                    draggable: true
                                    , icon: addComplexMarker($scope.markers.length).then(function (image) {
                                        return image.url;
                                    })
                                }
                                , click: function (marker) {
                                    var description = prompt($translate.instant('global.enterDescription'), $scope.markers[marker.key].description);
                                    if (description) {
                                        $scope.markers[marker.model.idKey].description = description;
                                    }
                                }
                                , events: {
                                    rightclick: function (marker) {
                                        $scope.markers.splice(marker.model.idKey, 1);
                                        angular.forEach($scope.markers, function (val, key) {
                                            $scope.markers[key].id = key;
                                        })
                                    }
                                }
                            };
                            $scope.markers.push(marker);
                            $scope.$apply();
                        }
                    }
                }
            };
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
                            draggable: true
                            , icon: '/images/spotlight-poi-green.png'
                        }
                        , click: function (marker) {
                            var description = prompt($translate.instant('global.enterDescription'), $scope.markers[marker.key].description);
                            if (description) {
                                $scope.markers[marker.model.idKey].description = description;
                            }
                        }
                        , events: {
                            rightclick: function (marker) {
                                $scope.markers.splice(marker.model.idKey, 1);
                                angular.forEach($scope.markers, function (val, key) {
                                    $scope.markers[key].id = key;
                                })
                            }
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
                            draggable: true
                        }
                        , click: function (marker) {
                            var description = prompt($translate.instant('global.enterDescription'), $scope.markers[marker.key].description);
                            if (description) {
                                $scope.markers[marker.model.idKey].description = description;
                            }
                        }
                        , events: {
                            rightclick: function (marker) {
                                $scope.markers.splice(marker.model.idKey, 1);
                                angular.forEach($scope.markers, function (val, key) {
                                    $scope.markers[key].id = key;
                                })
                            }
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
                            draggable: true
                            , icon: '/images/spotlight-poi-blue.png'
                        }
                        , click: function (marker) {
                            var description = prompt($translate.instant('global.enterDescription'), $scope.markers[marker.key].description);
                            if (description) {
                                $scope.markers[marker.model.idKey].description = description;
                            }
                        }
                    })
                }
            });
            $scope.getBoats($scope.shipOwner.idUsers);
            angular.element('.cover-img').css({
                'background-image': 'url(\'' + $scope.event.imageURL + '\')'
            });
            angular.element('#slick-demo').slick({
                slidesToShow: 3
                , slidesToScroll: 3
            });
            angular.element('#slick-demo').slickLightbox({
                src: 'src'
                , images: $scope.imagesLarge
                , itemSelector: '.item'
            });
        }, function (err) {});
    };
    $scope.getEvent();
    $scope.getNumber = function (num) {
        if (num) {
            return new Array(num);
        }
        else {
            return;
        }
    }

    function addComplexMarker(label) {
        var canvas = document.createElement('canvas');
        var context = canvas.getContext("2d");
        var imageObj = new Image();
        imageObj.src = "/images/map-marker.png";
        return $q(function (resolve, reject) {
            imageObj.onload = function () {
                context.drawImage(imageObj, 0, 0);
                //Adjustable parameters
                context.font = "40px Arial";
                context.fillText(label, 17, 55);
                //End
                var image = {
                    url: canvas.toDataURL()
                    , size: new google.maps.Size(80, 104)
                    , origin: new google.maps.Point(0, 0)
                    , anchor: new google.maps.Point(40, 104)
                };
                // the clickable region of the icon.
                var shape = {
                    coords: [1, 1, 1, 104, 80, 104, 80, 1]
                    , type: 'poly'
                };
                resolve(image);
            };
        })
    };

    function maxTicketsForBoat(boat) {
        var nBunks, nCabinsWBathroom, nCabinsNBathroom, max;
        nBunks = boat.bunks;
        nCabinsWBathroom = boat.cabinsWithBathroom * 2;
        nCabinsNBathroom = boat.cabinsNoBathroom * 2;
        max = Math.max(nBunks, nCabinsNBathroom, nCabinsWBathroom);
        return max;
    }
    $scope.addTicket = function (row, col) {
        $scope.tempTickets[row]=$scope.tempTickets[row] || [];
        $scope.tempTickets[row][col] = {
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
    };
    $scope.setImageAsDefault = function (image) {
        $http.post(URLs.ddns + 'rest/events/images/default', {
            eventId: $scope.event.idEvents
            , imageURL: image
        }).then(function (res) {
            toastr.success('Image set as default');
            $scope.event.imageURL = image.replace('small', 'large');
        })
    };
//    $scope.tempTickets = [
//                          [
//            {
//                "eventId": 2
//                , "price": 70
//                , "ticketType": 1
//                            }
//            , {
//                "eventId": 2
//                , "price": 150
//                , "ticketType": 1
//                            }
//                          ]
//                          , [
//            {
//                "eventId": 2
//                , "price": 120
//                , "ticketType": 2
//                            }
//                          ]
//                        ];
    // $scope.tempTickets[0][0]
    $scope.updateTickets = function (ticketTypeIndex, priceIndex, value) {
        $scope.tempTickets[ticketTypeIndex][priceIndex].price = value;
    };
    $scope.deleteImage = function (image) {
        $http.delete(URLs.ddns + 'rest/events/' + $scope.event.idEvents + '/' + image.substring(image.lastIndexOf("ev"))).then(function (res) {
            $scope.images.splice($scope.images.indexOf(image), 1);
        }, function (err) {})
    };
    $scope.saveEvent = function () {
        if (confirm($translate.instant('global.confirm.eventChanges'))) {
            $scope.tempEvent.categoryId = $scope.event.categoryId;
            $scope.tempEvent.idEvents = $scope.event.idEvents;
            $scope.tempEvent.shipOwnerId = $scope.shipOwner.idUsers;
            $scope.tempEvent.eventType = $scope.event.eventType;
            $scope.tempEvent.dateStart = $scope.event.dateStart;
            $scope.tempEvent.dateEnd = $scope.event.dateEnd;
            $scope.tempEvent.title = $scope.event.title;
            $scope.tempEvent.description = $scope.description;
            $scope.tempEvent.backgroundOffsetY=posY;
            $scope.tempEvent.logistics = $scope.logistics;
            $scope.tempEvent.includes = $scope.includes;
            $scope.tempEvent.excludes = $scope.excludes;
            $scope.tempEvent.location = $scope.event.location;
            $scope.tempEvent.imageURL = $scope.event.imageURL;
            $scope.tempEvent.route = [];
            $scope.tempEvent.participants = $scope.participants;
            $scope.tempEvent.labels = [];
            $scope.tempEvent.tickets = $scope.tempTickets;
            $scope.tempEvent.boatId = $scope.selectedBoat.idBoats;
            angular.forEach($scope.markers, function (val, key) {
                $scope.tempEvent.route.push({
                    "description": val.description
                    , "eventId": $scope.event.idEvents
                    , "lat": val.coords.latitude
                    , "lng": val.coords.longitude
                    , "seq": val.id
                })
            })
            $http.put(URLs.ddns + 'rest/events/' + $scope.event.idEvents, $scope.tempEvent, {
                headers: {
                    'Language': $scope.selectedLanguage
                }
            }).then(function (res) {
                unsavedWork = false;
                toastr.success($translate.instant('createeventid.saved'));
            }, function (err) {
                toastr.error($translate.instant('global.updateError', {
                    errorMsg: err.data.error
                }));
            })
        }
    };
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
                    $scope.imagesSmall = response.data.imagesSmall;
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
    var posY;
    $scope.calculateBackgroundPositionUp = function (howMuch) {
        //        $scope.backgroundPositionY=curPosition
        posY = posY - howMuch;
        angular.element('.cover-img').css('background-position-y', posY + 'px');
    }
    $scope.calculateBackgroundPositionDown = function (howMuch) {
        //        $scope.backgroundPositionY=curPosition
            posY = posY + howMuch;
            angular.element('.cover-img').css('background-position-y', posY + 'px');
    }
    $scope.searchLocation = function () {
        $scope.newLocation = {
            latitude: $scope.mapDetails.geometry.location.lat()
            , longitude: $scope.mapDetails.geometry.location.lng()
        };
        $scope.map.center.latitude = $scope.mapDetails.geometry.location.lat()
        $scope.map.center.longitude = $scope.mapDetails.geometry.location.lng()
        $scope.map.control.refresh($scope.newLocation);
        $scope.markers[0] = {
            id: 0
            , coords: $scope.newLocation
            , description: 'Sailing point'
            , options: {
                draggable: true
                , icon: addComplexMarker($scope.markers.length).then(function (image) {
                    return image.url;
                })
            }
            , click: function (marker) {
                var description = prompt($translate.instant('global.enterDescription'), $scope.markers[marker.key].description);
                if (description) {
                    $scope.markers[marker.model.idKey].description = description;
                }
            }
            , events: {
                rightclick: function (marker) {
                    $scope.markers.splice(marker.model.idKey, 1);
                    angular.forEach($scope.markers, function (val, key) {
                        $scope.markers[key].id = key;
                    })
                }
            }
        };
    };
    $scope.setLanguage = function (lang) {
        if (unsavedWork) {
            var answer = confirm($translate.instant('global.changeLanguage'));
            if (!answer) {
                event.preventDefault();
            }
            else {
                $scope.saveEvent();
                $scope.selectedLanguage = lang;
                $scope.getEvent();
                unsavedWork = false;
            }
        }
        else {
            $scope.selectedLanguage = lang;
            $scope.getEvent();
            unsavedWork = false;
        }
    };
    angular.element('.ui.language.dropdown').dropdown({
        action: 'activate'
    });
    angular.element('#slick-boat').slick({
        slidesToShow: 1
        , slidesToScroll: 1
    });
    $scope.$watch('selectedBoat', function (boat) {
            if (boat) {
                angular.element('#slick-boat').slickLightbox({
                    src: 'src'
                    , images: boat.images
                    , itemSelector: '.item'
                });
            }
        })
        //     $scope.tempEvent.description = $scope.description;
        //        $scope.tempEvent.logistics = $scope.logistics;
        //        $scope.tempEvent.includes = $scope.includes;
        //        $scope.tempEvent.excludes = $scope.excludes;
        //        $scope.tempEvent.location = $scope.event.location;
        //        $scope.tempEvent.imageURL = $scope.event.imageURL;
        //        $scope.tempEvent.route=$scope.route;
        //        $scope.tempEvent.participants=$scope.participants;
        //        $scope.tempEvent.labels = [];
        //        $scope.tempEvent.tickets = $scope.tickets;
        //        $scope.tempEvent.boatId = $scope.selectedBoat.idBoats;
        //  
    function isArrayContentUndefined(array) {
        for (var i = 0; i < array.length; i++) {
            if (array[i] != undefined) {
                return false;
            }
        }
        return true;
    }
    $scope.$watchCollection('[event, event.location, event.dateStart, event.dateEvent, event.title, description, logistics, includes, excludes, route, participants, tickets, seletedBoat]', function (newVal, oldVal, scope) {
        if (newVal != oldVal && !isArrayContentUndefined(oldVal) && newVal[0].languageId === oldVal[0].languageId) {
            unsavedWork = true;
        }
    })
});