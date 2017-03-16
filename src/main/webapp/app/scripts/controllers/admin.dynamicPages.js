'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:AdmindynamicPagesCtrl
 * @description
 * # AdmindynamicPagesCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('AdminDymamicPagesCtrl', function ($scope, $http, URLs, MAPS, lodash, toastr, ngDialog, $filter, $translate, $timeout, LOCALES, $state, moment) {
        $scope.dynamicPagesLoading = true;

        $scope.getDynamicPages = function () {
            $scope.dynamicPagesLoading = true;
            $scope.gridOptions = null;
            $http.get(URLs.ddns + 'rest/pages/dynamic').then(function (res) {
                $scope.dynamicPages = res.data;
                $scope.dynamicPagesLoading = false;
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
                            minWidth: 220,
                            width: 220,
                            name: 'Actions',
                            field: "",
                            cellTemplate: '<div class="ui-grid-cell-contents"><i ng-click="grid.appScope.activate(row.entity)" class="large green checkmark link icon"></i> <i ng-click="grid.appScope.deactivate(row.entity)" class="large red ban link icon"></i> <i ui-sref="dynamicPageEdit({idPageRef: row.entity.idDynamicPages})" class="large grey edit link icon"></i> </div>'
                        },

                        {
                            name: 'Status',
                            field: 'status',
                            cellTemplate: '<div class="ui-grid-cell-contents"> <div ng-switch="row.entity.status"><div ng-switch-when="A"><i class="large green checkmark icon"></i></div><div ng-switch-when="I"><i class="large red remove icon"></i></div><div ng-switch-when="P"><i class="large yellow wait icon"></i></div><div ng-switch-default> </div></div></div>'
                        },
                        {
                            name: 'ID',
                            field: 'idDynamicPages'
                        },
                        {
                            name: 'url',
                            field: 'uRLReference'
                        },

        ],
                    onRegisterApi: function (gridApi) {
                        $scope.gridApi = gridApi;
                        $timeout(function () {
                            //                $scope.restoreState();  //call my restore function 
                        }, 100);
                    },
                    data: res.data.dynamicPages
                }



            });
        };


        $scope.tempPage = {
            status: 'P',
            URLReference: '',
            language: LOCALES.preferredLocale,
            innerHTML: ''
        }

        $scope.getDynamicPages();


        $scope.createPage = function (page) {
            $http.post(URLs.ddns + '/rest/pages/dynamic', page).then(function (res) {
                toastr.success($translate.instant('global.success'));
                $scope.closeModals();
                $state.go('dynamicPageEdit', {
                    idPageRef: res.data.dynamicPage.idDynamicPages
                });
            }, function (err) {
                toastr.error(err.data.error)
            })
        };



        $scope.activate = function (page) {
            page.status = "A";
            page.createdOn = Number(moment(page.createdOn, 'YYYY/MM/DD').format('x'));
            $http.put(URLs.ddns + 'rest/pages/dynamic/' + page.idDynamicPages, page).then(function (res) {
                toastr.success('global.success');
                page.createdOn = moment(page.createdOn, 'x').format('YYYY/MM/DD');
            }, function (err) {
                toastr.error();
            })
        };

        $scope.deactivate = function (page) {
            page.status = "P";
            page.createdOn = Number(moment(page.createdOn, 'YYYY/MM/DD').format('x'));
            $http.put(URLs.ddns + 'rest/pages/dynamic/' + page.idDynamicPages, page).then(function (res) {
                toastr.success('global.success');
                page.createdOn = moment(page.createdOn, 'x').format('YYYY/MM/DD');
            }, function (err) {
                toastr.error();
            })
        };




        $scope.showCreatePagePopup = function () {
            ngDialog.open({
                template: 'views/admin.dynamicPages.create.html',
                className: 'ngdialog-theme-default',
                controller: 'AdminDymamicPagesCtrl',
                scope: $scope
            });
        };



        $scope.closeModals = function () {
            ngDialog.closeAll();
        };
    });
