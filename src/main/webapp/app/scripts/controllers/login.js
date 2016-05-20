'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:LoginCtrl
 * @description
 * # LoginCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('LoginCtrl', function ($scope, $rootScope, AUTH_EVENTS, AuthService, $location, ngDialog, $window, $http) {
        $scope.credentials = {
            username: '',
            password: ''
        };
        $scope.login = function (credentials) {
            
            AuthService.login(credentials).then(function (res) {
                 $http.defaults.headers.common['Authorization'] = res.token;
                if(res.user.roleId==3){
                    $window.location.href = '/#/admin?token='+res.token;
                }else{
                    $window.location.href = '/#/?token='+res.token;    
                }
                
                $scope.setCurrentUser(res.user);
//                $window.location.reload();
                $rootScope.$broadcast(AUTH_EVENTS.loginSuccess);
                 ngDialog.closeAll();
                
                
                
            }, function () {
                $rootScope.$broadcast(AUTH_EVENTS.loginFailed);
            });
        };
    });