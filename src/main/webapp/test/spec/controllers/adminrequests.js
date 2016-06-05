'use strict';

describe('Controller: AdminrequestsCtrl', function () {

  // load the controller's module
  beforeEach(module('yeswesailApp'));

  var AdminrequestsCtrl,
    scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    AdminrequestsCtrl = $controller('AdminrequestsCtrl', {
      $scope: scope
      // place here mocked dependencies
    });
  }));

  it('should attach a list of awesomeThings to the scope', function () {
    expect(AdminrequestsCtrl.awesomeThings.length).toBe(3);
  });
});
