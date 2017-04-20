'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:LoginCtrl
 * @description
 * # LoginCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('CreateEventCtrl', function ($scope, ngDialog, $window, $http, URLs, MAPS, AuthService, Session) {
        $scope.tempEvent = {
          title: "",
          eventType: 1,
          shipOwnerId: "",
          categoryId: 1,
          eventId: null,
          shipId: 1,
          dateStart: null,
          dateEnd: null,
          location: null,
          description: null,
          logistics: null,
          includes: null,
          excludes: null,
          notes: null,
          imageUrl: null,
            route: [{
                lat: 42.47,
                lng: 12.36
            }],
          labels: []
        };
    $scope.isAuthorized=AuthService.isAuthorized;
    
        $scope.create = function (temporaryEvent) {
            
            if(!!!$scope.suggestedOwner){
                temporaryEvent.shipOwnerId=Session.getCurrentUser().idUsers;
                temporaryEvent.eventType=$scope.EVENTTYPES[0].idEventTypes;
                
            }else{
                temporaryEvent.shipOwnerId=$scope.suggestedOwner.originalObject.idUsers;
            }
            $http.post(URLs.ddns + 'rest/events/create', temporaryEvent).then(function (res) {
                $window.location.href = '/edit-event/'+res.data.event.idEvents;
                 $scope.createError=null;
                //$window.location.reload();
                ngDialog.closeAll();                
            }, function (err) {
                $scope.createError=err.data.error;
                console.log(err);
            });
        };
    
    
        $scope.EVENTTYPES=[];
        $scope.CATEGORIES=[];
        
        MAPS.getMap('EVENTTYPES').then(function(res){
                     $scope.EVENTTYPES = res;   
        }   
        );
        
        MAPS.getMap('CATEGORIES').then(function(res){
                     $scope.CATEGORIES = res;   
        }   
        );
    
    
        
    
        $http.get(URLs.ddns + 'rest/users/suggestion/6').then(function(res){
            $scope.shipOwners=res.data;
        }, function(err){
            
        });

    $scope.closeModals=function(){
      ngDialog.closeAll();  
    };
    
   $scope.initializeSelect = function () {
            angular.element('.ui.selection.dropdown').dropdown();
        };
     angular.element('.ui.type.dropdown').dropdown();
    });