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
        $scope.gridOptions = {
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
                    minWidth: 100,
                    width: 100,
                    name: 'Actions',
                    field: "",
                    cellTemplate: '<div class="ui-grid-cell-contents"><i ng-click="grid.appScope.activate(row.entity)" class="large green checkmark link icon"></i> <i ng-click="grid.appScope.deactivate(row.entity)" class="large red ban link icon"></i> <i ui-sref="userId.profile({userId: row.entity.idUsers})" class="large grey edit link icon"></i> </div>'
                        },
                {
                    name: 'Status',
                    field: 'status',
                    cellTemplate: '<div class="ui-grid-cell-contents"> <div ng-switch="row.entity.status"><div ng-switch-when="A"><i class="large green checkmark icon"></i></div><div ng-switch-when="I"><i class="large red remove icon"></i></div><div ng-switch-when="P"><i class="large yellow wait icon"></i></div><div ng-switch-default> </div></div></div>'
                            },
                {
                    name: 'ID',
                    field: 'idUsers'
                            },
                {
                    name: 'role',
                    field: 'roleId'
                            },
                {
                    name: 'name',
                    field: 'name'
                            },
                {
                    name: 'surname',
                    field: 'surname'
                            },
                {
                    name: 'email',
                    field: 'email'
                            },
                {
                    name: 'phone',
                    field: 'phone1'
                            },

                    ],
            rowTemplate: '<div ng-click="grid.appScope.openRequestApprovalDialog(row.entity.link, row.entity.actionType, row.entity.idPendingActions, row.entity.payload)" ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.uid" ui-grid-one-bind-id-grid="rowRenderIndex + \'-\' + col.uid + \'-cell\'" class="ui-grid-cell" ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader }" role="{{col.isRowHeader ? \'rowheader\' : \'gridcell\'}}" ui-grid-cell></div>',
            onRegisterApi: function (gridApi) {
                $scope.gridApi = gridApi;
            }
        }

        $scope.getUsers = function () {
            $http.get(URLs.ddns + 'rest/users', {}).then(function (res) {
                $scope.users = res.data;
                $scope.badges.numberUsers = 0;
                angular.forEach(res.data, function (value, key) {
                    if (value.status == 'P') {
                        $scope.badges.numberUsers++;
                    }
                });
                $scope.gridOptions.data = res.data;
            });
        };

        $scope.getUsers();




        $scope.activate = function (user) {
            user.status = 'A';
            $http.put(URLs.ddns + 'rest/users/' + user.idUsers, user).then(function (res) {
                toastr.success($translate.instant('adminuser.activatedSuccess'));
            }, function (err) {
                toastr.error($translate.instant('adminuser.activatedFailure'));
            });
        }
        $scope.deactivate = function (user) {
            user.status = 'P';
            $http.put(URLs.ddns + 'rest/users/' + user.idUsers, user).then(function (res) {
                toastr.success($translate.instant('adminuser.deactivatedSuccess'));
            }, function (err) {
                toastr.error($translate.instant('adminuser.deactivatedFailure'));
            });
        }


//
//
//        $scope.showClonePopup = function (event) {
//            $scope.tempCloneEvent = event;
//            $scope.tempCloneEvent.dateStart = $filter('date')($scope.tempCloneEvent.dateStart, 'dd MMM yyyy')
//            $scope.tempCloneEvent.dateEnd = $filter('date')($scope.tempCloneEvent.dateEnd, 'dd MMM yyyy')
//            $scope.tempCloneEvent.aggregateKey = false;
//            ngDialog.open({
//                template: 'views/admin.events.clone.html',
//                className: 'ngdialog-theme-default',
//                controller: 'AdminCtrl',
//                scope: $scope
//            });
//        };

        $scope.closeModals = function () {
            ngDialog.closeAll();
        };

    });
