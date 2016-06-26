'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:EventsEventidCtrl
 * @description
 * # EventsEventidCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('EditEventCtrl', function ($scope, $http, URLs, $stateParams, Upload, $timeout, $filter, toastr, $translate, uiGmapIsReady) {
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
    $scope.selectedLanguage='IT';
    $scope.tempEvent={};
    $scope.markers=[];
    $scope.getEvent=function(){
        $http.post(URLs.ddns + 'rest/events/details', {eventId: $stateParams.eventId
        }, {headers: {'Edit-Mode': 'true', 'Language': $scope.selectedLanguage}}).then(function (res) {
            
            $scope.event=res.data.event;
            $scope.event.dateStart=$filter('date')(res.data.event.dateStart, 'yyyy-MM-dd');
            $scope.event.dateEnd=$filter('date')(res.data.event.dateEnd, 'yyyy-MM-dd');
            $scope.shipOwner = res.data.shipOwner;
            $scope.images = res.data.images;
            $scope.tickets = res.data.tickets;
            $scope.participants=res.data.participants;
            $scope.logistics= res.data.logistics;
            $scope.includes= res.data.includes;
            $scope.excludes= res.data.excludes;
            $scope.boat= res.data.boat;
            $scope.description= res.data.description;
            $scope.newLocation={
                    latitude:res.data.route[0].lat,
                    longitude:res.data.route[0].lng
                };
             $scope.map = {
            center: {
                latitude: $scope.newLocation.latitude,
                longitude: $scope.newLocation.longitude
            },
            zoom: 12,
            options: {
                scrollwheel: false
            },
            control: {},
                 events: {
            click: function (map, eventName, originalEventArgs) {
                var e = originalEventArgs[0];
                var lat = e.latLng.lat(),lon = e.latLng.lng();
                var marker = {
                    id: $scope.markers.length,
                    coords: {
                        latitude: lat,
                        longitude: lon
                    }
                };
                $scope.markers.push(marker);
                $scope.$apply();
            }
        }
            
        };
            angular.forEach(res.data.route, function(value, key){
                $scope.markers.push({
                    coords:{
                        latitude: value.lat,
                        longitude: value.lng
                    },
                    id: value.seq
                })
        });
                
            
            
            
            
            angular.element('.cover-img')
                .css({'background-image': 'url(\'' + $scope.event.imageURL +'\')'});
        }, function (err) {});
        
        
    };
        
$scope.getEvent();
    
        $scope.deleteImage=function(image){
               
            $http.delete(URLs.ddns+'rest/events/'+$scope.event.idEvents+'/'+image.substring(image.lastIndexOf("ev"))).then(function(res){
                $scope.images.splice($scope.images.indexOf(image), 1);
            }, function(err){})
        };
        
        $scope.saveEvent=function(){
            
                $scope.tempEvent.categoryId= $scope.event.categoryId,
                $scope.tempEvent.idEvents= $scope.event.idEvents,
                $scope.tempEvent.shipOwnerId=$scope.shipOwner.idUsers,
                $scope.tempEvent.shipId= $scope.boat.idBoats,
                $scope.tempEvent.eventType= $scope.event.eventType,
                $scope.tempEvent.dateStart= $scope.event.dateStart,
                $scope.tempEvent.dateEnd= $scope.event.dateEnd,
                $scope.tempEvent.title= $scope.event.title,
                $scope.tempEvent.description= $scope.description,
                $scope.tempEvent.logistics= $scope.logistics,
                $scope.tempEvent.includes= $scope.includes,
                $scope.tempEvent.excludes= $scope.excludes,
                $scope.tempEvent.location= $scope.event.location,
                $scope.tempEvent.imageURL= $scope.event.imageURL,
                $scope.tempEvent.labels= []
            
            
            $http.put(URLs.ddns+'rest/events/'+$scope.event.idEvents, $scope.tempEvent, {headers: {'Language': $scope.selectedLanguage}}).then(function(res){
                toastr.success($translate.instant('edit.events.success.save'));
            }, function(err){})
            
            
            
        }
     
                
           
      
         $scope.uploadFiles = function (files) {
        $scope.files = files;
        if (files && files.length) {
            Upload.upload({
                url: URLs.ddns+'rest/events/'+$scope.event.idEvents+'/upload',
                data: {
                    files: files
                }
            }).then(function (response) {
                $timeout(function () {
                    $scope.images=response.data.images;
                    $scope.progress=null;
                });
            }, function (response) {
                if (response.status > 0) {
                    $scope.errorMsg = response.status + ': ' + response.data;
                }
            }, function (evt) {
                $scope.progress = 
                    Math.min(100, parseInt(100.0 * evt.loaded / evt.total));
                  $('#file-upload-progress').progress({
                      percent: $scope.progress
                    });
            });
        }
    };
        
        
        
          
        
      
        $scope.searchLocation=function(){
            $scope.newLocation={latitude: $scope.mapDetails.geometry.location.lat(), longitude: $scope.mapDetails.geometry.location.lng()};
            $scope.markers[0].coords=$scope.newLocation;
            $scope.map.control.refresh($scope.newLocation);
            $scope.tempEvent.route=[{
                seq: 0,
                description: 'No Description',
                lat: $scope.newLocation.latitude,
                lng: $scope.newLocation.longitude
            }];
        };
        
        
       
        $scope.setLanguage=function(lang){
            $scope.selectedLanguage=lang;
            $scope.getEvent();
        };
    
        angular.element('.ui.selection.dropdown').dropdown();
    });