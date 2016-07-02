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

        $scope.getPendingRequests = function () {
            $http.get(URLs.ddns + 'rest/requests/user/' + $scope.currentUser.idUsers).then(function (res) {
                $scope.pendingRequests = 0;
                angular.forEach(res.data, function (value, key) {
                    if (value.actionType == 'statusUpgrade') {
                        $scope.pendingRequests++;
                    };
                });
            });
        };
        $scope.$watch('currentUser', function (newValue, oldValue) {
            if ($scope.currentUser != undefined) {
                $scope.getPendingRequests();
            }
        });

        $scope.uploadSailingFiles = function (files) {
            $scope.sailingDocsNotLoaded = false;
            $scope.sailor.sailingDocs = files;
        }
        $scope.uploadNavigationFiles = function (files) {
            $scope.navigationDocsNotLoaded = false;
            $scope.sailor.navigationDocs = files;
        }

        $scope.sendFiles = function (sailorInfo) {
            sailorInfo.usersId = $scope.currentUser.idUsers;

            if (sailorInfo.navigationDocs == null) {
                $scope.navigationDocsNotLoaded = true;
            }

            if (sailorInfo.sailingDocs == null) {
                $scope.sailingDocsNotLoaded = true;
            }

            if ($scope.sailingDocsNotLoaded == false && $scope.navigationDocsNotLoaded == false) {
                var files = sailorInfo.navigationDocs.concat(sailorInfo.sailingDocs);

                Upload.upload({
                    url: URLs.ddns + 'rest/users/shipowners',
                    data: {
                        sailorInfo: JSON.stringify({
                            usersId: String(sailorInfo.usersId),
                            sailingLicense: sailorInfo.sailingLicense,
                            navigationLicense: sailorInfo.navigationLicense
                        }),
                        files: files
                    }
                }).then(function (response) {
                    toastr.success('Registration sent!');
                     $scope.getPendingRequests();
                }, function (response) {

                }, function (evt) {
                    $scope.progress = 
                    Math.min(100, parseInt(100.0 * evt.loaded / evt.total));
                  $('#file-upload-progress').progress({
                      percent: $scope.progress
                    });
                    
                });
            }






        };

    });
