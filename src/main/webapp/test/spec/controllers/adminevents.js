'use strict';

describe('Controller: AdmineventsCtrl', function () {

  // load the controller's module
  beforeEach(module('yeswesailApp'));

  var AdmineventsCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    AdmineventsCtrl = $controller('AdmineventsCtrl', {
      $scope: scope
      // place here mocked dependencies
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(AdmineventsCtrl.awesomeThings.length).toBe(3);
  });
});
