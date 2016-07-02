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
       
   
    $scope.getUsers=function(){
        $http.get(URLs.ddns + 'rest/users', {}).then(function(res){
            $scope.users=res.data;
              
        });
    };
    
    $scope.getUsers();
     $scope.activate=function(user){
        event.status='A';
        $http.put(URLs.ddns + 'rest/users/'+user.idUsers, user).then(function(res){
            toastr.success('User correctly activated!');
        }, function(err){
            toastr.error('Something went wrong while trying to activate the user, maybe the hamsters ran away!');
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
    $scope.remove=function(user){
         $http.delete(URLs.ddns + 'rest/users/delete/'+ user.idusers).then(function(res){
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
