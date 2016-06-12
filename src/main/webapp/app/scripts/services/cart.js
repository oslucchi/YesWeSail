'use strict';

/**
 * @ngdoc service
 * @name yeswesailApp.cart
 * @description
 * # cart
 * Service in the yeswesailApp.
 */
angular.module('yeswesailApp')
  .service('CartService', function ($http, URLs, toastr, $cookieStore, $window, Session) {
    var cartService = {};
        cartService.cartQty=null;
        cartService.bookedTickets=[];
        cartService.totalAmount=0;
        cartService.status={
                        paying:false
                    };
            
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
           

            return $http.delete(URLs.ddns + 'rest/cart')
                .then(function (res) {
                    return res;
                });
        };
    
        cartService.requestToken=function (bookedEvents) {
            return $http
                .post(URLs.ddns + 'rest/cart/generateToken', bookedEvents)
                .then(function (res) {
                    return res;
                });
        };
    
        cartService.requestNonceFromBraintree=function (clientToken) {
             braintree.setup(clientToken, "paypal", {
                paypal: {
                        container: 'paypal-container',
                        singleUse: true, // Required
                        amount: cartService.totalAmount, // Required
                        currency: 'EUR' // Required
                       
                },
                onPaymentMethodReceived: function (obj) {
                    cartService.status.paying=true;
                 cartService.checkout(obj.nonce).then(function(res){
                     console.log(res);
                 });
                }
              });
            
            braintree.setup(clientToken, "custom", {
                id: "checkout"
            });
            
            
 
        };
    
    
    
        cartService.checkout = function (nonce) {
            return $http
                .get(URLs.ddns + 'rest/cart/checkout/pp/'+ Session.getCurrentUser().idUsers+'?payment_method_nonce='+nonce)
                .then(function (res) {
                    $window.location.href = '/#/cart/success?transactionId='+res.data.transactionId;    
                    return res;
                }, function(err){
                    $window.location.href = '/#/cart/error?responseCode='+err.data.responseCode;    
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
            return $http.delete(URLs.ddns + 'rest/cart')
                .then(function (res) {
                cartService.cartQty=null;
                
                 $cookieStore.remove('bookedTickets');
                    return res;
                });
        };
    
        
    
        return cartService;
  });
