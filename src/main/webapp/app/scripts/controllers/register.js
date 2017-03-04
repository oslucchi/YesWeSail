'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:RegisterCtrl
 * @description
 * # RegisterCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('RegisterCtrl', function ($scope, $rootScope, AUTH_EVENTS, AuthService, ngDialog, $window, URLs, toastr) {
        $scope.credentials = {
            username: '',
            password: '',
            firstName: '',
            lastName: ''
        };
    
    $scope.fbRedirectUrl=URLs.ddns;
        $scope.register = function (credentials) {

            AuthService.register(credentials).then(function (res) {
                $rootScope.$broadcast(AUTH_EVENTS.registerSuccess);
                ngDialog.closeAll();
                toastr.success(res.data.responseMessage);
                
            }, function(res) {
                if (res.data.error == undefined)
            	{
                    $scope.error="Errore interno al server";            	
            	}
                else
                {
                    $scope.error=res.data.error;
                }
                $rootScope.$broadcast(AUTH_EVENTS.registerFailed);
            });
        };
    });