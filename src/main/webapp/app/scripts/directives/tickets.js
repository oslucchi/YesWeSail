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
          
          var totalAmount=0;
          scope.calculatePrice=function(tickets){
              if(tickets[0].ticketType==1){
                  return null;
              }else if(tickets.length>1){
                  totalAmount=tickets[0].price+tickets[1].price
                  return totalAmount;
              }
          CartService.totalAmount=totalAmount;
          };
          
          scope.availableTickets=function(tickets){
                var sum= 0;  
              angular.forEach(tickets, function(value, key){
                    sum+=(value.available-value.booked);
                });  
              return sum;
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
