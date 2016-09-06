'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:CarterrorCtrl
 * @description
 * # CarterrorCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
  .controller('CartCtrl', function ($scope, $http, URLs, $state) {
        $scope.hotEvents=null;
        
        $http.post(URLs.ddns +'rest/events/hotEvents' ).then(function(res){
            $scope.hotEvents=res.data;
        }, function(err){})
        $scope.showAvailableDatesForEvents=function(events){
          $scope.aggregatedEvents=events;
            ngDialog.open({
                template: 'views/aggregatedEvents.html'
                , className: 'ngdialog-theme-default'
                , controller: 'MainCtrl',
                scope: $scope
            });
        };
    $scope.closeModals=function(){
        ngDialog.closeAll();
    };
  
    $scope.processEventClick=function(events){
        
        if(events.length>1){
            $scope.showAvailableDatesForEvents(events);    
        }else if(events.length<2){
            
            $state.go('event',{eventId: events[0].idEvents});
        }
        
        
    }
  });
