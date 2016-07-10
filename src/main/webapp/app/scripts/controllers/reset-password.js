'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:SailorRegistrationCtrl
 * @description
 * # SailorRegistrationCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('PasswordResetCtrl', function ($scope, Session, toastr, $http, URLs, Upload) {
        $scope.credentials = {
            username: null,
            password: null
        };

        $scope.resetPassword = function (creds) {
            $http.post(URLs.ddns + 'rest/auth/changePassword', creds).then(function (res) {
                $scope.successMessage=res.data.message;
            }, function(err){
                $scope.errorMessage=err.data.error;
            });
        };
    

      

   

    });
