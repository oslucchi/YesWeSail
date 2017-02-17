'use strict';
/**
 * @ngdoc function
 * @name yeswesailApp.controller:UseridCtrl
 * @description
 * # UseridCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp').controller('UseridProfileCtrl', function ($scope, $stateParams, $http, URLs, Session, toastr, $timeout, ngDialog, Upload, $translate) {
    $scope.reviews = {};
    var getUser = function () {
        $http.get(URLs.ddns + 'rest/users/' + $stateParams.userId).then(function (res) {
            if (!!res.data.docs) {
                $scope.user = res.data.user;
                $scope.docs = res.data.docs;
                $('.new.star.rating').rating({
                    maxRating: 5
                    , onRate: function (value) {
                        updateRate(value);
                    }
                });
            }
            else {
                $scope.user = res.data;
            }
        }, function (err) {});
    };
    getUser();
    $scope.updateUser = function (user) {
        $http.put(URLs.ddns + 'rest/users/' + $scope.currentUser.idUsers, user).then(function (res) {
            toastr.success($translate.instant('useridInfo.updateSuccess'));
        }, function (err) {
            toastr.error($translate.instant('global.updateError'), {
                errorMsg: ""
            });
        })
    }
    
        $scope.$watch('currentUser.idUsers', function(dataLoaded) {
          if (dataLoaded) {
              if($stateParams.userId == $scope.currentUser.idUsers){
                  $scope.isOwner=true;
              }
               $scope.isAdmin=Session.isAdmin($scope.currentUser.roleId);
          }
        });
       
    $scope.uploadProfilePic=function(pic){
             Upload.upload({
                url: URLs.ddns + 'rest/users/' + $scope.currentUser.idUsers + '/profilePic',
                data: {
                    picture: pic
                }
            }).then(function (response) {
            	toastr.success('Image set');
                 $scope.user.imageURL=response.data.images[0]+ '?decache=' + Math.random();;
            }, function (response) {

            }, function (evt) {
                $scope.progress =
                    Math.min(100, parseInt(100.0 * evt.loaded / evt.total));
                $('#file-upload-progress').progress({
                    percent: $scope.progress
                });

            });
    }
    
    $scope.tempReview = {
        rating: 0
        , review: ''
        , reviewForId: $stateParams.userId
    };
    var updateRate = function (value) {
        $scope.tempReview.rating = value;
    }
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
        if (review.review) {
            review.reviewerId = Session.getCurrentUser().idUsers;
            $http.post(URLs.ddns + 'rest/reviews', review).then(function (res) {
                toastr.success($translate.instant('userid.addReview', {
                    message: res.data.message
                }), {
                    message: res.data.message
                });
                $scope.tempReview.review = '';
            }, function (err) {});
        }
    };
    getUserReviews();
});