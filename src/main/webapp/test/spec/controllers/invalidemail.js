'use strict';

describe('Controller: InvalidemailCtrl', function () {

  // load the controller's module
  beforeEach(module('yeswesailApp'));

  var InvalidemailCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    InvalidemailCtrl = $controller('InvalidemailCtrl', {
      $scope: scope
      // place here mocked dependencies
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(InvalidemailCtrl.awesomeThings.length).toBe(3);
  });
});
