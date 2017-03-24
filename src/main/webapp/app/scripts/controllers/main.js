'use strict';
/**
 * @ngdoc function
 * @name yeswesailApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp').controller('MainCtrl', function ($scope, $http, URLs, MAPS, lodash, $state, ngDialog, toastr, $translate) {
    $scope.CATEGORIES = [];
    $scope.LOCATIONS = [];
    $scope.url=URLs.ddns;
    MAPS.getMap('LOCATIONS').then(function (res) {
        if (!!res.status) {
            $scope.LOCATIONS = [];
        }
        else {
            $scope.LOCATIONS = res;
            $scope.locationsLoaded = true;
        }
    });
    MAPS.getMap('CATEGORIES').then(function (res) {
        if (!!res.status) {
            $scope.CATEGORIES = [];
        }
        else {
            $scope.CATEGORIES = res;
            $scope.categoriesLoaded = true;
        }
    });
    $scope.hotEvents = null;
    $http.post(URLs.ddns + 'rest/events/hotEvents').then(function (res) {
            $scope.hotEvents = res.data.events;
            $scope.shipOwners=res.data.shipowners;
        }, function (err) {})

    $scope.showAvailableDatesForEvents = function (events) {
        $scope.aggregatedEvents = events;
        ngDialog.open({
            template: 'views/aggregatedEvents.html'
            , className: 'ngdialog-theme-default'
            , controller: 'MainCtrl'
            , scope: $scope
        });
    };
    $scope.preventD=function(e){
        e.stopPropagation();
    }
    $scope.email=function(obj, e){
        e.stopPropagation();
        location.href= 'mailto:?subject='+obj.title+'&body='+$translate.instant('global.emailMessage')+'%0D%0A'+URLs.ddns+'events/'+obj.idEvents;
    }
    $scope.closeModals = function () {
        ngDialog.closeAll();
    };
    $scope.processEventClick = function (events) {
        if (events.length > 1) {
            $scope.showAvailableDatesForEvents(events);
        }
        else if (events.length < 2) {
            $state.go('event', {
                eventId: events[0].idEvents
            });
        }
    }
    
    $scope.user={
        email: '',
        name: '',
        surname: ''
    }
    
    $scope.subscribe=function(){
        
        if($scope.user.email){
            $http.post(URLs.ddns + 'rest/users/subscribe', {u: $scope.user, what: 'MAILCHIMP'}).then(function(res){
                toastr.success('Subscribed');
                 $scope.user={
                                email: '',
                                name: '',
                                surname: ''
                            }
            }, function(err){
                toastr.error(err.data.error);
            })
        }else{
            toastr.warning($translate.instant('global.emailMissing'));
        }
    }
    
});