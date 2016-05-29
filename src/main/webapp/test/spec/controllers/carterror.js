'use strict';

describe('Controller: CarterrorCtrl', function () {

  // load the controller's module
  beforeEach(module('yeswesailApp'));

  var CarterrorCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    CarterrorCtrl = $controller('CarterrorCtrl', {
      $scope: scope
      // place here mocked dependencies
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(CarterrorCtrl.awesomeThings.length).toBe(3);
  });
});
