'use strict';

/**
 * @ngdoc directive
 * @name yeswesailApp.directive:tickets
 * @description
 * # tickets
 */
angular.module('yeswesailApp')
  .directive('cart', function ($http, URLs, toastr, CartService) {
    
    return {
        
      templateUrl: 'views/cart.template.html',
        scope:{
               
        },
      restrict: 'E',
      link: function postLink(scope, element, attrs) {
//          CartService.getAllItems().then(function(res){
//              scope.bookedTickets=CartService.bookedTickets;
//          });
          scope.paymentProceeded=true;
          scope.inProgress=false;
          
          
        scope.bookedTickets=CartService.bookedTickets;
        scope.checkout=function(obj){
            console.log(obj);
        }
          
        scope.requestToken=function(){
            scope.inProgress=true;
            CartService.requestToken().then(function(res){
                scope.paymentProceeded=false;
                CartService.requestNonceFromBraintree(res.data);
            });
        };
        
      }
    };
  });
