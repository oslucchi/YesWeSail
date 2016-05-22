'use strict';

/**
 * @ngdoc directive
 * @name yeswesailApp.directive:tickets
 * @description
 * # tickets
 */
angular.module('yeswesailApp')
  .directive('tickets', function ($http, URLs, toastr, CartService) {
    
    return {
        
      templateUrl: 'views/tickets.html',
        scope:{
                globalTickets:"="
        },
      restrict: 'E',
      link: function postLink(scope, element, attrs) {
          

          scope.calculatePrice=function(tickets){
              if(tickets[0].ticketType==1){
                  return null;
              }else if(tickets.length>1){
                  return tickets[0].price+tickets[1].price;
              }
          };
          
          scope.addToCart=function(tickets, cabinType){
              CartService.addToCart(tickets, cabinType).then(function(res){
                  scope.globalTickets=res.data;
              });
              
          };
          scope.buy=CartService.buy;
          
      }
    };
  });