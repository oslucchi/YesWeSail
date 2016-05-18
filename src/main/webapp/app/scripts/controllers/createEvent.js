'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:LoginCtrl
 * @description
 * # LoginCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('CreateEventCtrl', function ($scope, ngDialog, $window, $http, URLs, MAPS) {
        $scope.tempEvent = {
          title: "",
          eventType: "",
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
          labels: []
        };
    
        $scope.create = function (temporaryEvent) {
            temporaryEvent.shipOwnerId=$scope.suggestedOwner.originalObject.idUsers;
            $http.post(URLs.ddns + 'rest/events/create', temporaryEvent).then(function (res) {
                $window.location.href = '/#/edit-event/'+res.data.idEvents;
                //$window.location.reload();
                ngDialog.closeAll();                
            }, function (err) {
                console.log(err);
            });
        };
        $scope.EVENTTYPES=[];
        
        MAPS.getMap('EVENTTYPES').then(function(res){
                     $scope.EVENTTYPES = res;   
        }   
        );
    
        $http.get(URLs.ddns + 'rest/users/suggestion/2').then(function(res){
            $scope.shipOwners=res.data;
        }, function(err){
            
        });

    
    
   $scope.initializeSelect = function () {


            angular.element('.ui.selection.dropdown').dropdown();



        };
     angular.element('.ui.type.dropdown').dropdown();
    });