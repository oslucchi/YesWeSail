'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:LoginCtrl
 * @description
 * # LoginCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('LoginCtrl', function ($scope, $rootScope, ngDialog, AUTH_EVENTS, AuthService, $location, $window, $http, USER_ROLES) {
        $scope.credentials = {
            username: '',
            password: ''
        };
        $scope.login = function (credentials) {
            $scope.error=null;
            AuthService.login(credentials).then(function (res) {
                 $http.defaults.headers.common['Authorization'] = res.token;
                if(res.user.roleId==USER_ROLES.ADMIN){
                    $window.location.href = '#/admin/events?token='+res.token;
                }else{
                    $window.location.href = '#/?token='+res.token;    
                }
                
                $scope.setCurrentUser(res.user);
//                $window.location.reload();
                $rootScope.$broadcast(AUTH_EVENTS.loginSuccess);
                 ngDialog.closeAll();
                
                
                
            }, function (res) {
                $scope.error=res.data.error;
                $rootScope.$broadcast(AUTH_EVENTS.loginFailed);
            });
        };
    
    $scope.closeModals=function(){
      ngDialog.closeAll();  
    };
    });