'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:UseridCtrl
 * @description
 * # UseridCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('UseridBoatsCtrl', function ($scope, $stateParams, $http, URLs, Session, toastr, $timeout, ngDialog, Upload, $translate) {


    
    
   

        $scope.getBoats = function () {
            $http.get(URLs.ddns + 'rest/users/shipowners/' + $stateParams.userId + '/boats').then(function (res) {
                $scope.boats = res.data.boats;
            });
        };
    
        $scope.getBoats();

        $scope.tempBoat = {
            info: {
                bunks: 2,
                cabinsNoBathroom: 3,
                cabinsWithBathroom: 4,
                engineType: 'V',
                idBoats: 0,
                insurance: '1234567890',
                length: '1110',
                model: 'boh',
                name: 'laPippa',
                ownerId: 0,
                plate: 'BN023WX',
                RTFLicense: '1234',
                securityCertification: '56789',
                sharedBathrooms: '1',
                year: '2000'
            },
            files: {
                docs: [],
                bluePrints: [],
                other: []

            }
        };

        $scope.addDocs = function (files) {
            $scope.tempBoat.files.docs = files;
        };
    
        $scope.removeFile=function(files, file){
            files.splice(files.indexOf(file), 1);
        }
        
        $scope.removeBoat=function(boat){
            $http.delete( URLs.ddns + 'rest/users/shipowners/' + $scope.user.idUsers + '/boats/'+boat.idBoats).then(function(res){
                toastr.success($translate.instant('global.delete.success'));
                $scope.boats=res.data.boats;
            }, function(err){
                toastr.error($translate.instant('global.delete.error'));
            })
        }
    
        $scope.addBluePrints = function (files) {
            $scope.tempBoat.files.bluePrints = files;
        };
        $scope.addOtherImages = function (files) {
            $scope.tempBoat.files.other = files;
        };

        $scope.sendBoat = function (boat) {
            $scope.tempBoat.ownerId=$scope.currentUser.idUsers;
            Upload.upload({
                url: URLs.ddns + 'rest/users/shipowners/' + $scope.user.idUsers + '/boats',
                data: {
                    boatInfo: JSON.stringify(boat.info),
                    docs: boat.files.docs,
                    bluePrints: boat.files.bluePrints,
                    other: boat.files.other
                }
            }).then(function (response) {
            	toastr.success($translate.instant('userIdBoats.dataUploaded'));
                $scope.error={
                    rejectionMessage:response.data.rejectionMessage,
                    rejectedList:response.data.rejectedList,
                };
                if(!!!$scope.error.rejectedList){
                     $scope.closeModals();
                }else{
                    $scope.boatUploadedPartially=true;
                }
                $scope.getBoats();
            }, function (response) {

            }, function (evt) {
                $scope.progress =
                    Math.min(100, parseInt(100.0 * evt.loaded / evt.total));
                $('#file-upload-progress').progress({
                    percent: $scope.progress
                });

            });

        };
    
        $scope.engines = [
            {
            engine: 'V',
                description: $translate.instant('userid.boats.addBoat.propulsion.sail') 
        },{
            engine: 'M',
                description: $translate.instant('userid.boats.addBoat.propulsion.engine') 
        }
                         ]    
    
        $scope.showAddBoatDialog = function () {
            ngDialog.open({
                template: 'views/userId.boats.addBoat.html',
                className: 'ngdialog-theme-default custom-width',
                controller: 'UseridBoatsCtrl',
                scope: $scope
            });

        };
    
    $scope.closeModals=function(){
        ngDialog.closeAll();
    };

    });
