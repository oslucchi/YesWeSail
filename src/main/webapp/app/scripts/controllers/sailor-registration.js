'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:SailorRegistrationCtrl
 * @description
 * # SailorRegistrationCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('SailorRegistrationCtrl', function ($scope, Session, toastr, $http, URLs, Upload) {
        $scope.sailor = {
            usersId: null,
            sailingDocs: null,
            navigationDocs: null,
            sailingLicense: null,
            navigationLicense: null
        }

        $scope.uploadSailingFiles = function (files) {
            $scope.sailor.sailingDocs = files;
        }
        $scope.uploadNavigationFiles = function (files) {
            $scope.sailor.navigationDocs = files;
        }

        $scope.sendFiles = function (sailorInfo) {
            sailorInfo.usersId = $scope.currentUser.idUsers;
            var files = sailorInfo.navigationDocs.concat(sailorInfo.sailingDocs);




            Upload.upload({
                url: URLs.ddns + 'rest/users/shipowners',
                data: {
                    sailorInfo: {
                        usersId: sailorInfo.usersId,
                        sailingLicense: sailorInfo.sailingLicense,
                        navigationLicense: sailorInfo.navigationLicense
                    },
                    files: files
                }
            }).then(function (response) {
                toastr.succes('Registration sent!');
            }, function (response) {

            }, function (evt) {

            });


        };

    });
