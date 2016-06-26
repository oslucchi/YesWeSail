'use strict';

describe('Controller: RemovepassengerCtrl', function () {

  // load the controller's module
  beforeEach(module('yeswesailApp'));

  var RemovepassengerCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    RemovepassengerCtrl = $controller('RemovepassengerCtrl', {
      $scope: scope
      // place here mocked dependencies
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(RemovepassengerCtrl.awesomeThings.length).toBe(3);
  });
});
