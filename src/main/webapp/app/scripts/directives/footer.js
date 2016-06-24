'use strict';

/**
 * @ngdoc directive
 * @name yeswesailApp.directive:footer
 * @description
 * # footer
 */
angular.module('yeswesailApp')
  .directive('footer', function () {
    return {
      templateUrl: 'views/footer.html',
      restrict: 'E',
      link: function postLink(scope, element, attrs) {
        
      }
    };
  });
