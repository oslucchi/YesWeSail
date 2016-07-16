'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:UseridCtrl
 * @description
 * # UseridCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('UseridInfoCtrl', function ($scope, $stateParams, $http, URLs, Session, toastr, $timeout, ngDialog, Upload, $filter) {
    $scope.regexBirthday='^(\\d{4})-(\\d{2})-(\\d{2})$';
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
        $scope.user.personalInfo.country=$scope.personalCountry;
        $scope.user.billingInfo.country=$scope.billingCountry;
//        $scope.user.birthday=$filter('date')($scope.user.birthday, 'yyyy-MM-DD');
 
        $http.put(URLs.ddns + 'rest/users', $scope.user).then(function(res){
            $scope.editMode=false;
            toastr.success('Changes Saved');
        }, function(err){
            toastr.error('Unable to save');
        })
    };
$scope.differentBillingAddress=false;
    $scope.toggleBilling=function(state){
        $scope.differentBillingAddress=!state;
    }

        var getUser = function () {
            $http.get(URLs.ddns + 'rest/users/' + $stateParams.userId).then(function (res) {
                if(!!res.data.docs){
                    $scope.user = res.data.user;
                    $scope.docs = res.data.docs;
                    $scope.user.birthday=$filter('date')(res.data.user.birthday, 'yyyy-MM-dd');
                }else{
                    $scope.user=res.data;
                }
                
               
               

            }, function (err) {});

        };
        getUser();
    $('.ui.checkbox').checkbox();
    
    $scope.uploadProfilePic=function(pic){
             Upload.upload({
                url: URLs.ddns + 'rest/users/' + $scope.currentUser.idUsers + '/profilePic',
                data: {
                    picture: pic
                }
            }).then(function (response) {
                toastr.success('Uploaded Boat');
                 $scope.user.imageURL=response.data.images[0];
            }, function (response) {

            }, function (evt) {
                $scope.progress =
                    Math.min(100, parseInt(100.0 * evt.loaded / evt.total));
                $('#file-upload-progress').progress({
                    percent: $scope.progress
                });

            });
    }


    });
