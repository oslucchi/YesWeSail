'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:AdminrequestsCtrl
 * @description
 * # AdminrequestsCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
  .controller('AdminrequestsCtrl', function  ($scope, $http, URLs, MAPS, lodash, toastr, ngDialog, $filter, $translate) {
    $scope.getRequests=function(){
        $http.get(URLs.ddns + 'rest/requests', {}).then(function(res){
            $scope.requests=res.data;
        });
    };
     $http.get(URLs.ddns + 'rest/reviews/1', {}).then(function(res){
            $scope.reviews=res.data;
        });
    
    $scope.getRequests();
    
     $scope.activate=function(event){
        event.status='A';
        $http.put(URLs.ddns + 'rest/events/'+event.idEvents, event).then(function(res){
            toastr.success('Event correctly activated!');
        }, function(err){
            toastr.error('Something went wrong while trying to activate the event, maybe the hamsters ran away!');
        });
    }
    $scope.deactivate=function(event){
        event.status='P';
        $http.put(URLs.ddns + 'rest/events/'+event.idEvents, event).then(function(res){
            toastr.success('Event correctly deactivated!');
        }, function(err){
            toastr.error('Something went wrong while trying to activate the event, maybe the hamsters ran away!');
        });
    }
    $scope.remove=function(event){
         $http.delete(URLs.ddns + 'rest/events/delete/'+ event.idEvents).then(function(res){
            toastr.success('Event deleted!');
             $scope.events.splice($scope.events.indexOf(event),1);
             
        }, function(err){
            toastr.error('Something went wrong while trying to delete the event, the gods chose to spare this event!');
        });
    }
    $scope.clone=function(event){
        
        event.dateStart=new Date(event.dateStart);
        event.dateEnd=new Date(event.dateEnd);
        event.dateStart=$filter('date')(event.dateStart, 'yyyy-MM-dd');
        event.dateEnd=$filter('date')(event.dateEnd, 'yyyy-MM-dd');
        
        $http.post(URLs.ddns + 'rest/events/clone', event).then(function(res){
            toastr.success('Event '+event.idEvents+' duplicated! New ID '+res.data.idEvents);
            $scope.closeModals();
        }, function(err){
            toastr.error('Something went wrong while trying to duplicate the event, the monkeys are probably on strike again!');
            $scope.closeModals();
        });
    };
    
    $scope.updateEarlyBooking=function(event, state){
        event.earlyBooking=state;
        $http.put(URLs.ddns + 'rest/events/'+event.idEvents, event).then(function(res){
            toastr.success($translate.instant('admin.events.success.update', {eventId: event.idEvents}));
        }, function(err){
            event.earlyBooking=!state;
            toastr.error('Something went wrong while trying to duplicate the event, the monkeys are probably on strike again!');
        });
    };
    
    $scope.updateLastMinute=function(event, state){
        event.lastMinute=state;
        $http.put(URLs.ddns + 'rest/events/'+event.idEvents, event).then(function(res){
            toastr.success($translate.instant('admin.events.success.update', {eventId: event.idEvents}));
        }, function(err){
            event.lastMinute=!state;
            toastr.error('Something went wrong while trying to duplicate the event, the monkeys are probably on strike again!');
        });
    };
    
    $scope.showClonePopup=function(event){
          $scope.tempCloneEvent=event;
        $scope.tempCloneEvent.dateStart=$filter('date')($scope.tempCloneEvent.dateStart, 'dd MMM yyyy')
        $scope.tempCloneEvent.dateEnd=$filter('date')( $scope.tempCloneEvent.dateEnd, 'dd MMM yyyy')
        $scope.tempCloneEvent.aggregateKey=false;
            ngDialog.open({
                template: 'views/admin.events.clone.html'
                , className: 'ngdialog-theme-default'
                , controller: 'AdminCtrl',
                scope: $scope
            });
    };
    
    $scope.closeModals=function(){
        ngDialog.closeAll();
    };
  });
