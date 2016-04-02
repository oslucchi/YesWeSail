'use strict';

/**
 * @ngdoc filter
 * @name yeswesailApp.filter:noemaildomain
 * @function
 * @description
 * # noemaildomain
 * Filter in the yeswesailApp.
 */
angular.module('yeswesailApp')
  .filter('noemaildomain', function () {
    return function (input) {
      return (!input) ? '' : input.replace(/@.*/g, '');
    };
  });
