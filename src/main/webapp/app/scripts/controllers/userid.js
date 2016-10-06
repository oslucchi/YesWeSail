'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:UseridCtrl
 * @description
 * # UseridCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('UseridCtrl', function ($scope, $stateParams, $http, URLs, Session, toastr, $timeout, $translate, ngDialog, Upload) {

    $scope.$watch('currentUser.idUsers', function(dataLoaded) {
          if (dataLoaded) {
              if($stateParams.userId == $scope.currentUser.idUsers){
                  $scope.isOwner=true;
              }
               $scope.isAdmin=Session.isAdmin($scope.currentUser.roleId);
          }
        });
        
        
    
//    
//    $scope.enterEditMode=function(){
//        $scope.editMode=true;    
//    };
//    
//    $scope.saveChanges=function(){
// 
//        $http.put(URLs.ddns + 'rest/users', $scope.user).then(function(res){
//            $scope.editMode=false;
//            toastr.success('Changes Saved');
//        }, function(err){
//            toastr.error('Unable to save');
//        })
//    };
//$scope.differentBillingAddress=false;
//    $scope.toggleBilling=function(state){
//        $scope.differentBillingAddress=!state;
//    }
//
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
  
    
    $scope.getRating=function(userId){
        $http.get(URLs.ddns + 'rest/reviews/'+userId+'/rating').then(function(res){
           
            $scope.reputation={
                rating: res.data.rating,
                populationSize: res.data.populationSize
            };
            
                
                 $('.reputation.star.rating').rating({
                    initialRating: Math.round($scope.reputation.rating),
                    maxRating: 5
            }).rating('disable');
        });
        
    };
    
    $scope.getRating($stateParams.userId);
    
    
    $('.ui.checkbox').checkbox();
    
    $scope.uploadProfilePic=function(pic){
             Upload.upload({
                url: URLs.ddns + 'rest/users/' + $scope.currentUser.idUsers + '/profilePic',
                data: {
                    picture: pic
                }
            }).then(function (response) {
            	toastr.success($translate.instant('userid.uploadedBoat'));
                 $scope.user.imageURL=response.data.images[0]+ '?decache=' + Math.random();;
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
