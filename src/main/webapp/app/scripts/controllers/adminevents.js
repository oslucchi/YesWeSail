'use strict';
/**
 * @ngdoc function
 * @name yeswesailApp.controller:AdmineventsCtrl
 * @description
 * # AdmineventsCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp').controller('AdmineventsCtrl', function ($scope, $http, URLs, MAPS, lodash, toastr, ngDialog, $filter, $translate) {
    $scope.getEvents = function () {
        $http.post(URLs.ddns + 'rest/events/search/all', {}).then(function (res) {
            $scope.events = res.data.events;
            $scope.shipOwners = res.data.shipowners;
            $('table').tablesort();    
            // Sort by dates in YYYY-MM-DD format
            $('thead th.date').data('sortBy', function(th, td, tablesort) {
                return new Date(td.text());
            });
            $('thead th.id').data('sortBy', function(th, td, tablesort) {
                return Number(td.text());
            });
            
         
            
        });
    };
//    $scope.sortVar1 = 'createdOn'
//    $scope.sortVar2 = 'createdOn'
//    $scope.sortVarDirection = '-'
//    $scope.sortBy = function (col) {
//       
//         if ($scope.sortVarDirection === '') {
//            $scope.sortVarDirection = '-';
//        }
//        else {
//            $scope.sortVarDirection = '';
//        }
//        if(col==='location'){
//            $scope.sortVar1 ='location';
//            $scope.sortVar2='shipOwnerId';
//            $scope.sortVar3='dateStart';
//        }else if(col==='shipOwnerId'){
//            $scope.sortVar1 ='shipOwnerId';
//            $scope.sortVar2='createdOn';
//            $scope.sortVar3='';
//        }else{
//            $scope.sortVar1 =col
//            $scope.sortVar2='createdOn';
//            $scope.sortVar3='';
//        }
//        
//    }
    $scope.getEvents();
    $scope.activate = function (event) {
        event.status = 'A';
        $http.put(URLs.ddns + 'rest/events/' + event.idEvents, event).then(function (res) {
            toastr.success($translate.instant('admin.eventActivatingSuccess'));
        }, function (err) {
            toastr.error($translate.instant('admin.eventActivatingFailure'));
        });
    }
    $scope.deactivate = function (event) {
        event.status = 'P';
        $http.put(URLs.ddns + 'rest/events/' + event.idEvents, event).then(function (res) {
            toastr.success($translate.instant('admin.eventDectivatingSuccess'));
        }, function (err) {
            toastr.error($translate.instant('admin.eventDectivatingFailure'));
        });
    }
    $scope.remove = function (event) {
        $http.delete(URLs.ddns + 'rest/events/delete/' + event.idEvents).then(function (res) {
            toastr.success($translate.instant('admin.eventDeleteSuccess'));
            $scope.events.splice($scope.events.indexOf(event), 1);
        }, function (err) {
            toastr.error(err.data.error);
        });
    }
    $scope.clone = function (event) {
        event.dateStart = new Date(event.dateStart);
        event.dateEnd = new Date(event.dateEnd);
        event.dateStart = $filter('date')(event.dateStart, 'yyyy-MM-dd');
        event.dateEnd = $filter('date')(event.dateEnd, 'yyyy-MM-dd');
        $http.post(URLs.ddns + 'rest/events/clone', event).then(function (res) {
            toastr.success($translate.instant('admin.eventDuplicateSuccess'), {
                eventId: event.IdEvents
                , newEventId: res.data.idEvents
            });
            $scope.closeModals();
        }, function (err) {
            toastr.error($translate.instant('admin.eventDuplicateFailure'));
            $scope.closeModals();
        });
    };
    $scope.updateEarlyBooking = function (event, state) {
        event.earlyBooking = state;
        $http.put(URLs.ddns + 'rest/events/' + event.idEvents, event).then(function (res) {
            toastr.success($translate.instant('admin.events.success.update', {
                eventId: event.idEvents
            }));
        }, function (err) {
            event.earlyBooking = !state;
            toastr.error($translate.instant('admin.eventUpdateFailure'));
        });
    };
    $scope.updateHotEvent = function (event, state) {
        event.hotEvent = state;
        $http.put(URLs.ddns + 'rest/events/' + event.idEvents, event).then(function (res) {
            toastr.success($translate.instant('admin.events.success.update', {
                eventId: event.idEvents
            }));
        }, function (err) {
            event.earlyBooking = !state;
            toastr.error($translate.instant('admin.eventUpdateFailure'));
        });
    };
    $scope.updateLastMinute = function (event, state) {
        event.lastMinute = state;
        $http.put(URLs.ddns + 'rest/events/' + event.idEvents, event).then(function (res) {
            toastr.success($translate.instant('admin.events.success.update', {
                eventId: event.idEvents
            }));
        }, function (err) {
            event.lastMinute = !state;
            toastr.error($translate.instant('admin.eventUpdateFailure'));
        });
    };
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
    $scope.showAddPassengerPopup = function (eventId) {
        $scope.eventId = eventId;
        ngDialog.open({
            template: 'views/admin.events.addpassenger.html'
            , className: 'ngdialog-theme-default'
            , controller: 'AddPassengerCtrl'
            , scope: $scope
        });
    };
    $scope.showRemovePassengerPopup = function (eventId) {
        $scope.eventId = eventId;
        ngDialog.open({
            template: 'views/admin.events.removepassenger.html'
            , className: 'ngdialog-theme-default'
            , controller: 'RemovePassengerCtrl'
            , scope: $scope
        });
    };
    $scope.closeModals = function () {
        ngDialog.closeAll();
    };
});