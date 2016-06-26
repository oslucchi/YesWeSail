'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:RemovepassengerCtrl
 * @description
 * # RemovepassengerCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
  .controller('RemovePassengerCtrl', function ($scope, $http, URLs, toastr, ngDialog) {
    
    $http.get(URLs.ddns + 'rest/tickets/'+$scope.eventId+'/ticketsSold').then(function(res){
            $scope.tickets=res.data.tickets;
        }, function(err){
            
        });
     $scope.selectTicket=function(ticket){
        if($scope.selectedTicket==ticket){
            $scope.selectedTicket=null;
        }else{
            $scope.selectedTicket=ticket;
        }
        
    };
    
    $scope.removeTicket=function(ticket){
      $http.delete(URLs.ddns+'rest/tickets/'+$scope.selectedTicket.idEventTicketsSold, {}).then(function(res){
          toastr.success('Ticket removed!');
           $scope.closeModals();
      }, function(err){})  
    };
    
    $scope.closeModals=function(){
        ngDialog.closeAll();
    };
  });
