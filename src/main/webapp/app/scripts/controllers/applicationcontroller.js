'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:ApplicationCtrl
 * @description
 * # ApplicationCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('ApplicationCtrl', function ($scope, USER_ROLES, AUTH_EVENTS, AuthService, $location, $rootScope, $cookieStore, ngDialog, Session, URLs, $http, $log) {
//        $rootScope.$on("$locationChangeStart", function(event, next, current) { 
//            if(!AuthService.isAuthenticated()){
//                 event.preventDefault()
//            }
//      });
        
        angular.element('.ui.dropdown').dropdown().dropdown({
            action: 'nothing'
        });
    
        $scope.currentUser = null;
        $scope.setCurrentUser=function(user){
            $scope.currentUser=user;
        }
        var token = $location.search().token;
        var invalidEmail = $location.search().invalidEmail;
    
        if (token != null) {

            $http.defaults.headers.common['Authorization'] = token;
            $http.defaults.headers.common['Language'] = 'IT';
            $http.post(URLs.ddns + 'rest/users/basic').then(function (res) {

                Session.create(token, res.data);
                $scope.setCurrentUser(res.data);
            }, function (err) {});
        }

        if (token == null) {

            $rootScope.globals = $cookieStore.get('globals') || {};
            if ($rootScope.globals.currentUser) {
                $http.defaults.headers.common['Authorization'] = $rootScope.globals.currentUser.token;
                $http.defaults.headers.common['Language'] = 'IT';
                $http.post(URLs.ddns + 'rest/users/basic').then(function (res) {

                    Session.create($rootScope.globals.currentUser.token, res.data);
                    $scope.currentUser = res.data;
                }, function (err) {
                    if (err.status == 401) {
                        Session.destroy();
                    }
                });
            }
        }


    

        $scope.credentials = {
            username: ''
            , password: ''
        }

        $scope.isAuthorized = AuthService.isAuthorized;
        $scope.isAuthenticated = AuthService.isAuthenticated;
        $scope.invalidMail=function(){
            if($scope.currentUser==null){
                return false;
            }
            if($scope.currentUser.email.startsWith('fake.') && $scope.currentUser.email.endsWith('yeswesail.com')){
                return true;
            }
                                                                                                                  
                
        }

        $scope.login = AuthService.login;





        $scope.logout = function () {
            AuthService.logout().then(function () {
                $rootScope.$broadcast(AUTH_EVENTS.logoutSuccess);
                $scope.setCurrentUser(null);
                $location.path('#/');
            }, function () {
                $rootScope.$broadcast(AUTH_EVENTS.logoutFail);
            });
        };
    
        
    
        var loginDialog = ngDialog;
        var registerDialog = ngDialog;
        var invalidEmailDialog = ngDialog;

        $scope.popupLogin = function () {

            ngDialog.closeAll();
            loginDialog.open({
                template: 'views/login.html'
                , className: 'ngdialog-theme-default'
                , controller: 'LoginCtrl',
                scope: $scope
            });
        };

        $scope.popupRegister = function () {
            ngDialog.closeAll();
            registerDialog.open({
                template: 'views/register.html'
                , className: 'ngdialog-theme-default'
                , controller: 'RegisterCtrl'
            });
        }; 
    $scope.popupCreateEventDialog = function () {
            ngDialog.closeAll();
        
            registerDialog.open({
                template: 'views/createEvent.html'
                , className: 'ngdialog-theme-default'
                , controller: 'CreateEventCtrl'
            });
            
        };
    if(invalidEmail=='true'){
        invalidEmailDialog.open({
                template: 'views/invalidemail.html'
                , className: 'ngdialog-theme-default'
                , controller: 'InvalidEmailCtrl',
            closeByEscape: false,
            showClose: false,
            closeByNavigation:false,
            closeByDocument: false
            });
    }
    
    
//       $rootScope.$on("$stateChangeStart", 
//    function (event, toState, toParams, 
//              fromState, fromParams) {
//    if (!AuthService.isAuthorized(toState.data.accessLevel)) {
//        $rootScope.error = "Access denied";
//        event.preventDefault();
//
//    }
//}); 
    });