'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:UseridCtrl
 * @description
 * # UseridCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('UseridInfoCtrl', function ($scope, $stateParams, $http, URLs, Session, toastr, $timeout, ngDialog, Upload, $filter, $translate) {
        $scope.regexBirthday = '^(\\d{2})/(\\d{2})/(\\d{4})$';
        $scope.$watch('currentUser.idUsers', function (dataLoaded) {
            if (dataLoaded) {
                if ($stateParams.userId == $scope.currentUser.idUsers) {
                    $scope.isOwner = true;
                }
                $scope.isAdmin = Session.isAdmin($scope.currentUser.roleId);
            }
        });


        $scope.personalCountry = null;
        $scope.billingCountry = null;

        $scope.enterEditMode = function () {
            $scope.editMode = true;
        };

        $scope.saveChanges = function (form) {
            if (form.$valid) {
                $scope.user.personalInfo.country = $scope.personalCountry;
                $scope.user.billingInfo.country = $scope.billingCountry;
                
                

                $http.put(URLs.ddns + 'rest/users/'+$stateParams.userId, $scope.user).then(function (res) {
                    $scope.editMode = false;
                	toastr.success($translate.instant('useridInfo.updateSuccess'));
                 }, function (err) {
                 	toastr.error($translate.instant('global.updateError'),
                 				 {
                 					errorMsg: ""
                 				 });
                })
            }else{
                  toastr.warning($translate.instant('useridInfo.updateWarning'));
            }
        };
        $scope.differentBillingAddress = false;
        $scope.toggleBilling = function (state) {
            if ($scope.editMode) {
                $scope.differentBillingAddress = !state;
            }
        }

        var getUser = function () {
            $http.get(URLs.ddns + 'rest/users/' + $stateParams.userId).then(function (res) {
                if (!!res.data.docs) {
                    $scope.user = res.data.user;
                    $scope.docs = res.data.docs;
                    $scope.personalCountry = $scope.user.personalInfo.country;
                    $scope.billingCountry = $scope.user.billingInfo.country;
                    $scope.user.birthday = $filter('date')(res.data.user.birthday, 'dd/MM/yyyy');
                } else {
                    $scope.user = res.data;
                }




            }, function (err) {});

        };
        getUser();
        $('.ui.checkbox').checkbox();

        $scope.uploadProfilePic = function (pic) {
            Upload.upload({
                url: URLs.ddns + 'rest/users/' + $scope.currentUser.idUsers + '/profilePic',
                data: {
                    picture: pic
                }
            }).then(function (response) {
            	toastr.success($translate.instant('useridInfo.uploadedBoat'));
                $scope.user.imageURL = response.data.images[0];
            }, function (response) {

            }, function (evt) {
                $scope.progress =
                    Math.min(100, parseInt(100.0 * evt.loaded / evt.total));
                $('#file-upload-progress').progress({
                    percent: $scope.progress
                });

            });
        }


    });