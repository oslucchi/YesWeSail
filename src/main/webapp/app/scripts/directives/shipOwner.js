angular.module('yeswesailApp')
  .directive('shipOwner', function ($http, URLs) {
      return {
        restrict: "E",
        replace: false,
        templateUrl: 'views/shipOwner.sticker.html',
        scope: {
            shipOwnerId: "=",
        },
        link: function (scope, element, attrs) {
              
            $http.get(URLs.ddns + 'rest/users/basic/'+scope.shipOwnerId).then(function(res){
                                    scope.imageURL=res.data.imageURL;                                             
                                                                                 })
        }
    };
    });