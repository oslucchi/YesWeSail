'use strict';
/**
 * @ngdoc directive
 * @name yeswesailApp.directive:tickets
 * @description
 * # tickets
 */
angular.module('yeswesailApp').directive('tickets', function ($http, URLs, toastr, CartService, ngDialog, Session, $rootScope, $state, AuthService) {
    return {
        templateUrl: 'views/tickets.html'
        , scope: {
            globalTickets: "="
        }
        , restrict: 'E'
        , link: function postLink(scope, element, attrs) {
            var user = Session.getCurrentUser();
            var totalAmount = 0;
            scope.calculatePrice = function (tickets) {
                if (tickets.length <= 1) {
                    return null;
                }
            
                totalAmount = tickets[0].price + tickets[1].price;
                
                CartService.totalAmount = totalAmount;
                return totalAmount;
            };
            scope.availableTickets = function (tickets) {
                var sum = 0;
                angular.forEach(tickets, function (value, key) {
                    sum += (value.available - value.booked);
                });
                return sum;
            };
            scope.unsold = function (tickets) {
                var sum = 0;
                angular.forEach(tickets, function(ticketType, key)
	        		{
	                    angular.forEach(ticketType, function (value, key) {
	                        sum += (value.available - value.booked);
	                    });
	        		});
                return sum;
            };
            scope.confirmAddToCart = function (tickets, cabinType) {
                AuthService.isAuthenticated().then(function (res) {
                    if (!res) {
                        $rootScope.$broadcast('LoginRequired', $state);
                        return;
                    }
                    else {
                        scope.selectedTickets = tickets;
                        scope.selectedCabinType = cabinType;
                        ngDialog.open({
                            template: 'views/tickets.confirm.html'
                            , className: 'ngdialog-theme-default'
                            , controller: 'TicketsConfirmCtrl'
                            , scope: scope
                        });
                    }
                }, function (err) {})
            };
            scope.buy = CartService.buy;
        }
    };
});