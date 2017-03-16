'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:AdminrequestsCtrl
 * @description
 * # AdminrequestsCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('AdminrequestsCtrl', function ($scope, $http, URLs, MAPS, lodash, toastr, ngDialog, $filter, $translate, $timeout) {
        $scope.requestsLoading = true;
        $scope.alertme = function () {
            alert();
        }
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
                                name: 'Status',
                                field: 'status',
                                cellTemplate: '<div class="ui-grid-cell-contents"> <div ng-switch="row.entity.status"><div ng-switch-when="A"><i class="large green checkmark icon"></i></div><div ng-switch-when="I"><i class="large red remove icon"></i></div><div ng-switch-when="P"><i class="large yellow wait icon"></i></div><div ng-switch-default> </div></div></div>'
                            },
                            {
                                name: 'ID',
                                field: 'idPendingActions'
                            },
                            {
                                name: 'Type',
                                field: 'actionType'
                            },
                            {
                                name: 'Issued By',
                                field: 'userId'
                            },
                            {
                                name: 'Created',
                                field: 'created',
                                cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity.created | date: "yyyy/MM/dd"}}</div>'
                            },
                            {
                                name: 'Updated',
                                field: 'updated',
                                cellTemplate: '<div class="ui-grid-cell-contents">{{row.entity.updated | date: "yyyy/MM/dd"}}</div>'
                            },

                    ],
                        rowTemplate: '<div ng-click="grid.appScope.openRequestApprovalDialog(row.entity.link, row.entity.actionType, row.entity.idPendingActions, row.entity.payload)" ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.uid" ui-grid-one-bind-id-grid="rowRenderIndex + \'-\' + col.uid + \'-cell\'" class="ui-grid-cell" ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader }" role="{{col.isRowHeader ? \'rowheader\' : \'gridcell\'}}" ui-grid-cell></div>',
                        onRegisterApi: function (gridApi) {
                            $scope.gridApi = gridApi;
                            $timeout(function () {
                                //                $scope.restoreState();  //call my restore function 
                            }, 100);
                        }
                    }
                    
                    
        $scope.getRequests = function () {
            $scope.requestsLoading = true;
            $http.get(URLs.ddns + 'rest/requests', {}).then(function (res) {
                $scope.requests = res.data;
                $scope.badges.numberReqs = 0;
                angular.forEach(res.data, function (value, key) {
                    if (value.status == 'P') {
                        $scope.badges.numberReqs++;
                    }
                });
                $scope.gridOptions.data=res.data;
                $scope.requestsLoading = false;
                
            });
        };




        $scope.getRequests();

        $scope.update = function (link, action, actionId) {
            $http.put(URLs.ddns + link + '/' + actionId + '/' + action).then(function (res) {
                toastr.success($translate.instant('adminRequest.updateCompleted'));
                $scope.closeModals();
                $scope.getRequests();
            }, function (err) {
                toastr.error($translate.instant('adminRequest.updateError'));
            });
        }



        $scope.restoreTicket = function (actionId, ticketLockId) {
            $http.put(URLs.ddns + 'rest/requests/ticketLost/' + actionId + '/' + ticketLockId).then(function (res) {
                toastr.success($translate.instant('adminRequest.updateCompleted'));
                $scope.closeModals();
                $scope.getRequests();
            }, function (err) {
                toastr.error($translate.instant('adminRequest.updateError'));
            });
        }

        $scope.confirmTicket = function (actionId, payload) {
            $http.post(URLs.ddns + 'rest/requests/confirmTicket/' + actionId, payload).then(function (res) {
                toastr.success($translate.instant('adminRequest.updateCompleted'));
                $scope.closeModals();
                $scope.getRequests();
            }, function (err) {
                toastr.error($translate.instant('adminRequest.updateError'));
            });
        }




        $scope.openRequestApprovalDialog = function (link, actionType, actionId, payload) {

            $scope.payload = JSON.parse(payload) || payload;
            $scope.requestType = actionType;
            $scope.actionId = actionId;
            $scope.reqLink = link;
            if (actionType !== 'confirmTicket') {
                $http.get(URLs.ddns + link).then(function (res) {
                    $scope.reqDetails = res.data;
                });
            }

            ngDialog.open({
                template: 'views/admin.requests.approve.html',
                className: 'ngdialog-theme-default',
                scope: $scope
            });
        };




        $scope.closeModals = function () {
            ngDialog.closeAll();
        };
    });
