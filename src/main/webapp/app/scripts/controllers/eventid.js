'use strict';

/**
 * @ngdoc function
 * @name yeswesailApp.controller:EventsEventidCtrl
 * @description
 * # EventsEventidCtrl
 * Controller of the yeswesailApp
 */
angular.module('yeswesailApp')
    .controller('EventidCtrl', function ($scope, $http, URLs, $routeParams) {

        $scope.event = {
            image: 'http://placehold.it/1900x400?text='
            , price: '100'
            , date: '20160505'
            , title: 'Event Title'
            , description: 'Phasellus  et  felis  posuere,  suscipit  purus  in,  lacinia  sem.  Nunc  tincidunt  tellus egestas, volutpat  justo ultrices,  consequat  ante. Aliquam ac vestibulum  tortor,  et fermentum  lorem.  Cras  lacinia  a  velit  at  fermentum.  Aliquam  erat  volutpat. Vestibulum ac tempus purus. Praesent dolor leo, placerat quis accumsan et, iaculis sit amet risus. Praesent fringilla lectus id porta egestas. Aliquam vitae erat est. Nam et elit sit amet metus tempor gravida at quis diam.'
            , images: [{
                    url: 'http://placehold.it/500x300'
                    , desc: 'test'
                }
                , {
                    url: 'http://placehold.it/500x300'
                    , desc: 'test'
                }
                , {
                    url: 'http://placehold.it/500x300'
                    , desc: 'test'
                }
                , {
                    url: 'http://placehold.it/500x300'
                    , desc: 'test'
                }

        ]
        };

        $http.post(URLs.ddns + 'rest/events/details', {eventId: $routeParams.eventId}).then(function(res){
            console.log(res.data);
        }, function(err){});
    });