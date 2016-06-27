'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:AddpassengerCtrl
 * @description
 * # AddpassengerCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
  .controller('AddPassengerCtrl', function ($scope, ngDialog, $http, URLs, toastr) {
    
    $scope.user={
    };
    $scope.existingUser=null;
    $scope.newUser=false;
    $http.get(URLs.ddns + 'rest/users/suggestion/3').then(function(res){
            $scope.allUsers=res.data;
        }, function(err){
            
        });
    
    $http.get(URLs.ddns + 'rest/tickets?eventId='+$scope.eventId).then(function(res){
            $scope.tickets=res.data.tickets;
        }, function(err){
            
        });
    
    
     $scope.addPassenger=function(eventId, ticket, newUser){
        var user;
         if(newUser){
            user=$scope.user;
        }else{
            user=$scope.existingUser.originalObject;
        }
         
         $http.post(URLs.ddns+'rest/events/passengers', {
             usersId: user.idUsers,
             userName: user.name,
             userSurname: user.surname,
             userEmail: user.email,
             eventsId: eventId,
             idEventTickets: ticket.idEventTickets
         }).then(function(res){
              $scope.closeModals();
             toastr.success('User added!');
             
         },function(err){
             toastr.error('Failed to add user!');
         })
     };
    
    $scope.selectTicket=function(ticket){
        if($scope.selectedTicket==ticket){
            $scope.selectedTicket=null;
        }else{
            $scope.selectedTicket=ticket;
        }
        
    };
    
    
      $scope.closeModals=function(){
        ngDialog.closeAll();
    };
  });
