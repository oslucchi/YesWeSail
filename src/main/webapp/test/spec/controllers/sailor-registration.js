'use strict';

describe('Controller: SailorRegistrationCtrl', function () {

  // load the controller's module
  beforeEach(module('yeswesailApp'));

  var SailorRegistrationCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    SailorRegistrationCtrl = $controller('SailorRegistrationCtrl', {
      $scope: scope
      // place here mocked dependencies
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(SailorRegistrationCtrl.awesomeThings.length).toBe(3);
  });
});
