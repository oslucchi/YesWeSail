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
            $scope.errorMessage=err.data.message;
        });
    };
    
    
  });