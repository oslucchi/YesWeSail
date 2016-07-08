'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:UseridCtrl
 * @description
 * # UseridCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('UseridProfileCtrl', function ($scope, $stateParams, $http, URLs, Session, toastr, $timeout, ngDialog, Upload) {

        $scope.reviews = {};
    
  
    
    
        $scope.tempReview = {
            rating: 0,
            review: '',
            reviewForId: $stateParams.userId
        };
        var updateRate = function (value) {
            $scope.tempReview.rating = value;
        }

        $('.new.star.rating').rating({
            maxRating: 5,
            onRate: function (value) {
                updateRate(value);

            }
        });


        var getUserReviews = function () {
            $http.get(URLs.ddns + 'rest/reviews?reviewForId=' + $stateParams.userId).then(function (res) {
                $scope.reviews = res.data;
                $timeout(function () {
                    $('.comment.rating').rating({
                        readOnly: true
                    }).rating('disable');
                }, 1000);

            }, function (err) {});

        };

        $scope.addReview = function (review) {
            review.reviewerId = Session.getCurrentUser().idUsers;
            $http.post(URLs.ddns + 'rest/reviews', review).then(function (res) {
                toastr.success('Review sent for approval');
            }, function (err) {});
        };


      
    
        getUserReviews();

    });
