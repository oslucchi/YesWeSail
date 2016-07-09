'use strict';

/**
 * @ngdoc overview
 * @name yeswesailApp
 * @description
 * # yeswesailApp
 *
 * Main module of the application.
 */
angular
    .module('yeswesailApp', [
    'ngAnimate'
        , 'ngCookies'
        , 'ngResource'
        , 'ngSanitize'
        , 'ngTouch'
        , '720kb.datepicker'
        , 'ngDialog'
        , 'angular-carousel'
    , 'ngLodash'
    , 'uiGmapgoogle-maps',
    'ui.router',
    'textAngular',
    'angucomplete-alt',
    'ngAutocomplete',
    'toastr',
    'ngFileUpload',
    'pascalprecht.translate',
 'tmh.dynamicLocale',
    'gridster',
    'datePicker'
  ])
    .constant('AUTH_EVENTS', {
        loginSuccess: 'auth-login-success',
        loginFailed: 'auth-login-failed',
        registerSuccess: 'auth-register-success',
        registerFailed: 'auth-register-failed',
        logoutSuccess: 'auth-logout-success',
        logoutFail: 'auth-logout-fail',
        sessionTimeout: 'auth-session-timeout',
        notAuthenticated: 'auth-not-authenticated',
        notAuthorized: 'auth-not-authorized'
    })


.constant('USER_ROLES', {
    all: '*',
    user: 'user',
    admin: 'admin',
    sailor: 'sailor'
})



.constant('URLs', {
    ddns: 'http://yeswesail.ddns.net:8080/YesWeSail/'
})
.constant('LOCALES', {
    'locales': {
        'it_IT': 'Italiano',
        'en_US': 'English'
    },
    'preferredLocale': 'en_US'
})
.service('LocaleService', function ($translate, LOCALES, $rootScope, tmhDynamicLocale) {
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
    var currentLocale = $translate.proposedLanguage();// because of async loading
    
    // METHODS
    var checkLocaleIsValid = function (locale) {
      return _LOCALES.indexOf(locale) !== -1;
    };
    
    var setLocale = function (locale) {
      if (!checkLocaleIsValid(locale)) {
        console.error('Locale name "' + locale + '" is invalid');
        return;
      }
      currentLocale = locale;// updating current locale
    
      // asking angular-translate to load and apply proper translations
      $translate.use(locale);
    };
    
    // EVENTS
    // on successful applying translations by angular-translate
    $rootScope.$on('$translateChangeSuccess', function (event, data) {
      document.documentElement.setAttribute('lang', data.language);// sets "lang" attribute to html
    
       // asking angular-dynamic-locale to load and apply proper AngularJS $locale setting
      tmhDynamicLocale.set(data.language.toLowerCase().replace(/_/g, '-'));
    });
    
    return {
      getLocaleDisplayName: function () {
        return localesObj[currentLocale];
      },
      setLocaleByDisplayName: function (localeDisplayName) {
        setLocale(
          _LOCALES[
            _LOCALES_DISPLAY_NAMES.indexOf(localeDisplayName)// get locale index
            ]
        );
      },
      getLocalesDisplayNames: function () {
        return _LOCALES_DISPLAY_NAMES;
      },
        setLocale: function(locale){
            setLocale(locale);
        }
    };
})


