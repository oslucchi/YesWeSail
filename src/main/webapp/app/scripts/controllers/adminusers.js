'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:AdminusersCtrl
 * @description
 * # AdminusersCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
  .controller('AdminusersCtrl', function ($scope, $http, URLs, MAPS, lodash, toastr, ngDialog, $filter, $translate) {
       
   
    $scope.getUsers=function(){
        $http.get(URLs.ddns + 'rest/users', {}).then(function(res){
            $scope.users=res.data;
              
        });
    };
    
    $scope.getUsers();
    
    
    
    
     $scope.activate=function(user){
        user.status='A';
        $http.put(URLs.ddns + 'rest/users/'+user.idUsers, user).then(function(res){
        	toastr.success($translate.instant('adminuser.activatedSuccess'));
        }, function(err){
        	toastr.error($translate.instant('adminuser.activatedFailure'));
        });
    }
    $scope.deactivate=function(user){
        user.status='P';
        $http.put(URLs.ddns + 'rest/users/'+user.idUsers, user).then(function(res){
        	toastr.success($translate.instant('adminuser.deactivatedSuccess'));
        }, function(err){
        	toastr.error($translate.instant('adminuser.deactivatedFailure'));
        });
    }


 
    
    $scope.showClonePopup=function(event){
          $scope.tempCloneEvent=event;
        $scope.tempCloneEvent.dateStart=$filter('date')($scope.tempCloneEvent.dateStart, 'dd MMM yyyy')
        $scope.tempCloneEvent.dateEnd=$filter('date')( $scope.tempCloneEvent.dateEnd, 'dd MMM yyyy')
        $scope.tempCloneEvent.aggregateKey=false;
            ngDialog.open({
                template: 'views/admin.events.clone.html'
                , className: 'ngdialog-theme-default'
                , controller: 'AdminCtrl',
                scope: $scope
            });
    };
    
    $scope.closeModals=function(){
        ngDialog.closeAll();
    };
       
  });
