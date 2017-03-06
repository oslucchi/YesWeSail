'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:SailorRegistrationCtrl
 * @description
 * # SailorRegistrationCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('PasswordResetCtrl', function ($scope, Session, toastr, $http, URLs, Upload, $location) {
        $scope.inputType='password';
        
        $scope.togglePassword=function(){
            if($scope.inputType==='password'){
                $scope.inputType='text'
            }else{
                $scope.inputType='password'
            }
        }
    
        $scope.credentials = {
            username: $location.search().email || null,
            password: null
        };
        
        $scope.resetPassword = function (creds) {
            if(creds.password===$scope.confirmPassword){
                $http.post(URLs.ddns + 'rest/auth/changePassword', creds).then(function (res) {
                    $scope.successMessage=res.data.message;
                }, function(err){
                    $scope.errorMessage=err.data.error;
                });
            }
        };
    });
