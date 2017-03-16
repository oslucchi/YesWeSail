'use strict';
/**
 * @ngdoc function
 * @name yeswesailApp.controller:AdmineventsCtrl
 * @description
 * # AdmineventsCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp').controller('AdmineventsCtrl', function ($scope, $http, URLs, MAPS, lodash, toastr, ngDialog, $filter, $translate, $timeout, $cookies) {

    $scope.eventsLoading = true;
    
            $scope.gridOptions = {
                autoResize: false,
                enableHorizontalScrollbar: 0,
                enableColumnResizing: true,
                enableSorting: true,
                enableGridMenu: true,
                enableFiltering: true,
                gridMenuCustomItems: [
                    {
                        title: 'Save',
                        action: function ($event) {
                            $scope.saveState();
                        },
                        order: 210
                      }, {
                        title: 'Restore',
                        action: function ($event) {
                            $scope.restoreState();
                        },
                        order: 211
                      }
                    ],
                columnDefs: [
                    {
                        enableFiltering: false,
                        minWidth: 220,
                        width: 220,
                        name: 'Actions',
                        field: "",
                        cellTemplate: '<div class="ui-grid-cell-contents"><i ng-click="grid.appScope.activate(row.entity)" class="large green checkmark link icon"></i> <i ng-really-message="Are you sure?" ng-really-click="remove(row.entity)" class="large red trash outline link icon"></i> <i ng-click="grid.appScope.deactivate(row.entity)" class="large red ban link icon"></i> <i ng-click="grid.appScope.showClonePopup(row.entity)" class="large grey copy link icon"></i> <i ui-sref="editEvent({eventId: row.entity.idEvents})" class="large grey edit link icon"></i> <i ng-click="grid.appScope.showAddPassengerPopup(row.entity.idEvents)" class="large grey add user link icon"></i> <i ng-click="grid.appScope.showRemovePassengerPopup(row.entity.idEvents)" class="large grey remove user link icon"></i></div>'
                    },
                    {
                        minWidth: 65,
                        width: 65,
                        name: 'Status',
                        field: 'status',
                        cellTemplate: '<div class="ui-grid-cell-contents"> <div ng-switch="row.entity.status"><div ng-switch-when="A"><i class="large green checkmark icon"></i></div><div ng-switch-when="I"><i class="large red remove icon"></i></div><div ng-switch-when="P"><i class="large yellow wait icon"></i></div><div ng-switch-default> </div></div></div>'
                    },
                    {
                        minWidth: 65,
                        width: 65,
                        name: 'Hot',
                        field: 'hotEvent',
                        cellTemplate: '<div class="ui-grid-cell-contents"><div class="ui fitted center  checkbox"> <input type="checkbox" ng-model="row.entity.hotEvent" ng-change="grid.appScope.updateHotEvent(row.entity, row.entity.hotEvent)" ng-checked="row.entity.hotEvent">   <label></label> </div></div>'
                    },
                    {
                        minWidth: 65,
                        width: 65,
                        name: 'Early',
                        field: 'earlyBooking',
                        cellTemplate: '<div class="ui-grid-cell-contents"><div class="ui fitted center  checkbox"> <input type="checkbox" ng-model="row.entity.earlyBooking" ng-change="grid.appScope.updateEarlyBooking(row.entity, row.entity.earlyBooking)" ng-checked="row.entity.earlyBooking">   <label></label> </div></div>'
                    },
                    {
                        minWidth: 65,
                        width: 65,
                        name: 'Last Min.',
                        field: 'lastMinute',
                        cellTemplate: '<div class="ui-grid-cell-contents"><div class="ui fitted center  checkbox"> <input type="checkbox" ng-model="row.entity.lastMinute" ng-change="grid.appScope.updateLastMinute(row.entity, row.entity.lastMinute)" ng-checked="row.entity.lastMinute">   <label></label> </div></div>'
                    },
                    {
                        minWidth: 65,
                        width: 65,
                        name: 'ID',
                        field: 'idEvents'
                    },
                    {
                        name: 'Titolo',
                        field: 'title'
                    },
                    {
                        name: 'ID Armatore',
                        field: 'shipOwnerId'
                    },
                    {
                        enableFiltering: false,
                        sort: {
                            direction: 'desc',
                            priority: 0
                        },
                        name: 'Armatore',
                        field: 'shipOwnerId',
                        cellTemplate: '<div class="ui-grid-cell-contents">{{grid.appScope.shipOwners[row.entity.shipOwnerId].name}} {{grid.appScope.shipOwners[row.entity.shipOwnerId].surname}}</div>'
                    },
                    {
                        enableFiltering: false,
                        sort: {
                            direction: 'desc',
                            priority: 1
                        },
                        name: 'Data Inizio',
                        field: 'dateStart',
                        cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity.dateStart | date: "yyyy/MM/dd"}}</div>'
                    },
                    {
                        enableFiltering: false,
                        name: 'Data Fine',
                        field: 'dateEnd',
                        cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity.dateEnd | date: "yyyy/MM/dd"}}</div>'
                    },
                    {
                        minWidth: 103,
                        width: 103,
                        name: 'Creato da',
                        field: 'createdBy'
                    },
                    {
                        enableFiltering: false,
                        name: 'Creato il',
                        field: 'createdOn',
                        cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity.createdOn | date: "yyyy/MM/dd"}}</div>'
                    },
                    {
                        name: 'Meta',
                        field: 'location'
                    }
        ],
                onRegisterApi: function (gridApi) {
                    $scope.gridApi = gridApi;
                    $timeout(function () {
                        $scope.restoreState(); //call my restore function 
                    }, 100);
                }
            }
    
    $scope.getEvents = function () {
        $scope.eventsLoading = true;
        $http.post(URLs.ddns + 'rest/events/search/all', {}).then(function (res) {
            $scope.events = res.data.events;
            $scope.shipOwners = res.data.shipowners;
            $scope.gridOptions.data=res.data.events;
            $scope.badges.numberEvents = 0;
            angular.forEach(res.data.events, function (value, key) {
                if (value.status == 'P') {
                    $scope.badges.numberEvents++;
                }
            });
    

            $scope.eventsLoading = false;
        });
    };





    $scope.getEvents();
    $scope.activate = function (event) {
        event.status = 'A';
        $http.put(URLs.ddns + 'rest/events/' + event.idEvents + '/changeStatus', {
            status: event.status
        }).then(function (res) {
            toastr.success($translate.instant('admin.eventActivatingSuccess'));
        }, function (err) {
            toastr.error(err.data.error);
        });
    }

    $scope.deactivate = function (event) {
        event.status = 'P';
        $http.put(URLs.ddns + 'rest/events/' + event.idEvents + '/changeStatus', {
            status: event.status
        }).then(function (res) {
            toastr.success($translate.instant('admin.eventDectivatingSuccess'));
        }, function (err) {
            toastr.error(err.data.error);
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
                eventId: event.IdEvents,
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
        $http.put(URLs.ddns + 'rest/events/' + event.idEvents + '/makeEarlyBooking', {
            earlyBooking: state
        }).then(function (res) {
            toastr.success($translate.instant('admin.events.success.update', {
                eventId: event.idEvents
            }));
        }, function (err) {
            event.earlyBooking = !state;
            toastr.error(err.data.error);
        });
    };
    $scope.updateHotEvent = function (event, state) {
        event.hotEvent = state;
        $http.put(URLs.ddns + 'rest/events/' + event.idEvents + '/makeHot', {
            hotEvent: state
        }).then(function (res) {
            toastr.success($translate.instant('admin.events.success.update', {
                eventId: event.idEvents
            }));
        }, function (err) {
            event.earlyBooking = !state;
            toastr.error(err.data.error);
        });
    };
    $scope.updateLastMinute = function (event, state) {
        event.lastMinute = state;
        $http.put(URLs.ddns + 'rest/events/' + event.idEvents + '/makeLastMinute', {
            lastMinute: state
        }).then(function (res) {
            toastr.success($translate.instant('admin.events.success.update', {
                eventId: event.idEvents
            }));
        }, function (err) {
            event.lastMinute = !state;
            toastr.error(err.data.error);
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
    $scope.showAddPassengerPopup = function (eventId) {
        $scope.eventId = eventId;
        ngDialog.open({
            template: 'views/admin.events.addpassenger.html',
            className: 'ngdialog-theme-default',
            controller: 'AddPassengerCtrl',
            scope: $scope
        });
    };
    $scope.showRemovePassengerPopup = function (eventId) {
        $scope.eventId = eventId;
        ngDialog.open({
            template: 'views/admin.events.removepassenger.html',
            className: 'ngdialog-theme-default',
            controller: 'RemovePassengerCtrl',
            scope: $scope
        });
    };
    $scope.closeModals = function () {
        ngDialog.closeAll();
    };

    $scope.saveState = function () {
        $scope.state = $scope.gridApi.saveState.save();
        $cookies.put('ywsEventsTableState', JSON.stringify($scope.state));
    };
    $scope.restoreState = function () {
        $scope.state = $cookies.get('ywsEventsTableState');
        if ($scope.state) {
            $scope.gridApi.saveState.restore($scope, JSON.parse($scope.state));
        }
    };
});
