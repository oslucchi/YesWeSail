'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:LoginCtrl
 * @description
 * # LoginCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('TicketsConfirmCtrl', function ($scope, CartService, ngDialog, $window, $http, URLs, MAPS, AuthService, Session) {
    
      $scope.addToCart=function(tickets, cabinType){
              CartService.addToCart(tickets, cabinType).then(function(res){
                  $scope.$parent.globalTickets=res.data;
                  $scope.closeModals();
              });
              
          };
    
    $scope.closeModals=function(){
        ngDialog.closeAll();
    };
    
    
    });