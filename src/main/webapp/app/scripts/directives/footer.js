'use strict';

/**
 * @ngdoc directive
 * @name yeswesailApp.directive:footer
 * @description
 * # footer
 */
angular.module('yeswesailApp')
  .directive('footer', function ($state, $http, LocaleService) {
    return {
      templateUrl: 'views/footer.html',
      restrict: 'E',
      link: function postLink(scope, element, attrs) {
        var hideOnStates=['admin', 'admin.events', 'admin.requests', 'admin.users', 'admin.dynamicPages'];
          
          element.find('.ui.selection.language.dropdown').dropdown({
            action: 'activate'
          });
          
          
          scope.shouldItBeVisible=function(){
                if(hideOnStates.indexOf($state.current.name)==-1){
                    return true;
                }else{
                    return false;
                }
          }
          
          
      }
    };
  });
