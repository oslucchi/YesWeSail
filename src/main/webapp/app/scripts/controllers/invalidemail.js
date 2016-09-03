'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:InvalidemailCtrl
 * @description
 * # InvalidemailCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
  .controller('InvalidEmailCtrl', function ($http, $scope, URLs, $location, ngDialog, $window, USER_ROLES, AUTH_EVENTS, $rootScope) {
    var token = $location.search().token;
    $scope.correctEmail='';
    $scope.errorMessage='';
    $scope.confirmRegistration=function(){
        $http.get(URLs.ddns + 'rest/auth/confirmUser/FB/'+token+'?email='+$scope.correctEmail).then(function(res){
               $http.defaults.headers.common['Authorization'] = res.data.token;
                if (res.data.user.roleId == USER_ROLES.ADMIN) {
                    $window.location.href = '#/admin/events?token=' + res.data.token;
                } else {
                    $window.location.href = '#/?token=' + res.data.token;
                }

                $scope.setCurrentUser(res.data.user);
                //                $window.location.reload();
                $rootScope.$broadcast(AUTH_EVENTS.loginSuccess);
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
        $http.get(URLs.ddns + 'rest/auth/confirmUser/FB/'+token+'?email='+$scope.correctEmail+'&password='+$scope.correctPassword).then(function(res){
            $http.defaults.headers.common['Authorization'] = res.data.token;
                if (res.data.user.roleId == USER_ROLES.ADMIN) {
                    $window.location.href = '#/admin/events?token=' + res.data.token;
                } else {
                    $window.location.href = '#/?token=' + res.data.token;
                }

                $scope.setCurrentUser(res.data.user);
                //                $window.location.reload();
                $rootScope.$broadcast(AUTH_EVENTS.loginSuccess);
                ngDialog.closeAll();
        }, function(err){
        });
    };
    
    
  });