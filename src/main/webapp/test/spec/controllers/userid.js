'use strict';

describe('Controller: UseridCtrl', function () {

  // load the controller's module
  beforeEach(module('yeswesailApp'));

  var UseridCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    UseridCtrl = $controller('UseridCtrl', {
      $scope: scope
      // place here mocked dependencies
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(UseridCtrl.awesomeThings.length).toBe(3);
  });
});
