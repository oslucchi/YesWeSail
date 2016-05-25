'use strict';

/**
 * @ngdoc service
 * @name yeswesailApp.cart
 * @description
 * # cart
 * Service in the yeswesailApp.
 */
angular.module('yeswesailApp')
  .service('CartService', function ($http, URLs, toastr, $cookieStore) {
    var cartService = {};
        cartService.cartQty=null;
        cartService.bookedTickets=[];
       
        
            
        cartService.addToCart = function(tickets, cabinType){
             var ticketArray=[];
            
              if(cabinType=='single'){
                  ticketArray.push(tickets[0]);
              }else if(cabinType=='double'){
                  ticketArray.push(tickets[0]);
                  ticketArray.push(tickets[1]);
              }
              
             var promise= $http.post(URLs.ddns + 'rest/tickets/bookTicket', ticketArray).then(function(res){
                  cartService.cartQty++;
                  cartService.availableTickets=res.data;
                  toastr.success('Ticket added to cart!')
                  cartService.bookedTickets.push(ticketArray);
                  $cookieStore.put('bookedTickets', cartService.bookedTickets);
                  return res;
              });
               return promise;  
          };

        cartService.removeFromCart = function (ticket) {
           

            return $http
                .post(URLs.ddns + 'rest/auth/logout', {
                    token: $rootScope.globals.currentUser.token
                })
                .then(function (res) {
                    Session.destroy();
                    return null;
                });
        };
    
        cartService.requestToken=function () {
            return $http
                .get(URLs.ddns + 'rest/cart/generateToken')
                .then(function (res) {
                    return res;
                });
        };
    
        cartService.requestNonceFromBraintree=function (clientToken) {
             braintree.setup(clientToken, "custom", {
                paypal: {
                        container: 'paypal-container',
                        singleUse: true, // Required
                        amount: 5.00, // Required
                        currency: 'EUR', // Required
                        locale: 'it_it',
                        enableShippingAddress: true,
                        shippingAddressOverride: {
                          recipientName: 'Scruff McGruff',
                          streetAddress: '1234 Main St.',
                          extendedAddress: 'Unit 1',
                          locality: 'Chicago',
                          countryCodeAlpha2: 'US',
                          postalCode: '60652',
                          region: 'IL',
                          phone: '123.456.7890',
                          editable: false
                        },
                },
                onPaymentMethodReceived: function (obj) {
                 cartService.checkout(obj.nonce).then(function(res){
                     console.log(res);
                 });
                }
              });
            
            braintree.setup(clientToken, "custom", {id: "checkout"});
            
            
          
            
            
            
        };
    
    
    
        cartService.checkout = function (nonce) {
            return $http
                .get(URLs.ddns + 'rest/cart/checkout?nonce='+nonce)
                .then(function (res) {
                    return res;
                });
        };

        cartService.getAllItems = function () {
             return $http
                .get(URLs.ddns + 'rest/cart')
                .then(function (res) {
                 cartService.bookedTickets=res.data;
                    return res;
                });
        };

        cartService.emptyCart = function () {
            if(Session.userProfile){

                if(roleId<=Session.userProfile.roleId){
                    return true;
                }   
            }
        };
    
        
    
        return cartService;
  });
