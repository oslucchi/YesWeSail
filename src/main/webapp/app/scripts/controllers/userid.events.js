'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:UseridCtrl
 * @description
 * # UseridCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('UseridEventsCtrl', function ($scope, $stateParams, $http, URLs, Session, toastr, $timeout, ngDialog, Upload, $translate, moment, $filter) {


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

        $scope.getEvents = function () {
            $http.get(URLs.ddns + 'rest/users/' + $stateParams.userId + '/events').then(function (res) {
                $scope.userEvents = res.data.events;
            });
        };
    
        $scope.getEvents();

      
    
    $scope.showClonePopup = function (event) {
        $scope.tempCloneEvent = event;
        $scope.tempCloneEvent.dateStart = $filter('date')($scope.tempCloneEvent.dateStart, 'yyyy/MM/dd')
        $scope.tempCloneEvent.dateEnd = $filter('date')($scope.tempCloneEvent.dateEnd, 'yyyy/MM/dd')
        $scope.tempCloneEvent.aggregateKey = false;
        ngDialog.open({
            template: 'views/admin.events.clone.html'
            , className: 'ngdialog-theme-default'
            , controller: 'AdminCtrl'
            , scope: $scope
        });
    };

    });
