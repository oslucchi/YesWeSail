'use strict';
/**
 * @ngdoc function
 * @name yeswesailApp.controller:AdmineventsCtrl
 * @description
 * # AdmineventsCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp').controller('ContactsCtrl', function ($scope, $http, URLs, MAPS, lodash, toastr, ngDialog, $filter, $translate) {
   $scope.email={
       name:'',
       email:'',
       subject:'',
       message:''
   }
    
    $scope.sendEmail=function(email){
       $http.post(URLs.ddns+'users/contacts', email).then(function(res){
           toastr.success(res.data.message)
       }, function(err){
           toastr.error(err.data.error)
       })
   }
});