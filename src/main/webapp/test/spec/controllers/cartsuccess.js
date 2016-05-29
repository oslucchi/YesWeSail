'use strict';

describe('Controller: CartsuccessCtrl', function () {

  // load the controller's module
  beforeEach(module('yeswesailApp'));

  var CartsuccessCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    CartsuccessCtrl = $controller('CartsuccessCtrl', {
      $scope: scope
      // place here mocked dependencies
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(CartsuccessCtrl.awesomeThings.length).toBe(3);
  });
});
