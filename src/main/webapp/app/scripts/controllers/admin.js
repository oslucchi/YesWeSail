'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
  .controller('AdminCtrl', function ($scope, $http, URLs, MAPS, lodash ) {
        $http.post(URLs.ddns + 'rest/events/uploadMultiImage', {eventRef: 'Non capisco a cosa serva sta chiamata!'});
       
  });
