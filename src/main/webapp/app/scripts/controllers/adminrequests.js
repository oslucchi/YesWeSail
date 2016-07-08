'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:AdminrequestsCtrl
 * @description
 * # AdminrequestsCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('AdminrequestsCtrl', function ($scope, $http, URLs, MAPS, lodash, toastr, ngDialog, $filter, $translate) {
        $scope.getRequests = function () {
            $http.get(URLs.ddns + 'rest/requests', {}).then(function (res) {
                $scope.requests = res.data;

            });
        };
      

        $scope.getRequests();

        $scope.update = function (link, action, actionId) {
            $http.put(URLs.ddns + link +'/'+actionId+'/'+action).then(function (res) {
                toastr.success('Updated!');
                $scope.closeModals();
                $scope.getRequests();
            }, function (err) {
                toastr.error('Something went wrong!');
            });
        }
  
       

        $scope.openRequestApprovalDialog = function (link, actionType, actionId) {
            
            $scope.requestType = actionType;
            $scope.actionId = actionId;
            $scope.reqLink=link;
            $http.get(URLs.ddns + link).then(function(res){
               $scope.reqDetails=res.data; 
            });
            
            ngDialog.open({
                template: 'views/admin.requests.approve.html',
                className: 'ngdialog-theme-default',
                controller: 'AdminrequestsCtrl',
                scope: $scope
            });
        };




        $scope.closeModals = function () {
            ngDialog.closeAll();
        };
    });
