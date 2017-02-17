'use strict';
/**
 * @ngdoc overview
 * @name yeswesailApp
 * @description
 * # yeswesailApp
 *
 * Main module of the application.
 */
angular.module('yeswesailApp', [
    'ngAnimate'
        , 'ngCookies'
        , 'ngResource'
        , 'ngSanitize'
        , 'ngTouch'
        , '720kb.datepicker'
        , 'ngDialog'
        , 'angular-carousel'
    , 'ngLodash'
    , 'uiGmapgoogle-maps'
    , 'ui.router'
    , 'textAngular'
    , 'angucomplete-alt'
    , 'ngAutocomplete'
    , 'toastr'
    , 'ngFileUpload'
    , 'pascalprecht.translate'
 , 'tmh.dynamicLocale'
    , 'gridster'
    , 'datePicker'
    , 'timer'
  ]).constant('AUTH_EVENTS', {
    loginSuccess: 'auth-login-success'
    , loginFailed: 'auth-login-failed'
    , registerSuccess: 'auth-register-success'
    , registerFailed: 'auth-register-failed'
    , logoutSuccess: 'auth-logout-success'
    , logoutFail: 'auth-logout-fail'
    , sessionTimeout: 'auth-session-timeout'
    , notAuthenticated: 'auth-not-authenticated'
    , notAuthorized: 'auth-not-authorized'
}).constant('USER_ROLES', {
    FAKE: 1
    , TRAVELLER: 3
    , SHIPOWNER: 6
    , ADMIN: 9
}).constant('URLs', {
    ddns: 'http://test.yeswesail.com:8080/YesWeSail/'
        //    ddns: 'http://yeswesail.ddns.net:8080/YesWeSail/'
}).constant('LOCALES', {
    'locales': {
        'it_IT': 'Italiano',
        'en_US': 'English',
        'de_DE': 'German'
    },
    'preferredLocale': (navigator.language.startsWith("it") ? "it_IT" : 
    					(navigator.language.startsWith("en") ? "en_US" : 
    					(navigator.language.startsWith("de") ? "de_DE" : "en_US")))
}).service('LocaleService', function ($translate, LOCALES, $rootScope, tmhDynamicLocale) {
    'use strict';
    // PREPARING LOCALES INFO
    var localesObj = LOCALES.locales;
    // locales and locales display names
    var _LOCALES = Object.keys(localesObj);
    if (!_LOCALES || _LOCALES.length === 0) {
        console.error('There are no _LOCALES provided');
    }
    var _LOCALES_DISPLAY_NAMES = [];
    _LOCALES.forEach(function (locale) {
        _LOCALES_DISPLAY_NAMES.push(localesObj[locale]);
    });
    // STORING CURRENT LOCALE
    var currentLocale = $translate.proposedLanguage(); // because of async loading
    // METHODS
    var checkLocaleIsValid = function (locale) {
        return _LOCALES.indexOf(locale) !== -1;
    };
    var setLocale = function (locale) {
        if (!checkLocaleIsValid(locale)) {
            console.error('Locale name "' + locale + '" is invalid');
            return;
        }
        currentLocale = locale;
        // asking angular-translate to load and apply proper translations
        $translate.use(locale);
    };
    // EVENTS
    // on successful applying translations by angular-translate
    $rootScope.$on('$translateChangeSuccess', function (event, data) {
        document.documentElement.setAttribute('lang', data.language); // sets "lang" attribute to html
        // asking angular-dynamic-locale to load and apply proper AngularJS $locale setting
        tmhDynamicLocale.set(data.language.toLowerCase().replace(/_/g, '-'));
    });
    return {
        getLocaleDisplayName: function () {
            return localesObj[currentLocale];
        }
        , setLocaleByDisplayName: function (localeDisplayName) {
            setLocale(_LOCALES[_LOCALES_DISPLAY_NAMES.indexOf(localeDisplayName) // get locale index
                ]);
        }
        , getCurrentLocale: function () {
            return currentLocale;
        }
        , getLocalesDisplayNames: function () {
            return _LOCALES_DISPLAY_NAMES;
        }
        , setLocale: function (locale) {
            setLocale(locale);
        }
    };
}).factory('AuthResolver', function ($state, $http, URLs) {
    return {
        resolve: function () {
            return $http.get(URLs.ddns + 'rest/auth/isAuthenticated').then(function (res) {
                return res.data.authorized;
            }, function (res) {
                return false;
            });
        }
    };
}).config(function ($stateProvider, $urlRouterProvider, $translateProvider, tmhDynamicLocaleProvider, USER_ROLES, $httpProvider, LOCALES) {
    $translateProvider.useStaticFilesLoader({
        prefix: 'resources/locale-', // path to translations files
        suffix: '.json' // suffix, currently- extension of the translations
    });
//    if (navigator.language.indexOf('_') != -1) {
        $translateProvider.preferredLanguage(LOCALES.preferredLocale); // is applied on first load
        $httpProvider.defaults.headers.common['Language'] = navigator.language;
//    }
//    else {
//        $translateProvider.preferredLanguage(navigator.language.replace('-', '_')); // is applied on first load
//        $httpProvider.defaults.headers.common['Language'] = navigator.language.replace('-', '_');
//    }
    tmhDynamicLocaleProvider.localeLocationPattern('bower_components/angular-i18n/angular-locale_{{locale}}.js');
    // For any unmatched url, redirect to /
    $urlRouterProvider.otherwise("/");
    //
    // Now set up the states
    $stateProvider.state('main', {
        url: "/"
        , templateUrl: 'views/main.html'
        , controller: 'MainCtrl'
        , resolve: {
            auth: function resolveAuthentication(AuthResolver) {
                return AuthResolver.resolve();
            }
        }
    }).state('howitworks', {
        url: "/howitworks"
        , templateUrl: 'views/comefunziona.html'
        , controller: 'ComefunzionaCtrl'
        , data: {
            accessLevel: 0
        }
    }).state('userId', {
        url: "/users/{userId}"
        , templateUrl: 'views/userId.html'
        , controller: 'UseridCtrl'
        , accessLevel: USER_ROLES.TRAVELLER
    }).state('userId.profile', {
        url: "/profile"
        , templateUrl: 'views/userId.profile.html'
        , controller: 'UseridProfileCtrl'
        , accessLevel: USER_ROLES.TRAVELLER
    }).state('userId.boats', {
        url: "/boats"
        , templateUrl: 'views/userId.boats.html'
        , controller: 'UseridBoatsCtrl'
        , accessLevel: USER_ROLES.TRAVELLER
    }).state('userId.documents', {
        url: "/documents"
        , templateUrl: 'views/userId.documents.html'
        , controller: 'UseridDocumentsCtrl'
        , accessLevel: USER_ROLES.TRAVELLER
    }).state('userId.info', {
        url: "/personal-info"
        , templateUrl: 'views/userId.info.html'
        , controller: 'UseridInfoCtrl'
        , accessLevel: USER_ROLES.TRAVELLER
        , resolve: {
            auth: function resolveAuthentication(AuthResolver) {
                return AuthResolver.resolve();
            }
        }
    }).state('userId.events', {
        url: "/events"
        , templateUrl: 'views/userId.events.html'
        , controller: 'UseridCtrl'
        , accessLevel: USER_ROLES.TRAVELLER
    }).state('events', {
        url: "/events?location&categoryId"
        , templateUrl: 'views/events.html'
        , controller: 'EventsCtrl'
    }).state('event', {
        url: "/events/:eventId"
        , templateUrl: 'views/eventid.html'
        , controller: 'EventidCtrl'
    }).state('editEvent', {
        url: "/edit-event/:eventId"
        , templateUrl: 'views/create.eventid.html'
        , controller: 'EditEventCtrl'
        , accessLevel: USER_ROLES.SHIPOWNER
    }).state('cart', {
        url: "/cart"
        , templateUrl: 'views/cart.html'
        , controller: 'CartCtrl'
        , accessLevel: USER_ROLES.TRAVELLER
    }).state('cartSuccess', {
        url: "/cart/success"
        , templateUrl: 'views/cartsuccess.html'
        , controller: 'CartSuccessCtrl'
        , accessLevel: USER_ROLES.TRAVELLER
    }).state('cartError', {
        url: "/cart/error"
        , templateUrl: 'views/carterror.html'
        , controller: 'CartErrorCtrl'
        , accessLevel: USER_ROLES.TRAVELLER
    }).state('admin', {
        url: "/admin"
        , templateUrl: 'views/admin.html'
        , controller: 'AdminCtrl'
        , accessLevel: USER_ROLES.ADMIN
    }).state('admin.events', {
        url: "/events"
        , templateUrl: 'views/admin.events.html'
        , controller: 'AdmineventsCtrl'
        , accessLevel: USER_ROLES.ADMIN
    }).state('admin.users', {
        url: "/users"
        , templateUrl: 'views/admin.users.html'
        , controller: 'AdminusersCtrl'
        , accessLevel: USER_ROLES.ADMIN
    }).state('admin.requests', {
        url: "/requests"
        , templateUrl: 'views/admin.requests.html'
        , controller: 'AdminrequestsCtrl'
        , accessLevel: USER_ROLES.ADMIN
    }).state('dynamicPage', {
        url: "/dynamic/{pageRef}"
        , templateUrl: 'views/dynamicPage.html'
        , controller: 'DynamicPageCtrl'
    }).state('dynamicPageEdit', {
        url: "/dynamic/edit/{pageRef}"
        , templateUrl: 'views/dynamicPage.edit.html'
        , controller: 'DynamicPageEditCtrl'
    }).state('sailorRegistration', {
        url: "/sailor-registration"
        , templateUrl: 'views/sailor-registration.html'
        , controller: 'SailorRegistrationCtrl'
        , accessLevel: USER_ROLES.TRAVELLER
    }).state('resetPassword', {
        url: "/reset-password"
        , templateUrl: 'views/reset-password.html'
        , controller: 'PasswordResetCtrl'
    }).state('registerSuccess', {
        url: "/register-success"
        , templateUrl: 'views/register.success.html'
        , controller: 'RegisterSuccessCtrl'
    });
}).run(function ($http, $rootScope, $state, AuthService) {
    //    $http.defaults.headers.common['Language'] = 'IT';
    //    $rootScope.$on("$stateChangeStart", function(event, toState, toParams, fromState, fromParams){
    //         if (!!toState.accessLevel && !AuthService.isAuthorized(toState.accessLevel)) {
    //              event.preventDefault();
    //             AuthService.isAuthenticated().then(function(res){
    //              if (res) {
    //                // user is not allowed
    //                $state.go('main');
    //              } else {
    //                // user is not logged in
    //                $rootScope.$broadcast('LoginRequired', 'Some data');
    //                   event.preventDefault();
    //                   $state.go('main'); 
    //              }
    //             });
    //            }
    //  });
}).directive('dropdown', function ($timeout) {
    return {
        restrict: "A"
        , link: function (scope, elm, attr) {
            $timeout(function () {
                $(elm).dropdown().dropdown('setting', {
                    action: attr.action || 'activate'
                    , onChange: function (value) {
                        scope.$parent[attr.ngModel] = value;
                        scope.$parent.$apply();
                    }
                }).dropdown('set selected', scope.$parent[attr.ngModel]);
            }, 0);
            scope.$watch(attr.ngModel, function (value) {
                $timeout(function () {
                    var selected = $(elm).dropdown('get value');
                    if (value != selected && value != undefined) {
                        $(elm).dropdown('set exactly', value);
                    }
                });
            }, true);
        }
    };
})
    
