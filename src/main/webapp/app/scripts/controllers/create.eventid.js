'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:EventsEventidCtrl
 * @description
 * # EventsEventidCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('EditEventCtrl', function ($scope, $http, URLs, $stateParams, Upload, $timeout, $filter, toastr, $translate) {
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
            
            $scope.tempEvent={
                categoryId: $scope.event.categoryId,
                eventId: $scope.event.idEvents,
                shipOwnerId:$scope.shipOwner.idUsers,
                shipId: $scope.boat.idBoats,
                eventType: $scope.event.eventType,
                dateStart: $scope.event.dateStart,
                dateEnd: $scope.event.dateEnd,
                title: $scope.event.title,
                description: $scope.description,
                logistics: $scope.logistics,
                includes: $scope.includes,
                excludes: $scope.excludes,
                location: $scope.event.location,
                imageURL: $scope.event.imageURL,
                labels: []
            }; 
            
            
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
            $scope.map.control.refresh({latitude: $scope.mapDetails.geometry.location.lat(), longitude: $scope.mapDetails.geometry.location.lng()});
        };
        
        
        $scope.map = {
            center: {
                latitude: 45.27,
                longitude: 9.11
            },
            zoom: 15,
            options: {
                scrollwheel: false
            },
            control: {}
            
        };
        $scope.setLanguage=function(lang){
            $scope.selectedLanguage=lang;
            $scope.getEvent();
        };
    
        angular.element('.ui.selection.dropdown').dropdown();
    });