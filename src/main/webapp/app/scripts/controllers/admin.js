'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('AdminCtrl', function ($scope, $http, URLs, MAPS, lodash, toastr, ngDialog, $filter, $translate, moment) {

        $scope.badges = {
            numberEvents: 0,
            numberReqs: 0,
            numberUsers: 0
        };

        $http.post(URLs.ddns + 'rest/events/search/all', {}).then(function (res) {
            $scope.badges.numberEvents = 0;
            angular.forEach(res.data, function (value, key) {
                if (value.status == 'P') {
                    $scope.badges.numberEvents++;
                }
            });
        });

        $http.get(URLs.ddns + 'rest/requests', {}).then(function (res) {
            $scope.badges.numberReqs = 0;
            angular.forEach(res.data, function (value, key) {
                if (value.status == 'P') {
                    $scope.badges.numberReqs++;
                }
            });
        });


        $http.get(URLs.ddns + 'rest/users', {}).then(function (res) {
            $scope.badges.numberUsers = 0;
            angular.forEach(res.data, function (value, key) {
                if (value.status == 'P') {
                    $scope.badges.numberUsers++;
                }
            });
        });


        $scope.getUsers = function () {
            $http.get(URLs.ddns + 'rest/users', {}).then(function (res) {
                $scope.users = res.data;
            });
        };

        $scope.getUsers();

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
            	toastr.error($translate.instant('admin.eventDeactivatingFailure'));
            });
        }
        $scope.remove = function (event) {
            $http.delete(URLs.ddns + 'rest/events/delete/' + event.idEvents).then(function (res) {
               	toastr.success($translate.instant('admin.eventDeleteSuccess'));
                $scope.events.splice($scope.events.indexOf(event), 1);
            }, function (err) {
            	toastr.error($translate.instant('admin.eventDeleteFailure'));
            });
        }
        $scope.clone = function (event) {

            event.dateStart = moment(event.dateStart, 'YYYY/MM/DD').format('YYYY-MM-DD');
            event.dateEnd = moment(event.dateEnd, 'YYYY/MM/DD').format('YYYY-MM-DD');
//            event.dateStart = $filter('date')(event.dateStart, 'yyyy-MM-dd');
//            event.dateEnd = $filter('date')(event.dateEnd, 'yyyy-MM-dd');

            $http.post(URLs.ddns + 'rest/events/clone', event).then(function (res) {
               	toastr.success($translate.instant('admin.eventDuplicateSuccess'), 
               				   {
               					 eventId: event.idEvents,
               					 newEventId: res.data.idEvents
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
                template: 'views/admin.events.clone.html',
                className: 'ngdialog-theme-default',
                controller: 'AdminCtrl',
                scope: $scope
            });
        };

        $scope.closeModals = function () {
            ngDialog.closeAll();
        };

    });
