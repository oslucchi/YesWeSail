'use strict';

describe('Controller: ApplicationcontrollerCtrl', function () {

  // load the controller's module
  beforeEach(module('yeswesailApp'));

  var ApplicationcontrollerCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    ApplicationcontrollerCtrl = $controller('ApplicationcontrollerCtrl', {
      $scope: scope
      // place here mocked dependencies
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(ApplicationcontrollerCtrl.awesomeThings.length).toBe(3);
  });
});
