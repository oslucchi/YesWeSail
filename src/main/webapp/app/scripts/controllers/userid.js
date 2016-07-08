'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:UseridCtrl
 * @description
 * # UseridCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('UseridCtrl', function ($scope, $stateParams, $http, URLs, Session, toastr, $timeout, ngDialog, Upload) {

    $scope.$watch('currentUser.idUsers', function(dataLoaded) {
          if (dataLoaded) {
              if($stateParams.userId == $scope.currentUser.idUsers){
                  $scope.isOwner=true;
              }
               $scope.isAdmin=Session.isAdmin($scope.currentUser.roleId);
          }
        });
        
        

    
    $scope.enterEditMode=function(){
        $scope.editMode=true;    
    };
    
    $scope.saveChanges=function(){
        $http.put(URLs.ddns + 'rest/users', $scope.user).then(function(res){
            $scope.editMode=false;
            toastr.success('Changes Saved');
        }, function(err){
            toastr.error('Unable to save');
        })
    };


        var getUser = function () {
            $http.get(URLs.ddns + 'rest/users/' + $stateParams.userId).then(function (res) {
                if(!!res.data.docs){
                    $scope.user = res.data.user;
                    $scope.docs = res.data.docs;
                }else{
                    $scope.user=res.data;
                }
                
               

            }, function (err) {});

        };
        getUser();


    });
