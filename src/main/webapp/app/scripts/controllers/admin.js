'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
  .controller('AdminCtrl', function ($scope, $http, URLs, MAPS, lodash, toastr ) {
            
    
    
    $scope.getEvents=function(){
        $http.post(URLs.ddns + 'rest/events/search/all', {}).then(function(res){
            $scope.events=res.data;
        });
    };
    
    $scope.getEvents();
    
    $scope.activate=function(event){
        event.status='A';
        $http.put(URLs.ddns + 'rest/events/activate', event).then(function(res){
            toastr.success('Event correctly activated!');
        }, function(err){
            toastr.error('Something went wrong while trying to activate the event, maybe the hamsters ran away!');
        });
    }
    $scope.deactivate=function(event){
        event.status='P';
        $http.put(URLs.ddns + 'rest/events/activate', event).then(function(res){
            toastr.success('Event correctly deactivated!');
        }, function(err){
            toastr.error('Something went wrong while trying to activate the event, maybe the hamsters ran away!');
        });
    }
    $scope.remove=function(event){
         $http.delete(URLs.ddns + 'rest/events/delete/'+ event.idEvents).then(function(res){
            toastr.success('Event deleted!');
             $scope.events.splice($scope.events.indexOf(event),1);
             
        }, function(err){
            toastr.error('Something went wrong while trying to delete the event, the gods chose to spare this event!');
        });
    }
    $scope.clone=function(event){
        $http.post(URLs.ddns + 'rest/events/clone', event).then(function(res){
            toastr.success('Event '+event.idEvents+' duplicated! New ID '+res.data.idEvents);
        }, function(err){
            toastr.error('Something went wrong while trying to duplicate the event, the monkeys are probably on strike again!');
        });
    }
    
       
  });
