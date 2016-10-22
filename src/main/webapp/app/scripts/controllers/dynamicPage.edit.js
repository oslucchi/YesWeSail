'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:RegisterCtrl
 * @description
 * # RegisterCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('DynamicPageEditCtrl', function ($scope, $rootScope, AUTH_EVENTS, AuthService, ngDialog, $http, $window, URLs, $stateParams) {
 
    $scope.dynamicPage={
        createdOn: '',
        uRLReference: $stateParams.pageRef,
        innerHTML: '',
        language: '',
        status: 'I',
    }
    
    
        
        function getPage(pageRef){
            $http.get(URLs.ddns + 'rest/pages/dynamic/'+pageRef).then(function(res){
                $scope.dynamicPage=res.data.dynamicPage;
            }, function(err){
                $scope.page='<h2 class="ui icon center aligned header"><i class="settings icon"></i><div class="content">404 - Page not found<div class="sub header">The requested page was not found</div></div></h2>';
            })
        } 
    
        $scope.savePage=function(page){
                $http.put(URLs.ddns + 'rest/pages/dynamic/'+$scope.dynamicPage.uRLReference, page).then(function(res){
                    toastr.success();
                }, function(err){
                    toastr.error();
                })
            }
        if($scope.dynamicPage.uRLReference){
            getPage($scope.dynamicPage.uRLReference);
        }

        

    });