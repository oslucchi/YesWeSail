'use strict';

describe('Controller: AddpassengerCtrl', function () {

  // load the controller's module
  beforeEach(module('yeswesailApp'));

  var AddpassengerCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    AddpassengerCtrl = $controller('AddpassengerCtrl', {
      $scope: scope
      // place here mocked dependencies
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(AddpassengerCtrl.awesomeThings.length).toBe(3);
  });
});
