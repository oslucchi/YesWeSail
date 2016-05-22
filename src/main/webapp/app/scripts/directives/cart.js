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
//          
        scope.bookedTickets=CartService.bookedTickets;
        console.log(scope.bookedTickets)
        scope.buy=CartService.buy;
      }
    };
  });
