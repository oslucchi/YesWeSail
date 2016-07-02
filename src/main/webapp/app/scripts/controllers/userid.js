'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:UseridCtrl
 * @description
 * # UseridCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('UseridCtrl', function ($scope, $stateParams, $http, URLs, Session, toastr, $timeout, ngDialog, Upload) {

        $scope.user = {};
        $scope.reviews = {};
        $scope.tempReview = {
            rating: 0,
            review: '',
            reviewForId: $stateParams.userId
        };
        var updateRate = function (value) {
            $scope.tempReview.rating = value;
        }

        $('.rating').rating({
            maxRating: 5,
            onRate: function (value) {
                updateRate(value);

            }
        });


        var getUser = function () {
            $http.get(URLs.ddns + 'rest/users/' + $stateParams.userId).then(function (res) {
                $scope.user = res.data.user;
                $scope.getBoats();

            }, function (err) {});

        };

        var getUserReviews = function () {
            $http.get(URLs.ddns + 'rest/reviews?reviewForId=' + $stateParams.userId).then(function (res) {
                $scope.reviews = res.data;
                $timeout(function () {
                    $('.comment.rating').rating({
                        readOnly: true
                    }).rating('disable');
                }, 100);

            }, function (err) {});

        };

        $scope.addReview = function (review) {
            review.reviewerId = Session.getCurrentUser().idUsers;
            $http.post(URLs.ddns + 'rest/reviews', review).then(function (res) {
                toastr.success('Review sent for approval');
            }, function (err) {});

        };


        $scope.getBoats = function () {
            $http.get(URLs.ddns + 'rest/users/shipowners/' + $scope.user.idUsers + '/boats').then(function (res) {
                $scope.boats = res.data.boats;
            });
        };


        getUser();
        getUserReviews();

        $scope.tempBoat = {
            info: {
                bunks: 0,
                cabinsNoBathroom: 0,
                cabinsWithBathroom: 0,
                engineType: null,
                idBoats: 0,
                insurance: null,
                length: 0,
                model: '',
                name: '',
                ownerId: 0,
                plate: '',
                RTFLicense: null,
                securityCertification: null,
                sharedBathrooms: 0,
                year: 1970
            },
            files: {
                docs: [],
                bluePrints: [],
                other: []

            }
        };

        $scope.addDocs = function (files) {
            $scope.tempBoat.files.docs = files;
        };
        $scope.addBluePrints = function (files) {
            $scope.tempBoat.files.bluePrints = files;
        };
        $scope.addOtherImages = function (files) {
            $scope.tempBoat.files.other = files;
        };

        $scope.sendBoat = function (boat) {
          

            Upload.upload({
                url: URLs.ddns + 'rest/users/shipowners/' + $scope.user.idUsers + '/boats',
                data: {
                    boatInfo: JSON.stringify(boat.info),
                    files: boat.files
                }
            }).then(function (response) {
                toastr.success('Uploaded Boat');
            }, function (response) {

            }, function (evt) {
                $scope.progress =
                    Math.min(100, parseInt(100.0 * evt.loaded / evt.total));
                $('#file-upload-progress').progress({
                    percent: $scope.progress
                });

            });

        };

        $scope.showAddBoatDialog = function () {
            ngDialog.open({
                template: 'views/userId.boats.addBoat.html',
                className: 'ngdialog-theme-default',
                controller: 'UseridCtrl',
                scope: $scope
            });

        };

    });
