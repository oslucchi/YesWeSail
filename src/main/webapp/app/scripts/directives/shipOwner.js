angular.module('yeswesailApp')
  .directive('shipOwner', [function ($http) {
      return {
        restrict: "E",
        replace: false,
        templateUrl: 'views/shipOwner.sticker.html',
        scope: {
            shipOwnerId: "=",
        },
        link: function (scope, element, attrs) {
            scope.imageURL='https://scontent-mxp1-1.xx.fbcdn.net/v/t1.0-1/c0.41.153.153/1897878_10203059351146553_1428969181_n.jpg?oh=631c448bf10972dd6feddc9b68d55f6a&oe=579B2AAB'   
       
        }
    };
    }]);