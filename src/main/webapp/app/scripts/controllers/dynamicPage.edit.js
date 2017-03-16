'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:RegisterCtrl
 * @description
 * # RegisterCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('DynamicPageEditCtrl', function ($scope, toastr, $rootScope, AUTH_EVENTS, AuthService, ngDialog, $http, $window, URLs, $stateParams, moment) {
 
    $scope.dynamicPage={
        createdOn: '',
        uRLReference: '',
        innerHTML: '',
        language: '',
        status: 'I',
        idDynamicPages: $stateParams.idPageRef
    }
    
    
        
        function getPage(idPageRef){
            $http.get(URLs.ddns + 'rest/pages/dynamic/edit/'+idPageRef).then(function(res){
                $scope.dynamicPage=res.data.dynamicPage;
                $scope.dynamicPage.createdOn=moment($scope.dynamicPage.createdOn).format('YYYY/MM/DD');
            }, function(err){
                toastr.error(err.data.error);
            })
        } 
    
        $scope.savePage=function(page){
                    page.createdOn=Number(moment(page.createdOn, 'YYYY/MM/DD').format('x'));
                    $http.put(URLs.ddns + 'rest/pages/dynamic/'+$scope.dynamicPage.idDynamicPages, page).then(function(res){
                    toastr.success('global.success');
                    page.createdOn=moment(page.createdOn, 'x').format('YYYY/MM/DD');
                }, function(err){
                    toastr.error();
                })
            }
        if($scope.dynamicPage.idDynamicPages){
            getPage($scope.dynamicPage.idDynamicPages);
        }

        

    });