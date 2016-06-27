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
        element.find('.ui.selection.language.dropdown').dropdown();
          
          scope.changeLanguage=function(lang){
              $http.defaults.headers.common['Language'] = lang;
              LocaleService.setLocale(lang.toLowerCase()+'_'+lang.toUpperCase());
              $state.go($state.current, {}, {reload: true});
              
          }
      }
    };
  });
