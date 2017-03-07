'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:UseridCtrl
 * @description
 * # UseridCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('UseridTicketsCtrl', function ($scope, $stateParams, $http, URLs, Session, toastr, $timeout, ngDialog, Upload, $translate, moment, $filter) {


    $scope.sortDirection='-';
    $scope.sortValue='title';
    $scope.passed=false;
    $scope.actives=true;
    
    $scope.toggleSortDirection=function(){
        if($scope.sortDirection==='-'){
            $scope.sortDirection='';
        }else{
            $scope.sortDirection='-';
        }
        $scope.sortValueComposed=$scope.sortDirection+$scope.sortValue;
    }
    var today = moment();
    $scope.isPast=function(date){
            return today.isBefore(date);
    }
    
    $scope.$watch('sortValue', function(oldVal, newVal){
        $scope.sortValueComposed=$scope.sortDirection+$scope.sortValue;
    })

        $scope.getTickets = function () {
            $http.get(URLs.ddns + 'rest/users/' + $stateParams.userId + '/tickets').then(function (res) {
                $scope.userTickets = res.data.tickets;
            });
        };
    
        $scope.getTickets();

      
    

    });
