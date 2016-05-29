'use strict';

/**
 * @ngdoc directive
 * @name yeswesailApp.directive:tickets
 * @description
 * # tickets
 */
angular.module('yeswesailApp')
  .directive('cart', function ($http, URLs, toastr, CartService, Session) {
    
    return {
        
      templateUrl: 'views/cart.template.html',
        scope:{
               
        },
      restrict: 'E',
      link: function postLink(scope, element, attrs) {
               CartService.cartQty=null;
          scope.total=0;
          scope.getTotal=function(bookedTickets){
              
              angular.forEach(bookedTickets, function(value, key){
                if(value.toBuy){
                    scope.total+=value.price;
                }
            });
              
              
          }
          scope.recalculateTotal=function(){
            scope.total=0;
            angular.forEach(scope.bookedEvents, function(value, key){
                    scope.getTotal(value.tickets);  
              })
          }
          CartService.getAllItems().then(function(res){
              scope.bookedEvents=CartService.bookedTickets;
              scope.total=0;
              
              angular.forEach(scope.bookedEvents, function(value, key){
                    CartService.cartQty+=value.tickets.length;
                    scope.getTotal(value.tickets);  
              })
              
          });
          
          scope.emptyCart=function(){
              
              CartService.emptyCart().then(function(res){
                  scope.bookedEvents=[];
              scope.total=0;
              });
          }
          scope.paymentProceeded=true;
          scope.inProgress=false;
          
          
        scope.checkout=function(obj){
            console.log(obj);
        }
          
        scope.requestToken=function(){
            scope.user=Session.getCurrentUser();
            scope.checkoutUrl='http://yeswesail.ddns.net:8080/YesWeSail/rest/cart/checkout/'+scope.user.idUsers;
            scope.inProgress=true;
            CartService.requestToken(scope.bookedEvents).then(function(res){
                scope.paymentProceeded=false;
                CartService.requestNonceFromBraintree(res.data);
            });
        };
        
      }
    };
  });
