'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:InvalidemailCtrl
 * @description
 * # InvalidemailCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
  .controller('InvalidEmailCtrl', function ($http, $scope, URLs, $location, ngDialog) {
    var token = $location.search().token;
    $scope.correctEmail='';
    $scope.errorMessage='';
    $scope.confirmRegistration=function(){
        $http.get(URLs.ddns + 'rest/auth/confirmUser/'+token+'?email='+$scope.correctEmail).then(function(res){
            ngDialog.closeAll();
        }, function(err){
            if(err.status==409){
                $scope.confirmPassword=true;
            }else{
                $scope.confirmPassword=false;
            }
            $scope.errorMessage=err.data.error;
        });
    };  
    
    $scope.confirmFacebookAccountLink=function(){
        $http.get(URLs.ddns + 'rest/auth/confirmUser/'+token+'?email='+$scope.correctEmail+'&password='+$scope.correctPassword).then(function(res){
            ngDialog.closeAll();
        }, function(err){
            $scope.errorMessage=err.data.error;
        });
    };
    
    
  });