.config(function ($stateProvider, $urlRouterProvider, $translateProvider, tmhDynamicLocaleProvider) {
    
    $translateProvider.useStaticFilesLoader({
        prefix: 'resources/locale-',// path to translations files
        suffix: '.json'// suffix, currently- extension of the translations
    });
    $translateProvider.preferredLanguage('en_US');// is applied on first load
     tmhDynamicLocaleProvider.localeLocationPattern('bower_components/angular-i18n/angular-locale_{{locale}}.js');
    
    
  // For any unmatched url, redirect to /
  
  $urlRouterProvider.otherwise("/");
  //
  // Now set up the states
  $stateProvider
    .state('main', {
      url: "/",
      templateUrl: 'views/main.html',
      controller:'MainCtrl',
      data: {
                accessLevel: 0
            }
    })
    .state('howitworks', {
      url: "/howitworks",
      templateUrl: 'views/comefunziona.html',
      controller:  'ComefunzionaCtrl',
      data: {
                accessLevel: 0
            }
    })
    .state('userId', {
      url: "/users/{userId}",
      templateUrl: 'views/userId.html',
      controller:  'UseridCtrl',
      data: {
                accessLevel: 0
            }
    })
    .state('userId.profile', {
      url: "/profile",
      templateUrl: 'views/userId.profile.html',
      controller:  'UseridProfileCtrl',
      data: {
                accessLevel: 0
            }
    })
    .state('userId.boats', {
      url: "/boats",
      templateUrl: 'views/userId.boats.html',
      controller:  'UseridBoatsCtrl',
      data: {
                accessLevel: 0
            }
    })
    .state('userId.documents', {
      url: "/documents",
      templateUrl: 'views/userId.documents.html',
      controller:  'UseridDocumentsCtrl',
      data: {
                accessLevel: 0
            }
    })
    .state('userId.info', {
      url: "/personal-info",
      templateUrl: 'views/userId.info.html',
      controller:  'UseridInfoCtrl',
      data: {
                accessLevel: 0
            }
    })
    .state('userId.events', {
      url: "/events",
      templateUrl: 'views/userId.events.html',
      controller:  'UseridCtrl',
      data: {
                accessLevel: 0
            }
    })
    .state('events', {
      url: "/events?location&categoryId",
      templateUrl: 'views/events.html',
      controller:  'EventsCtrl',
      data: {
                accessLevel: 0
            }
    })
    .state('event', {
      url: "/events/:eventId",
      templateUrl:  'views/eventid.html',
      controller: 'EventidCtrl',
      data: {
                accessLevel: 0
            }
    })
    .state('editEvent', {
      url: "/edit-event/:eventId",
      templateUrl:  'views/create.eventid.html',
      controller: 'EditEventCtrl',
      data: {
                accessLevel: 3
            }
    })
      .state('cart', {
      url: "/cart",
      templateUrl:  'views/cart.html',
      data: {
                accessLevel: 3
            }
    }) 
      .state('cartSuccess', {
      url: "/cart/success",
      templateUrl:  'views/cartsuccess.html',
      controller: 'CartSuccessCtrl',
      data: {
                accessLevel: 3
            }
    })
      .state('cartError', {
      url: "/cart/error",
      templateUrl:  'views/carterror.html',
      controller: 'CartErrorCtrl',
      data: {
                accessLevel: 3
            }
    })
      .state('admin', {
      url: "/admin",
      templateUrl:  'views/admin.html',
      controller: 'AdminCtrl',
      data: {
                accessLevel: 3
            }
    }) 
      .state('admin.events', {
      url: "/events",
      templateUrl:  'views/admin.events.html',
      controller: 'AdmineventsCtrl',
      data: {
                accessLevel: 3
            }
    }) 
      .state('admin.users', {
      url: "/users",
      templateUrl:  'views/admin.users.html',
      controller: 'AdminCtrl',
      data: {
                accessLevel: 3
            }
    })
      .state('admin.requests', {
      url: "/requests",
      templateUrl:  'views/admin.requests.html',
      controller: 'AdminrequestsCtrl',
      data: {
                accessLevel: 3
            }
    })
      .state('sailorRegistration', {
      url: "/sailor-registration",
      templateUrl:  'views/sailor-registration.html',
      controller: 'SailorRegistrationCtrl',
      data: {
                accessLevel: 3
            }
    });
    
    
    })
    .run(function($http) {
    
     $http.defaults.headers.common['Language'] = 'IT';
 
})
    
    
    
    .factory('AuthResolver', function ($q, $rootScope, $state) {
  return {
    resolve: function () {
      var deferred = $q.defer();
      var unwatch = $rootScope.$watch('currentUser', function (currentUser) {
        if (angular.isDefined(currentUser)) {
          if (currentUser) {
            deferred.resolve(currentUser);
          } else {
            deferred.reject();
            $state.go('main');
          }
          unwatch();
        }
      });
      return deferred.promise;
    }
  };
})

.directive('dropdown', function ($timeout) {
    return {
        restrict: "C",
        link: function (scope, elm, attr) {
            $timeout(function () {
                $(elm).dropdown().dropdown('setting', {
                    onChange: function (value) {
                        scope.$parent[attr.ngModel] = value;
                        scope.$parent.$apply();
                    }
                });
            }, 0);
        }
    };
})

.directive('ngReallyClick', [function() {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            element.bind('click', function() {
                var message = attrs.ngReallyMessage;
                if (message && confirm(message)) {
                    scope.$apply(attrs.ngReallyClick);
                }
            });
        }
    }
}]).filter('trusted', ['$sce', function ($sce) {
    return function(url) {
        return $sce.trustAsResourceUrl(url);
    };
}]);
