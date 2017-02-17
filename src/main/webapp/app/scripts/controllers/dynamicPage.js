'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:RegisterCtrl
 * @description
 * # RegisterCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('DynamicPageCtrl', function ($scope, $rootScope, AUTH_EVENTS, AuthService, ngDialog, $http, $window, URLs, $stateParams) {
 
        var URLReference = $stateParams.pageRef;
        $scope.page='';
    
        function getPage(pageRef){
            $http.get(URLs.ddns + 'rest/pages/dynamic/'+pageRef).then(function(res){
                $scope.page=res.data.dynamicPage.innerHTML;
            }, function(err){
                $scope.page='<h2 class="ui icon center aligned header"><i class="settings icon"></i><div class="content">404 - Page not found<div class="sub header">The requested page was not found</div></div></h2>';
            });
        }
    
        getPage(URLReference);
        
    });