.directive('dimmer', function ($timeout) {
    return {
        restrict: "A"
        , link: function (scope, elm, attr) {
            $timeout(function () {
                $(elm).dimmer({
                    on: attr.on,
                    opacity: 0.5
                })
            }, 0);
        }
    };
})

.directive('fallbackSrc', function () {
  var fallbackSrc = {
    link: function postLink(scope, iElement, iAttrs) {
      iElement.bind('error', function() {
        angular.element(this).attr("src", iAttrs.fallbackSrc);
      });
    }
   }
   return fallbackSrc;
})
    
    
    .directive('countdown', [
        'Util'
        , '$interval'
        , function (Util, $interval) {
        return {
            restrict: 'A'
            , scope: {
                date: '@'
            }
            , controller: function ($scope, CartService) {
                $scope.cartGetAllItems = function () {
                    CartService.getAllItems();
                }
            }
            , link: function (scope, element) {
                var future;
                future = new Date(scope.date);
                $interval(function () {
                    var diff;
                    diff = Math.floor((future.getTime() - new Date().getTime()) / 1000);
                    if (diff <= 0) {
                        if (diff == -10) {
                            scope.cartGetAllItems();
                        }
                        return '0m 0s';
                    }
                    else return element.text(Util.dhms(diff));
                }, 1000);
            }
        };
        }
    ]).factory('Util', [function () {
    return {
        dhms: function (t) {
            var days, hours, minutes, seconds;
            days = Math.floor(t / 86400);
            t -= days * 86400;
            hours = Math.floor(t / 3600) % 24;
            t -= hours * 3600;
            minutes = Math.floor(t / 60) % 60;
            t -= minutes * 60;
            seconds = t % 60;
            return [
                        minutes + 'm'
                        , seconds + 's'
                    ].join(' ');
        }
    };
        }]).directive('dropdownSelection', function ($timeout) {
    return {
        restrict: "A"
        , link: function (scope, elm, attr) {
            $timeout(function () {
                $(elm).dropdown().dropdown('setting', {
                    action: attr.action || 'activate'
                    , onChange: function (value) {
                        scope.$parent[attr.ngModel] = value;
                        scope.$parent.$apply();
                    }
                });
            }, 0);
            scope.$watch(attr.ngModel, function (value) {
                $timeout(function () {
                    var selected = $(elm).dropdown('get value');
                    if (value != selected[0] && !!value) {
                        $(elm).dropdown('set exactly', value);
                    }
                });
            }, true);
        }
    };
}).directive('dropdownSearchSelection', function ($timeout) {
    return {
        restrict: "A"
        , link: function (scope, elm, attr) {
            $timeout(function () {
                $(elm).dropdown().dropdown('setting', {
                    action: attr.action || 'activate'
                    , onChange: function (value) {
                        scope.$parent[attr.ngModel] = value;
                        scope.$parent.$apply();
                    }
                });
            }, 0);
            scope.$watch(attr.ngModel, function (value) {
                $timeout(function () {
                    var selected = $(elm).dropdown('get value');
                    if (value != selected && !!value) {
                        $(elm).dropdown('set selected', value);
                    }
                });
            }, true);
        }
    };
}).directive('dropdownMenu', function ($timeout) {
    return {
        restrict: "A"
        , link: function (scope, elm, attr) {
            $timeout(function () {
                $(elm).dropdown().dropdown('setting', {
                    action: attr.action || 'nothing'
                });
            }, 0);
        }
    };
}).directive('ngReallyClick', [function () {
    return {
        restrict: 'A'
        , link: function (scope, element, attrs) {
            element.bind('click', function () {
                var message = attrs.ngReallyMessage;
                if (message && confirm(message)) {
                    scope.$apply(attrs.ngReallyClick);
                }
            });
        }
    }
}]).filter('trusted', ['$sce', function ($sce) {
    return function (url) {
        return $sce.trustAsResourceUrl(url);
    };
}]);