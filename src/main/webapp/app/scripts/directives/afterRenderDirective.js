angular.module('yeswesailApp')
  .directive('afterRender', ['$timeout', function ($timeout) {
        var def = {
            restrict: 'A',
            terminal: true,
            transclude: false,
            link: function (scope, element, attrs) {
                $timeout(scope.$eval(attrs.afterRender), 0);  //Calling a scoped method
            }
        };
        return def;
    }]);